import { Request, Response } from 'express';
import { pool } from '../config/db';

export const getDevices = async (req: Request, res: Response) => {
  const clientIp = req.ip || req.socket.remoteAddress;
  console.log(`📱 [ANDROID CLIENT REQUEST] GET /api/v1/devices requested from client IP: ${clientIp}`);
  try {
    const result = await pool.query('SELECT * FROM devices ORDER BY last_seen DESC');
    console.log(`📱 [ANDROID CLIENT REQUEST] Returning ${result.rows.length} device records to client IP: ${clientIp}`);
    res.json(result.rows);
  } catch (error) {
    console.error(`❌ [ANDROID CLIENT REQUEST] Failed to fetch devices for client IP: ${clientIp}`, error);
    res.status(500).json({ error: 'Failed to fetch devices' });
  }
};

export const getDeviceById = async (req: Request, res: Response) => {
  const clientIp = req.ip || req.socket.remoteAddress;
  console.log(`📱 [ANDROID CLIENT REQUEST] GET /api/v1/devices/${req.params.id} requested from client IP: ${clientIp}`);
  try {
    const result = await pool.query('SELECT * FROM devices WHERE device_id = $1', [req.params.id]);
    if (result.rows.length === 0) return res.status(404).json({ error: 'Device not found' });
    res.json(result.rows[0]);
  } catch (error) {
    console.error(`❌ [ANDROID CLIENT REQUEST] Failed to fetch device ${req.params.id} for client IP: ${clientIp}`, error);
    res.status(500).json({ error: 'Failed to fetch device details' });
  }
};

export const syncDevices = async (req: Request, res: Response) => {
  const clientIp = req.ip || req.socket.remoteAddress;
  const { devices } = req.body;

  console.log(`📱 [ANDROID CLIENT SYNC] Received POST /api/v1/devices/sync request from Android device (${clientIp})`);

  if (!Array.isArray(devices)) {
    console.warn(`⚠️ [ANDROID CLIENT SYNC] Invalid devices payload from Android device (${clientIp})`);
    return res.status(400).json({ error: 'Invalid data format' });
  }

  console.log(`📱 [ANDROID CLIENT SYNC] Processing ${devices.length} devices discovered by Android device (${clientIp})...`);

  try {
    for (const device of devices) {
      await pool.query(
        `INSERT INTO devices (device_id, name, type, ip_address, status, discovery_source, last_seen)
         VALUES ($1, $2, $3, $4, $5, 'ANDROID_APP_SYNC', CURRENT_TIMESTAMP)
         ON CONFLICT (device_id) DO UPDATE SET
            ip_address = EXCLUDED.ip_address,
            name = EXCLUDED.name,
            discovery_source = 'ANDROID_APP_SYNC',
            last_seen = CURRENT_TIMESTAMP,
            status = EXCLUDED.status`,
        [device.deviceId, device.name, device.type, device.ipAddress, device.status]
      );
      console.log(`💾 [ANDROID CLIENT SYNC] Saved/Updated device from Android client (${clientIp}): ${device.name} (${device.ipAddress}) [ID: ${device.deviceId}]`);
    }
    console.log(`✅ [ANDROID CLIENT SYNC] Successfully synced ${devices.length} devices from Android device (${clientIp})`);
    res.json({ success: true, message: 'Devices synced successfully', count: devices.length });
  } catch (error) {
    console.error(`❌ [ANDROID CLIENT SYNC] Sync error for client ${clientIp}:`, error);
    res.status(500).json({ error: 'Failed to sync devices' });
  }
};
