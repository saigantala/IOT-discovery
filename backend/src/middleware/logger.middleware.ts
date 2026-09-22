import { Request, Response, NextFunction } from 'express';
import crypto from 'crypto';

// Extend Express Request interface to include requestId
declare global {
  namespace Express {
    interface Request {
      requestId: string;
    }
  }
}

export const detailedRequestLogger = (req: Request, res: Response, next: NextFunction) => {
  const startTime = Date.now();
  const timestamp = new Date().toISOString();

  // Generate a short 8-char unique request ID
  const requestId = `req_${crypto.randomBytes(4).toString('hex')}`;
  req.requestId = requestId;
  res.setHeader('X-Request-ID', requestId);

  const method = req.method;
  const url = req.originalUrl || req.url;
  const clientIp = req.ip || req.socket.remoteAddress || 'unknown';

  // Sanitize body so sensitive credentials are never printed
  let safeBody = { ...req.body };
  if (safeBody.password) safeBody.password = '***MASKED***';
  if (safeBody.password_hash) safeBody.password_hash = '***MASKED***';
  if (safeBody.token) safeBody.token = '***MASKED***';
  if (safeBody.accessToken) safeBody.accessToken = '***MASKED***';
  if (safeBody.refreshToken) safeBody.refreshToken = '***MASKED***';

  const bodySummary = Object.keys(safeBody).length > 0 ? JSON.stringify(safeBody) : '';

  console.log(`\n[${timestamp}] [${requestId}] IN  ${method} ${url} from ${clientIp} ${bodySummary ? '| Body: ' + bodySummary : ''}`);

  res.on('finish', () => {
    const duration = Date.now() - startTime;
    const status = res.statusCode;
    const statusSymbol = status >= 400 ? '❌' : '✅';
    console.log(`[${requestId}] OUT ${status} ${method} ${url} ${duration}ms ${statusSymbol}`);
  });

  next();
};
