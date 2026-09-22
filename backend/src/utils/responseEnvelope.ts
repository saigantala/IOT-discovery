import { Response, Request } from 'express';

export interface ApiSuccessResponse<T = any> {
  success: true;
  data: T;
  requestId: string;
}

export interface ApiErrorResponse {
  success: false;
  error: {
    code: string;
    message: string;
    details?: any;
  };
  requestId: string;
}

export function sendSuccess<T>(res: Response, data: T, statusCode = 200) {
  const req = res.req as Request & { requestId?: string };
  const requestId = req?.requestId || 'req_unknown';
  return res.status(statusCode).json({
    success: true,
    data,
    requestId
  });
}

export function sendError(res: Response, message: string, code = 'API_ERROR', statusCode = 400, details?: any) {
  const req = res.req as Request & { requestId?: string };
  const requestId = req?.requestId || 'req_unknown';
  return res.status(statusCode).json({
    success: false,
    error: {
      code,
      message,
      ...(details && { details })
    },
    requestId
  });
}
