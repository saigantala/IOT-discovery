import { Request, Response } from 'express';
import { pool } from '../config/db';
import { DashboardSummaryDto } from '../dtos/dashboard.dto';
import { sendSuccess, sendError } from '../utils/responseEnvelope';

export const getDashboardSummary = async (req: Request, res: Response) => {
  const reqId = req.requestId;
  console.log(`[${reqId}] DASHBOARD GET /api/v1/dashboard/summary requested`);

  try {
    const devicesRes = await pool.query('SELECT COUNT(*) FROM devices');
    const alertsRes = await pool.query("SELECT COUNT(*) FROM alerts WHERE status = 'OPEN'");
    const quarantinedRes = await pool.query('SELECT COUNT(*) FROM devices WHERE is_quarantined = true');
    const onlineRes = await pool.query("SELECT COUNT(*) FROM devices WHERE status = 'ONLINE'");

    const summary: DashboardSummaryDto = {
      deviceCount: parseInt(devicesRes.rows[0].count, 10) || 0,
      alertCount: parseInt(alertsRes.rows[0].count, 10) || 0,
      quarantinedCount: parseInt(quarantinedRes.rows[0].count, 10) || 0,
      onlineCount: parseInt(onlineRes.rows[0].count, 10) || 0
    };

    console.log(`[${reqId}] DB Dashboard summary calculated: ${summary.deviceCount} devices, ${summary.alertCount} open alerts`);
    return sendSuccess(res, summary);
  } catch (error: any) {
    console.error(`[${reqId}] ❌ DB Error fetching dashboard summary:`, error.message);
    return sendError(res, 'Failed to fetch dashboard summary', 'DB_ERROR', 500);
  }
};
