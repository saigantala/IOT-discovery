import { Request, Response } from 'express';
import { TrafficService } from '../services/traffic.service';

export const logSession = async (req: Request, res: Response) => {
  try {
    const session = await TrafficService.logSession(req.body);
    res.status(201).json(session);
  } catch (error) {
    console.error('Error logging session:', error);
    res.status(500).json({ error: 'Failed to log network session' });
  }
};
