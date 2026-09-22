import { Request, Response } from 'express';
import bcrypt from 'bcrypt';
import jwt from 'jsonwebtoken';
import crypto from 'crypto';
import { pool } from '../config/db';
import { toUserDto } from '../dtos/auth.dto';
import { sendSuccess, sendError } from '../utils/responseEnvelope';
import { registerSchema, loginSchema, refreshSchema } from '../validators/auth.validator';
import { AuthenticatedRequest } from '../middleware/auth.middleware';

const JWT_ACCESS_SECRET = process.env.JWT_ACCESS_SECRET || 'your_super_secret_access_key';
const JWT_REFRESH_SECRET = process.env.JWT_REFRESH_SECRET || 'your_super_secret_refresh_key';

export const register = async (req: Request, res: Response) => {
  const reqId = req.requestId;

  // Zod Validation
  const validation = registerSchema.safeParse(req.body);
  if (!validation.success) {
    const errorMsg = validation.error.errors.map(e => e.message).join(', ');
    console.log(`[${reqId}] AUTH Registration validation failed: ${errorMsg}`);
    return sendError(res, errorMsg, 'VALIDATION_ERROR', 400);
  }

  const { name, email, password } = validation.data;
  console.log(`[${reqId}] AUTH Registration requested for email: ${email}`);

  try {
    const existing = await pool.query('SELECT id FROM users WHERE email = $1', [email]);
    if (existing.rows.length > 0) {
      console.log(`[${reqId}] AUTH Registration failed: Email ${email} already exists`);
      return sendError(res, 'User with this email already exists', 'USER_EXISTS', 400);
    }

    const salt = await bcrypt.genSalt(10);
    const passwordHash = await bcrypt.hash(password, salt);

    const result = await pool.query(
      `INSERT INTO users (name, email, password_hash, role)
       VALUES ($1, $2, $3, $4) RETURNING id, name, email, role`,
      [name, email, passwordHash, 'VIEWER']
    );

    const user = result.rows[0];
    const userDto = toUserDto(user);

    const accessToken = jwt.sign({ id: user.id, email: user.email, role: user.role }, JWT_ACCESS_SECRET, { expiresIn: '15m' });
    const refreshToken = jwt.sign({ id: user.id }, JWT_REFRESH_SECRET, { expiresIn: '7d' });

    // Store refresh token hash in DB
    const refreshTokenHash = crypto.createHash('sha256').update(refreshToken).digest('hex');
    const expiresAt = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000);
    await pool.query(
      `INSERT INTO refresh_tokens (user_id, token_hash, expires_at) VALUES ($1, $2, $3)`,
      [user.id, refreshTokenHash, expiresAt]
    );

    console.log(`[${reqId}] DB User created successfully in PostgreSQL [ID: ${user.id}]`);
    console.log(`[${reqId}] AUTH Registration succeeded for user: ${email}`);

    return sendSuccess(res, {
      accessToken,
      refreshToken,
      user: userDto
    }, 201);
  } catch (error: any) {
    console.error(`[${reqId}] ❌ AUTH Registration error:`, error.message);
    return sendError(res, 'Server error during registration', 'SERVER_ERROR', 500);
  }
};

export const login = async (req: Request, res: Response) => {
  const reqId = req.requestId;

  // Zod Validation
  const validation = loginSchema.safeParse(req.body);
  if (!validation.success) {
    const errorMsg = validation.error.errors.map(e => e.message).join(', ');
    console.log(`[${reqId}] AUTH Login validation failed: ${errorMsg}`);
    return sendError(res, errorMsg, 'VALIDATION_ERROR', 400);
  }

  const { email, password } = validation.data;
  console.log(`[${reqId}] AUTH Login requested for email: ${email}`);

  try {
    const result = await pool.query('SELECT * FROM users WHERE email = $1', [email]);
    console.log(`[${reqId}] DB User lookup executed for email: ${email}`);

    if (result.rows.length === 0) {
      console.log(`[${reqId}] AUTH Login failed: User email not found`);
      return sendError(res, 'Invalid email or password', 'INVALID_CREDENTIALS', 401);
    }

    const user = result.rows[0];
    const isMatch = await bcrypt.compare(password, user.password_hash);

    if (!isMatch) {
      console.log(`[${reqId}] AUTH Login failed: Password mismatch for user ID: ${user.id}`);
      return sendError(res, 'Invalid email or password', 'INVALID_CREDENTIALS', 401);
    }

    const userDto = toUserDto(user);
    const accessToken = jwt.sign({ id: user.id, email: user.email, role: user.role }, JWT_ACCESS_SECRET, { expiresIn: '15m' });
    const refreshToken = jwt.sign({ id: user.id }, JWT_REFRESH_SECRET, { expiresIn: '7d' });

    // Store refresh token hash in DB
    const refreshTokenHash = crypto.createHash('sha256').update(refreshToken).digest('hex');
    const expiresAt = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000);
    await pool.query(
      `INSERT INTO refresh_tokens (user_id, token_hash, expires_at) VALUES ($1, $2, $3)`,
      [user.id, refreshTokenHash, expiresAt]
    );

    console.log(`[${reqId}] AUTH Login succeeded for user ID: ${user.id}`);

    return sendSuccess(res, {
      accessToken,
      refreshToken,
      user: userDto
    });
  } catch (error: any) {
    console.error(`[${reqId}] ❌ AUTH Login error:`, error.message);
    return sendError(res, 'Server error during login', 'SERVER_ERROR', 500);
  }
};

