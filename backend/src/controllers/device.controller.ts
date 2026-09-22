import { Request, Response } from 'express';
import { pool } from '../config/db';
import { toDeviceDto } from '../dtos/device.dto';

export const getDevices = async (req: Request, res: Response) => {
  const clientIp = req.ip || req.socket.remoteAddress;
  console.log(`📱 [ANDROID CLIENT REQUEST] GET /api/v1/devices requested from client IP: ${clientIp}`);
  try {
    const result = await pool.query('SELECT * FROM devices ORDER BY last_seen DESC');
    const dtos = result.rows.map(toDeviceDto);
    console.log(`📱 [ANDROID CLIENT REQUEST] Returning ${dtos.length} device records to client IP: ${clientIp}`);
    res.json(dtos);
  } catch (error) {
    console.error(`❌ [ANDROID CLIENT REQUEST] Failed to fetch devices for client IP: ${clientIp}`, error);
    res.status(500).json({ error: 'Failed to fetch devices' });
  }
};

export const getDeviceById = async (req: Request, res: Response) => {
  const clientIp = req.ip || req.socket.remoteAddress;
  console.log(`📱 [ANDROID CLIENT REQUEST] GET /api/v1/devices/${req.params.id} requested from client IP: ${clientIp}`);
  try {
    const result = await pool.query('SELECT * FROM devices WHERE device_id = $1 OR id::text = $1', [req.params.id]);
    if (result.rows.length === 0) return res.status(404).json({ error: 'Device not found' });
    res.json(toDeviceDto(result.rows[0]));
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
      const devId = device.deviceId || device.id || device.ipAddress;
      const typeStr = device.type || 'UNKNOWN';
      const openPortsJson = JSON.stringify(device.openPorts || []);
      const servicesJson = JSON.stringify(device.services || []);

      await pool.query(
        `INSERT INTO devices (
            device_id, name, type, ip_address, status, discovery_source,
            manufacturer, hostname, os, risk_level, risk_score, risk_reason,
            open_ports, services, fingerprint_confidence, last_seen
         )
         VALUES ($1, $2, $3, $4, $5, 'ANDROID_APP_SYNC', $6, $7, $8, $9, $10, $11, $12, $13, $14, CURRENT_TIMESTAMP)
         ON CONFLICT (device_id) DO UPDATE SET
            ip_address = EXCLUDED.ip_address,
            name = EXCLUDED.name,
            type = EXCLUDED.type,
            discovery_source = 'ANDROID_APP_SYNC',
            manufacturer = EXCLUDED.manufacturer,
            hostname = EXCLUDED.hostname,
            os = EXCLUDED.os,
            risk_level = EXCLUDED.risk_level,
            risk_score = EXCLUDED.risk_score,
            open_ports = EXCLUDED.open_ports,
            services = EXCLUDED.services,
            last_seen = CURRENT_TIMESTAMP,
            status = EXCLUDED.status`,
        [
          devId,
          device.name || 'Discovered Asset',
          typeStr,
          device.ipAddress,
          device.status || 'ONLINE',
          device.manufacturer || 'Unknown Vendor',
          device.hostname || device.ipAddress,
          device.os || 'Mobile OS',
          device.riskLevel || 'LOW',
          device.riskScore || 0,
          device.riskReason || 'Scanned by Android Client',
          openPortsJson,
          servicesJson,
          device.fingerprintConfidence || 90
        ]
      );
      console.log(`💾 [ANDROID CLIENT SYNC] Saved/Updated device from Android client (${clientIp}): ${device.name} (${device.ipAddress}) [ID: ${devId}]`);
    }
    console.log(`✅ [ANDROID CLIENT SYNC] Successfully synced ${devices.length} devices from Android device (${clientIp})`);
    res.json({ success: true, message: 'Devices synced successfully', count: devices.length });
  } catch (error) {
    console.error(`❌ [ANDROID CLIENT SYNC] Sync error for client ${clientIp}:`, error);
    res.status(500).json({ error: 'Failed to sync devices' });
  }
};

export const quarantineDevice = async (req: Request, res: Response) => {
  const { id } = req.params;
  const clientIp = req.ip || req.socket.remoteAddress;

  console.log(`🔒 [QUARANTINE DEVICE] Quarantine request for device: ${id} from client IP: ${clientIp}`);

  try {
    const result = await pool.query(
      `UPDATE devices
       SET is_quarantined = true, status = 'BLOCKED', last_seen = CURRENT_TIMESTAMP
       WHERE device_id = $1 OR id::text = $1
       RETURNING *`,
      [id]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({ success: false, message: 'Device not found' });
    }

    console.log(`🔒 [QUARANTINE DEVICE] Successfully quarantined device: ${id}`);
    res.json({ success: true, message: `Device ${id} successfully quarantined.` });
  } catch (error) {
    console.error(`❌ [QUARANTINE DEVICE] Failed to quarantine device ${id}:`, error);
    res.status(500).json({ success: false, message: 'Failed to quarantine device' });
  }
};
