import { Request, Response } from 'express';
import { pool } from '../config/db';
import { toDeviceDto } from '../dtos/device.dto';
import { sendSuccess, sendError } from '../utils/responseEnvelope';
import { syncDevicesSchema } from '../validators/device.validator';

export const getDevices = async (req: Request, res: Response) => {
  const reqId = req.requestId;
  console.log(`[${reqId}] DEVICES GET /api/v1/devices requested`);

  try {
    const result = await pool.query('SELECT * FROM devices ORDER BY last_seen DESC');
    const dtos = result.rows.map(toDeviceDto);
    console.log(`[${reqId}] DB Fetched ${dtos.length} device records from PostgreSQL`);
    return sendSuccess(res, dtos);
  } catch (error: any) {
    console.error(`[${reqId}] ❌ DB Error fetching devices:`, error.message);
    return sendError(res, 'Failed to fetch devices from database', 'DB_ERROR', 500);
  }
};

export const getDeviceById = async (req: Request, res: Response) => {
  const reqId = req.requestId;
  const { id } = req.params;
  console.log(`[${reqId}] DEVICES GET /api/v1/devices/${id} requested`);

  try {
    const result = await pool.query('SELECT * FROM devices WHERE device_id = $1 OR id::text = $1', [id]);
    if (result.rows.length === 0) {
      console.log(`[${reqId}] DEVICES Device not found: ${id}`);
      return sendError(res, `Device with ID ${id} not found`, 'NOT_FOUND', 404);
    }
    const dto = toDeviceDto(result.rows[0]);
    console.log(`[${reqId}] DB Fetched device details for ID: ${id}`);
    return sendSuccess(res, dto);
  } catch (error: any) {
    console.error(`[${reqId}] ❌ DB Error fetching device ${id}:`, error.message);
    return sendError(res, 'Failed to fetch device details', 'DB_ERROR', 500);
  }
};

export const syncDevices = async (req: Request, res: Response) => {
  const reqId = req.requestId;

  const validation = syncDevicesSchema.safeParse(req.body);
  if (!validation.success) {
    const err = validation.error.errors.map(e => e.message).join(', ');
    console.log(`[${reqId}] DEVICES Sync validation error: ${err}`);
    return sendError(res, err, 'VALIDATION_ERROR', 400);
  }

  const { devices } = validation.data;
  console.log(`[${reqId}] DEVICES Sync request received with ${devices.length} devices from Android client`);

  try {
    for (const device of devices) {
      const devId = device.deviceId || device.ipAddress;
      const typeStr = device.type || 'UNKNOWN';

      await pool.query(
        `INSERT INTO devices (
            device_id, name, type, ip_address, status, discovery_source, last_seen
         )
         VALUES ($1, $2, $3, $4, $5, 'ANDROID_APP_SYNC', CURRENT_TIMESTAMP)
         ON CONFLICT (device_id) DO UPDATE SET
            ip_address = EXCLUDED.ip_address,
            name = EXCLUDED.name,
            type = EXCLUDED.type,
            discovery_source = 'ANDROID_APP_SYNC',
            last_seen = CURRENT_TIMESTAMP,
            status = EXCLUDED.status`,
        [
          devId,
          device.name || 'Discovered Asset',
          typeStr,
          device.ipAddress,
          device.status || 'ONLINE'
        ]
      );
      console.log(`[${reqId}] DB Persisted Android-scanned device: ${device.name} (${device.ipAddress}) [ID: ${devId}]`);
    }

    console.log(`[${reqId}] DEVICES Sync succeeded for ${devices.length} devices`);
    return sendSuccess(res, { syncedCount: devices.length, message: 'Devices synced successfully' });
  } catch (error: any) {
    console.error(`[${reqId}] ❌ DEVICES Sync error:`, error.message);
    return sendError(res, 'Failed to sync devices to database', 'DB_ERROR', 500);
  }
};

export const quarantineDevice = async (req: Request, res: Response) => {
  const reqId = req.requestId;
  const { id } = req.params;

  console.log(`[${reqId}] DEVICES Quarantine request for device ID: ${id}`);

  try {
    const result = await pool.query(
      `UPDATE devices
       SET is_quarantined = true, status = 'BLOCKED', last_seen = CURRENT_TIMESTAMP
       WHERE device_id = $1 OR id::text = $1
       RETURNING *`,
      [id]
    );

    if (result.rows.length === 0) {
      console.log(`[${reqId}] DEVICES Quarantine failed: Device ${id} not found`);
      return sendError(res, `Device ${id} not found`, 'NOT_FOUND', 404);
    }

    console.log(`[${reqId}] DB Successfully quarantined device ID: ${id}`);
    return sendSuccess(res, { message: `Device ${id} successfully quarantined.`, device: toDeviceDto(result.rows[0]) });
  } catch (error: any) {
    console.error(`[${reqId}] ❌ DB Error quarantining device ${id}:`, error.message);
    return sendError(res, 'Failed to quarantine device', 'DB_ERROR', 500);
  }
};

export const unquarantineDevice = async (req: Request, res: Response) => {
  const reqId = req.requestId;
  const { id } = req.params;

  console.log(`[${reqId}] DEVICES Unquarantine request for device ID: ${id}`);

  try {
    const result = await pool.query(
      `UPDATE devices
       SET is_quarantined = false, status = 'ONLINE', last_seen = CURRENT_TIMESTAMP
       WHERE device_id = $1 OR id::text = $1
       RETURNING *`,
      [id]
    );

    if (result.rows.length === 0) {
      console.log(`[${reqId}] DEVICES Unquarantine failed: Device ${id} not found`);
      return sendError(res, `Device ${id} not found`, 'NOT_FOUND', 404);
    }

    console.log(`[${reqId}] DB Successfully unquarantined device ID: ${id}`);
    return sendSuccess(res, { message: `Device ${id} successfully unquarantined.`, device: toDeviceDto(result.rows[0]) });
  } catch (error: any) {
    console.error(`[${reqId}] ❌ DB Error unquarantining device ${id}:`, error.message);
    return sendError(res, 'Failed to unquarantine device', 'DB_ERROR', 500);
  }
};
