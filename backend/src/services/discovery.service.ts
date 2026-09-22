import find from 'local-devices';
import { pool } from '../config/db';
import { socketService } from './socket.service';

const AUTO_DISCOVERY_ENABLED = process.env.AUTO_DISCOVERY_ENABLED !== 'false';

export class DiscoveryService {
  static async scanNetwork() {
    if (!AUTO_DISCOVERY_ENABLED) {
      console.log('🔍 [BACKEND AUTO-DISCOVERY] Auto-discovery is disabled via AUTO_DISCOVERY_ENABLED=false flag.');
      return;
    }

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

        // Keep this value compatible with Android's DeviceType enum and API DTOs.
        const result = await pool.query(query, [deviceId, name, d.ip, 'UNKNOWN', 'ONLINE']);
        const updatedDevice = result.rows[0];
        console.log(`💾 [BACKEND AUTO-DISCOVERY] Saved device: ${updatedDevice.name} (${updatedDevice.ip_address}) [ID: ${updatedDevice.device_id}]`);

        socketService.emit('device_discovered', updatedDevice);
      }
    } catch (error) {
      console.error('❌ [BACKEND AUTO-DISCOVERY] Error during network discovery:', error);
    }
  }

  static startAutoDiscovery() {
    if (!AUTO_DISCOVERY_ENABLED) {
      console.log('🔍 [BACKEND AUTO-DISCOVERY] Auto-discovery is disabled via environment configuration.');
      return;
    }
    this.scanNetwork();
    setInterval(() => this.scanNetwork(), 120000);
  }
}
