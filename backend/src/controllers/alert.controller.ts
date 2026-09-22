import { Request, Response } from 'express';
import { pool } from '../config/db';

export const getAlerts = async (req: Request, res: Response) => {
  try {
    const query = `
      SELECT a.*, d.name as device_name
      FROM alerts a
      JOIN devices d ON a.device_id = d.device_id
      ORDER BY a.created_at DESC
    `;
    const result = await pool.query(query);
    res.json(result.rows);
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch alerts' });
  }
};
