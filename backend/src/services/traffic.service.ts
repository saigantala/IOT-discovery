import { pool } from '../config/db';
import { MLService } from './ml.service';
import { socketService } from './socket.service';

export class TrafficService {
  static async processTraffic(data: any) {
    const { device_id, packet_rate, dest_diversity, port_diversity, mqtt_freq, attack_type } = data;

    // 1. Get ML Prediction
    const prediction = await MLService.predict({
      packet_rate, dest_diversity, port_diversity, mqtt_freq
    });

    // 2. Persist Device
    await pool.query(
      `INSERT INTO devices (device_id, name, type, status, last_seen)
       VALUES ($1, $2, $3, $4, CURRENT_TIMESTAMP)
       ON CONFLICT (device_id) DO UPDATE SET last_seen = CURRENT_TIMESTAMP`,
      [device_id, `Device ${device_id}`, 'IoT node', 'ONLINE']
    );

    // 3. Save Traffic Event
    const eventResult = await pool.query(
      `INSERT INTO traffic_events (device_id, packet_rate, dest_diversity, port_diversity, mqtt_freq, risk_score, is_anomaly)
       VALUES ($1, $2, $3, $4, $5, $6, $7) RETURNING *`,
      [device_id, packet_rate, dest_diversity, port_diversity, mqtt_freq, prediction.risk_score, prediction.is_anomaly]
    );
    const event = eventResult.rows[0];

    // 4. Handle Alert
    if (prediction.is_anomaly) {
      const severity = prediction.risk_score > 0.8 ? 'CRITICAL' : 'HIGH';
      const alertResult = await pool.query(
        `INSERT INTO alerts (device_id, traffic_event_id, risk_score, attack_type, severity, status)
         VALUES ($1, $2, $3, $4, $5, $6) RETURNING *`,
        [device_id, event.id, prediction.risk_score, attack_type || 'AI-Detected Anomaly', severity, 'OPEN']
      );

      // Fetch device info for the socket event
      const deviceRes = await pool.query('SELECT * FROM devices WHERE device_id = $1', [device_id]);
      const alertWithDevice = { ...alertResult.rows[0], device: deviceRes.rows[0] };

      socketService.emit('new_alert', alertWithDevice);
    }

    socketService.emit('new_traffic', event);
    return event;
  }

  // New method for session logging as requested
  static async logSession(sessionData: any) {
    const { deviceId, protocol, sourcePort, destPort, bytesSent, bytesReceived, status } = sessionData;

    const query = `
      INSERT INTO network_sessions (device_id, protocol, source_port, dest_port, bytes_sent, bytes_received, status)
      VALUES ($1, $2, $3, $4, $5, $6, $7) RETURNING *
    `;

    const result = await pool.query(query, [
      deviceId, protocol, sourcePort, destPort, bytesSent, bytesReceived, status
    ]);

    return result.rows[0];
  }
}
