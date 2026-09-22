import { Request, Response } from 'express';
import bcrypt from 'bcrypt';
import jwt from 'jsonwebtoken';
import { pool } from '../config/db';
import { toUserDto } from '../dtos/auth.dto';

const JWT_ACCESS_SECRET = process.env.JWT_ACCESS_SECRET || 'your_super_secret_access_key';
const JWT_REFRESH_SECRET = process.env.JWT_REFRESH_SECRET || 'your_super_secret_refresh_key';

export const register = async (req: Request, res: Response) => {
  const { name, email, password } = req.body;
  const clientIp = req.ip || req.socket.remoteAddress;

  console.log(`🔑 [AUTH REGISTER] Registration attempt for email: ${email} from IP: ${clientIp}`);

  if (!name || !email || !password) {
    return res.status(400).json({ success: false, message: 'Name, email, and password are required' });
  }

  try {
    const existing = await pool.query('SELECT id FROM users WHERE email = $1', [email]);
    if (existing.rows.length > 0) {
      return res.status(400).json({ success: false, message: 'User with this email already exists' });
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

    console.log(`✅ [AUTH REGISTER] Successfully registered user: ${email} [ID: ${user.id}]`);

    return res.status(201).json({
      success: true,
      accessToken,
      refreshToken,
      user: userDto
    });
  } catch (error) {
    console.error('❌ [AUTH REGISTER] Registration error:', error);
    return res.status(500).json({ success: false, message: 'Server error during registration' });
  }
};

export const login = async (req: Request, res: Response) => {
  const { email, password } = req.body;
  const clientIp = req.ip || req.socket.remoteAddress;

  console.log(`🔑 [AUTH LOGIN] Login attempt for email: ${email} from IP: ${clientIp}`);

  if (!email || !password) {
    return res.status(400).json({ success: false, message: 'Email and password are required' });
  }

  try {
    const result = await pool.query('SELECT * FROM users WHERE email = $1', [email]);
    if (result.rows.length === 0) {
      return res.status(401).json({ success: false, message: 'Invalid credentials' });
    }

    const user = result.rows[0];
    const isMatch = await bcrypt.compare(password, user.password_hash);

    if (!isMatch) {
      return res.status(401).json({ success: false, message: 'Invalid credentials' });
    }

    const userDto = toUserDto(user);
    const accessToken = jwt.sign({ id: user.id, email: user.email, role: user.role }, JWT_ACCESS_SECRET, { expiresIn: '15m' });
    const refreshToken = jwt.sign({ id: user.id }, JWT_REFRESH_SECRET, { expiresIn: '7d' });

    console.log(`✅ [AUTH LOGIN] Successful login for user: ${email} [ID: ${user.id}]`);

    return res.json({
      success: true,
      accessToken,
      refreshToken,
      user: userDto
    });
  } catch (error) {
    console.error('❌ [AUTH LOGIN] Login error:', error);
    return res.status(500).json({ success: false, message: 'Server error during login' });
  }
};

export const refresh = async (req: Request, res: Response) => {
  const { refreshToken } = req.body;
  if (!refreshToken) {
    return res.status(400).json({ success: false, message: 'Refresh token is required' });
  }

  try {
    const decoded = jwt.verify(refreshToken, JWT_REFRESH_SECRET) as any;
    const result = await pool.query('SELECT * FROM users WHERE id = $1', [decoded.id]);

    if (result.rows.length === 0) {
      return res.status(401).json({ success: false, message: 'User not found' });
    }

    const user = result.rows[0];
    const newAccessToken = jwt.sign({ id: user.id, email: user.email, role: user.role }, JWT_ACCESS_SECRET, { expiresIn: '15m' });
    const newRefreshToken = jwt.sign({ id: user.id }, JWT_REFRESH_SECRET, { expiresIn: '7d' });

    return res.json({
      success: true,
      accessToken: newAccessToken,
      refreshToken: newRefreshToken,
      user: toUserDto(user)
    });
  } catch (error) {
    return res.status(401).json({ success: false, message: 'Invalid or expired refresh token' });
  }
};

export const getMe = async (req: Request, res: Response) => {
  // Simple token payload or user fetch
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ success: false, message: 'Unauthorized' });
  }

  const token = authHeader.split(' ')[1];
  try {
    const decoded = jwt.verify(token, JWT_ACCESS_SECRET) as any;
    const result = await pool.query('SELECT id, name, email, role FROM users WHERE id = $1', [decoded.id]);
    if (result.rows.length === 0) return res.status(404).json({ success: false, message: 'User not found' });

    return res.json({
      success: true,
      data: toUserDto(result.rows[0])
    });
  } catch (error) {
    return res.status(401).json({ success: false, message: 'Invalid or expired access token' });
  }
};