export const refresh = async (req: Request, res: Response) => {
  const reqId = req.requestId;

  const validation = refreshSchema.safeParse(req.body);
  if (!validation.success) {
    return sendError(res, 'Refresh token is required', 'VALIDATION_ERROR', 400);
  }

  const { refreshToken } = validation.data;
  console.log(`[${reqId}] AUTH Token refresh requested`);

  try {
    const decoded = jwt.verify(refreshToken, JWT_REFRESH_SECRET) as any;
    const tokenHash = crypto.createHash('sha256').update(refreshToken).digest('hex');

    const tokenCheck = await pool.query(
      `SELECT * FROM refresh_tokens WHERE token_hash = $1 AND revoked = false AND expires_at > CURRENT_TIMESTAMP`,
      [tokenHash]
    );

    if (tokenCheck.rows.length === 0) {
      console.log(`[${reqId}] AUTH Token refresh failed: Token is revoked or not in DB`);
      return sendError(res, 'Invalid or expired refresh token', 'UNAUTHORIZED', 401);
    }

    const result = await pool.query('SELECT id, name, email, role FROM users WHERE id = $1', [decoded.id]);
    if (result.rows.length === 0) {
      return sendError(res, 'User not found', 'UNAUTHORIZED', 401);
    }

    const user = result.rows[0];
    const newAccessToken = jwt.sign({ id: user.id, email: user.email, role: user.role }, JWT_ACCESS_SECRET, { expiresIn: '15m' });
    const newRefreshToken = jwt.sign({ id: user.id }, JWT_REFRESH_SECRET, { expiresIn: '7d' });

    // Revoke old token and save new token
    await pool.query('UPDATE refresh_tokens SET revoked = true WHERE token_hash = $1', [tokenHash]);
    const newHash = crypto.createHash('sha256').update(newRefreshToken).digest('hex');
    const expiresAt = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000);
    await pool.query(
      `INSERT INTO refresh_tokens (user_id, token_hash, expires_at) VALUES ($1, $2, $3)`,
      [user.id, newHash, expiresAt]
    );

    console.log(`[${reqId}] AUTH Token refresh succeeded for user ID: ${user.id}`);

    return sendSuccess(res, {
      accessToken: newAccessToken,
      refreshToken: newRefreshToken,
      user: toUserDto(user)
    });
  } catch (error: any) {
    console.log(`[${reqId}] AUTH Token refresh error: ${error.message}`);
    return sendError(res, 'Invalid or expired refresh token', 'UNAUTHORIZED', 401);
  }
};

export const logout = async (req: Request, res: Response) => {
  const reqId = req.requestId;
  const { refreshToken } = req.body;

  if (refreshToken) {
    const tokenHash = crypto.createHash('sha256').update(refreshToken).digest('hex');
    await pool.query('UPDATE refresh_tokens SET revoked = true WHERE token_hash = $1', [tokenHash]);
  }

  console.log(`[${reqId}] AUTH User logged out successfully`);
  return sendSuccess(res, { message: 'Logged out successfully' });
};

export const getMe = async (req: AuthenticatedRequest, res: Response) => {
  const reqId = req.requestId;
  const userId = req.user?.id;

  if (!userId) {
    return sendError(res, 'Unauthorized', 'UNAUTHORIZED', 401);
  }

  try {
    const result = await pool.query('SELECT id, name, email, role FROM users WHERE id = $1', [userId]);
    if (result.rows.length === 0) {
      return sendError(res, 'User profile not found', 'NOT_FOUND', 404);
    }

    console.log(`[${reqId}] AUTH Profile retrieved for user ID: ${userId}`);
    return sendSuccess(res, toUserDto(result.rows[0]));
  } catch (error: any) {
    console.error(`[${reqId}] ❌ AUTH Profile error:`, error.message);
    return sendError(res, 'Failed to fetch user profile', 'SERVER_ERROR', 500);
  }
};
