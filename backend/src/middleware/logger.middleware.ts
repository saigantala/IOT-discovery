import { Request, Response, NextFunction } from 'express';

export const detailedRequestLogger = (req: Request, res: Response, next: NextFunction) => {
  const startTime = Date.now();
  const timestamp = new Date().toISOString();
  const method = req.method;
  const url = req.originalUrl || req.url;
  const clientIp = req.ip || req.socket.remoteAddress || 'unknown';

  // Sanitize request body to prevent logging sensitive passwords/tokens
  let safeBody = { ...req.body };
  if (safeBody.password) safeBody.password = '***MASKED***';
  if (safeBody.password_hash) safeBody.password_hash = '***MASKED***';
  if (safeBody.token) safeBody.token = '***MASKED***';
  if (safeBody.refreshToken) safeBody.refreshToken = '***MASKED***';

  const bodyString = Object.keys(safeBody).length > 0 ? JSON.stringify(safeBody) : '{}';

  console.log(`\n📥 [${timestamp}] Incoming Request: ${method} ${url} | IP: ${clientIp} | Body: ${bodyString}`);

  // Capture response status and duration when finished
  res.on('finish', () => {
    const duration = Date.now() - startTime;
    const status = res.statusCode;
    const statusSymbol = status >= 400 ? '❌' : '✅';
    console.log(`📤 [${new Date().toISOString()}] Response ${statusSymbol}: ${method} ${url} | Status: ${status} | Duration: ${duration}ms`);
  });

  next();
};
