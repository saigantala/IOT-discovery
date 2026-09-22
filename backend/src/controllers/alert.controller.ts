import { Request, Response } from 'express';
import { pool } from '../config/db';
import { toAlertDto } from '../dtos/alert.dto';
import { sendSuccess, sendError } from '../utils/responseEnvelope';

export const getAlerts = async (req: Request, res: Response) => {
  const reqId = req.requestId;
  console.log(`[${reqId}] ALERTS GET /api/v1/alerts requested`);

  try {
    const query = `
      SELECT a.*, d.name as device_name
      FROM alerts a
      LEFT JOIN devices d ON a.device_id = d.device_id
      ORDER BY a.created_at DESC
    `;
    const result = await pool.query(query);
    const dtos = result.rows.map(toAlertDto);
    console.log(`[${reqId}] DB Fetched ${dtos.length} alert records from PostgreSQL`);
    return sendSuccess(res, dtos);
  } catch (error: any) {
    console.error(`[${reqId}] ❌ DB Error fetching alerts:`, error.message);
    return sendError(res, 'Failed to fetch alerts from database', 'DB_ERROR', 500);
  }
};

export const updateAlertStatus = async (req: Request, res: Response) => {
  const reqId = req.requestId;
  const { id } = req.params;
  const { status } = req.body;

  const validStatuses = ['OPEN', 'ACKNOWLEDGED', 'RESOLVED', 'READ'];
  const newStatus = (status || 'RESOLVED').toUpperCase();

  if (!validStatuses.includes(newStatus)) {
    return sendError(res, `Invalid alert status. Must be one of: ${validStatuses.join(', ')}`, 'VALIDATION_ERROR', 400);
  }

  console.log(`[${reqId}] ALERTS PATCH /api/v1/alerts/${id} status -> ${newStatus}`);

  try {
    const result = await pool.query(
      `UPDATE alerts
       SET status = $1
       WHERE id::text = $2 OR device_id = $2
       RETURNING *`,
      [newStatus, id]
    );

    if (result.rows.length === 0) {
      console.log(`[${reqId}] ALERTS Update failed: Alert ID ${id} not found`);
      return sendError(res, `Alert with ID ${id} not found`, 'NOT_FOUND', 404);
    }

    console.log(`[${reqId}] DB Successfully updated alert ${id} status to ${newStatus}`);
    return sendSuccess(res, toAlertDto(result.rows[0]));
  } catch (error: any) {
    console.error(`[${reqId}] ❌ DB Error updating alert ${id}:`, error.message);
    return sendError(res, 'Failed to update alert status', 'DB_ERROR', 500);
  }
};
