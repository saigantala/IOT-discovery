import { Request, Response } from 'express';
import { TrafficService } from '../services/traffic.service';

export const reportTraffic = async (req: Request, res: Response) => {
  const clientIp = req.ip || req.socket.remoteAddress;
  console.log(`📱 [ANDROID/CLIENT TRAFFIC] Received POST /api/v1/traffic/report from client IP: ${clientIp}`);
  try {
    const eventDto = await TrafficService.processTraffic(req.body);
    console.log(`✅ [ANDROID/CLIENT TRAFFIC] Traffic processed for device: ${req.body.device_id || req.body.deviceId}`);
    res.json(eventDto);
  } catch (error) {
    console.error(`❌ [ANDROID/CLIENT TRAFFIC] Error processing traffic report from client ${clientIp}:`, error);
    res.status(500).json({ error: 'Failed to process traffic report' });
  }
};
