import { Request, Response } from 'express';
import { TrafficService } from '../services/traffic.service';

export const reportTraffic = async (req: Request, res: Response) => {
  try {
    const event = await TrafficService.processTraffic(req.body);
    res.json(event);
  } catch (error) {
    console.error('Error processing traffic:', error);
    res.status(500).json({ error: 'Failed to process traffic report' });
  }
};
