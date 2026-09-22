import { pool, withTransaction } from '../config/db';
import { MLService } from './ml.service';
import { socketService } from './socket.service';
import { toTrafficDto, TrafficResponseDto } from '../dtos/traffic.dto';
import { toAlertDto } from '../dtos/alert.dto';

export class TrafficService {
  static async processTraffic(data: any): Promise<TrafficResponseDto> {
    const device_id = data.deviceId || data.device_id;
    const packet_rate = Number(data.packetRate ?? data.packet_rate ?? 0);
    const dest_diversity = Number(data.destDiversity ?? data.dest_diversity ?? 0);
    const port_diversity = Number(data.portDiversity ?? data.port_diversity ?? 0);
    const mqtt_freq = Number(data.mqttFreq ?? data.mqtt_freq ?? 0);
    const attack_type = data.attackType || data.attack_type || (data.isSimulatedAttack ? 'Simulated Attack' : 'AI Anomaly');

    if (!device_id) {
      throw new Error('deviceId is required for traffic report');
    }

    // 1. Get ML Prediction
    const prediction = await MLService.predict({
      packet_rate, dest_diversity, port_diversity, mqtt_freq
    });

    return await withTransaction(async (client) => {
      // 2. Persist Device
      await client.query(
        `INSERT INTO devices (device_id, name, type, status, last_seen)
         VALUES ($1, $2, $3, $4, CURRENT_TIMESTAMP)
         ON CONFLICT (device_id) DO UPDATE SET last_seen = CURRENT_TIMESTAMP`,
        [device_id, `Device ${device_id}`, 'UNKNOWN', 'ONLINE']
      );

      // 3. Save Traffic Event
      const eventResult = await client.query(
        `INSERT INTO traffic_events (device_id, packet_rate, dest_diversity, port_diversity, mqtt_freq, risk_score, is_anomaly)
         VALUES ($1, $2, $3, $4, $5, $6, $7) RETURNING *`,
        [device_id, packet_rate, dest_diversity, port_diversity, mqtt_freq, prediction.risk_score, prediction.is_anomaly]
      );
      const eventRow = eventResult.rows[0];

      // 4. Handle Alert
      if (prediction.is_anomaly) {
        const severity = prediction.risk_score > 0.8 ? 'CRITICAL' : 'HIGH';
        const alertResult = await client.query(
          `INSERT INTO alerts (device_id, traffic_event_id, risk_score, attack_type, severity, status)
           VALUES ($1, $2, $3, $4, $5, $6) RETURNING *`,
          [device_id, eventRow.id, prediction.risk_score, attack_type, severity, 'OPEN']
        );

        const alertRow = alertResult.rows[0];
        const alertDto = toAlertDto(alertRow);

        socketService.emit('new_alert', alertDto);
      }

      const trafficDto = toTrafficDto(eventRow);
      socketService.emit('new_traffic', trafficDto);
      return trafficDto;
    });
  }

  static async logSession(sessionData: any) {
    const deviceId = sessionData.deviceId || sessionData.device_id;
    const protocol = sessionData.protocol || 'TCP';
    const sourcePort = Number(sessionData.sourcePort ?? sessionData.source_port ?? 0);
    const destPort = Number(sessionData.destPort ?? sessionData.dest_port ?? 0);
    const bytesSent = Number(sessionData.bytesSent ?? sessionData.bytes_sent ?? 0);
    const bytesReceived = Number(sessionData.bytesReceived ?? sessionData.bytes_received ?? 0);
    const status = sessionData.status || 'ACTIVE';

    if (!deviceId) throw new Error('deviceId is required for session log');

    return await withTransaction(async (client) => {
      // Ensure device exists
      await client.query(
        `INSERT INTO devices (device_id, name, type, status, last_seen)
         VALUES ($1, $2, 'UNKNOWN', 'ONLINE', CURRENT_TIMESTAMP)
         ON CONFLICT (device_id) DO UPDATE SET last_seen = CURRENT_TIMESTAMP`,
        [deviceId, `Device ${deviceId}`]
      );

      const query = `
        INSERT INTO network_sessions (device_id, protocol, source_port, dest_port, bytes_sent, bytes_received, status)
        VALUES ($1, $2, $3, $4, $5, $6, $7) RETURNING *
      `;

      const result = await client.query(query, [
        deviceId, protocol, sourcePort, destPort, bytesSent, bytesReceived, status
      ]);

      return result.rows[0];
    });
  }
}
