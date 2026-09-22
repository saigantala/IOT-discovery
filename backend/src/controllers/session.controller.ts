import { Request, Response } from 'express';
import { TrafficService } from '../services/traffic.service';
import { sendSuccess, sendError } from '../utils/responseEnvelope';
import { sessionLogSchema } from '../validators/device.validator';

export const logSession = async (req: Request, res: Response) => {
  const reqId = req.requestId;

  const validation = sessionLogSchema.safeParse(req.body);
  if (!validation.success) {
    const err = validation.error.errors.map(e => e.message).join(', ');
    console.log(`[${reqId}] SESSIONS Validation error: ${err}`);
    return sendError(res, err, 'VALIDATION_ERROR', 400);
  }

  console.log(`[${reqId}] SESSIONS Logging network session for device: ${validation.data.deviceId}`);

  try {
    const session = await TrafficService.logSession(validation.data);
    console.log(`[${reqId}] DB Session logged successfully [Session ID: ${session.id}]`);
    return sendSuccess(res, session, 201);
  } catch (error: any) {
    console.error(`[${reqId}] ❌ DB Error logging session:`, error.message);
    return sendError(res, 'Failed to log network session', 'DB_ERROR', 500);
  }
};
