import { Request, Response } from 'express';
import { sendSuccess } from '../utils/responseEnvelope';

export const logScreenView = async (req: Request, res: Response) => {
  const reqId = req.requestId;
  const clientIp = req.ip || req.socket.remoteAddress;
  const { screenName, route } = req.body;

  const displayName = screenName || route || 'Unknown Screen';

  console.log(`📱 [ANDROID APP NAVIGATION] User switched to Screen/Tab: "${displayName}" (${route || 'index'}) | Client IP: ${clientIp}`);

  // Note: As requested, navigation events are logged in the terminal but NOT saved to PostgreSQL.
  return sendSuccess(res, { logged: true, screenName: displayName });
};
