import { Request, Response } from 'express';
import { pool } from '../config/db';
import { toAlertDto } from '../dtos/alert.dto';

export const getAlerts = async (req: Request, res: Response) => {
  const clientIp = req.ip || req.socket.remoteAddress;
  console.log(`📱 [ANDROID CLIENT REQUEST] GET /api/v1/alerts requested from client IP: ${clientIp}`);
  try {
    const query = `
      SELECT a.*, d.name as device_name
      FROM alerts a
      LEFT JOIN devices d ON a.device_id = d.device_id
      ORDER BY a.created_at DESC
    `;
    const result = await pool.query(query);
    const dtos = result.rows.map(toAlertDto);
    console.log(`📱 [ANDROID CLIENT REQUEST] Returning ${dtos.length} alert records to client IP: ${clientIp}`);
    res.json(dtos);
  } catch (error) {
    console.error(`❌ [ANDROID CLIENT REQUEST] Failed to fetch alerts for client IP: ${clientIp}`, error);
    res.status(500).json({ error: 'Failed to fetch alerts' });
  }
};
