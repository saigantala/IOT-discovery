import { Request, Response, NextFunction } from 'express';
import jwt from 'jsonwebtoken';
import { sendError } from '../utils/responseEnvelope';

const JWT_ACCESS_SECRET = process.env.JWT_ACCESS_SECRET || 'your_super_secret_access_key';

export interface AuthenticatedRequest extends Request {
  user?: {
    id: string;
    email: string;
    role: string;
  };
}

export const authenticateToken = (req: AuthenticatedRequest, res: Response, next: NextFunction) => {
  const authHeader = req.headers.authorization;
  const token = authHeader && authHeader.startsWith('Bearer ') ? authHeader.split(' ')[1] : null;

  if (!token) {
    console.log(`[${req.requestId}] AUTH Access denied: No token provided`);
    return sendError(res, 'Access token is required', 'UNAUTHORIZED', 401);
  }

  try {
    const decoded = jwt.verify(token, JWT_ACCESS_SECRET) as any;
    req.user = decoded;
    next();
  } catch (err: any) {
    console.log(`[${req.requestId}] AUTH Access denied: Token expired or invalid (${err.message})`);
    return sendError(res, 'Invalid or expired access token', 'UNAUTHORIZED', 401);
  }
};
