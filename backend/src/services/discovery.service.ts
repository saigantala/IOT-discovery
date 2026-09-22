import find from 'local-devices';
import { pool } from '../config/db';
import { socketService } from './socket.service';

export class DiscoveryService {
  static async scanNetwork() {
    console.log('\n🔍 [BACKEND AUTO-DISCOVERY] Starting Background Network Discovery...');

    try {
      const devices = await find();
      console.log(`✅ [BACKEND AUTO-DISCOVERY] Scan complete. Found ${devices.length} real devices on server subnet.`);

      for (const d of devices) {
        const deviceId = d.mac.toUpperCase();
        const name = d.name !== '?' ? d.name : `Server-Discovered Device (${d.ip})`;

        const query = `
          INSERT INTO devices (device_id, name, ip_address, type, status, discovery_source, last_seen)
          VALUES ($1, $2, $3, $4, $5, 'BACKEND_AUTO_DISCOVERY', CURRENT_TIMESTAMP)
          ON CONFLICT (device_id) DO UPDATE SET
            ip_address = EXCLUDED.ip_address,
            name = EXCLUDED.name,
            discovery_source = 'BACKEND_AUTO_DISCOVERY',
            last_seen = CURRENT_TIMESTAMP,
            status = 'ONLINE'
          RETURNING *;
        `;

        const result = await pool.query(query, [deviceId, name, d.ip, 'Hardware Node', 'ONLINE']);
        const updatedDevice = result.rows[0];
        console.log(`💾 [BACKEND AUTO-DISCOVERY] Saved device: ${updatedDevice.name} (${updatedDevice.ip_address}) [ID: ${updatedDevice.device_id}]`);

        socketService.emit('device_discovered', updatedDevice);
      }
    } catch (error) {
      console.error('❌ [BACKEND AUTO-DISCOVERY] Error during network discovery:', error);
    }
  }

  static startAutoDiscovery() {
    this.scanNetwork();
    setInterval(() => this.scanNetwork(), 120000);
  }
}
