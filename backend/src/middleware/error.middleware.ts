import { Request, Response, NextFunction } from 'express';
import { sendError } from '../utils/responseEnvelope';

export const errorHandler = (err: any, req: Request, res: Response, next: NextFunction) => {
  const requestId = req.requestId || 'req_unknown';
  console.error(`[${requestId}] ❌ Express Global Error:`, err);

  const status = err.status || err.statusCode || 500;
  const message = err.message || 'Internal Server Error';
  const code = err.code || 'INTERNAL_SERVER_ERROR';

  return sendError(res, message, code, status);
};
