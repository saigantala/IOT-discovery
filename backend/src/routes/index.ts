import { Router } from 'express';
import authRoutes from './auth.routes';
import deviceRoutes from './device.routes';
import alertRoutes from './alert.routes';
import trafficRoutes from './traffic.routes';
import sessionRoutes from './session.routes';
import dashboardRoutes from './dashboard.routes';
import { authenticateToken } from '../middleware/auth.middleware';

const router = Router();

router.use('/auth', authRoutes);
router.use('/devices', authenticateToken, deviceRoutes);
router.use('/alerts', authenticateToken, alertRoutes);
router.use('/dashboard', authenticateToken, dashboardRoutes);
// Left open so simulator_client.py keeps working. Add a device API key before real use.
router.use('/traffic', trafficRoutes);
router.use('/sessions', sessionRoutes);

export default router;
