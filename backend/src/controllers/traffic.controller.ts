import { Request, Response } from 'express';
import { TrafficService } from '../services/traffic.service';
import { sendSuccess, sendError } from '../utils/responseEnvelope';
import { trafficReportSchema } from '../validators/device.validator';

export const reportTraffic = async (req: Request, res: Response) => {
  const reqId = req.requestId;

  const validation = trafficReportSchema.safeParse(req.body);
  if (!validation.success) {
    const err = validation.error.errors.map(e => e.message).join(', ');
    console.log(`[${reqId}] TRAFFIC Validation error: ${err}`);
    return sendError(res, err, 'VALIDATION_ERROR', 400);
  }

  console.log(`[${reqId}] TRAFFIC Report processing for device: ${validation.data.deviceId}`);

  try {
    const eventDto = await TrafficService.processTraffic(validation.data);
    console.log(`[${reqId}] DB Traffic event stored successfully [Event ID: ${eventDto.id}]`);
    return sendSuccess(res, eventDto);
  } catch (error: any) {
    console.error(`[${reqId}] ❌ DB Error processing traffic report:`, error.message);
    return sendError(res, 'Failed to process traffic report', 'DB_ERROR', 500);
  }
};
