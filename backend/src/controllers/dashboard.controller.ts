import { Request, Response } from 'express';
import { pool } from '../config/db';
import { DashboardSummaryDto } from '../dtos/dashboard.dto';

export const getDashboardSummary = async (req: Request, res: Response) => {
  const clientIp = req.ip || req.socket.remoteAddress;
  console.log(`📱 [ANDROID CLIENT REQUEST] GET /api/v1/dashboard/summary from IP: ${clientIp}`);

  try {
    const devicesRes = await pool.query('SELECT COUNT(*) FROM devices');
    const alertsRes = await pool.query('SELECT COUNT(*) FROM alerts WHERE status = $1', ['OPEN']);
    const quarantinedRes = await pool.query('SELECT COUNT(*) FROM devices WHERE is_quarantined = true');
    const onlineRes = await pool.query('SELECT COUNT(*) FROM devices WHERE status = $1', ['ONLINE']);

    const summary: DashboardSummaryDto = {
      deviceCount: parseInt(devicesRes.rows[0].count, 10) || 0,
      alertCount: parseInt(alertsRes.rows[0].count, 10) || 0,
      quarantinedCount: parseInt(quarantinedRes.rows[0].count, 10) || 0,
      onlineCount: parseInt(onlineRes.rows[0].count, 10) || 0
    };

    console.log(`📊 [DASHBOARD SUMMARY] Returned: ${summary.deviceCount} devices, ${summary.alertCount} alerts, ${summary.quarantinedCount} quarantined.`);
    return res.json(summary);
  } catch (error) {
    console.error('❌ [DASHBOARD SUMMARY] Error fetching summary:', error);
    return res.status(500).json({ error: 'Failed to fetch dashboard summary' });
  }
};
