import { Router } from 'express';
import authRoutes from './auth.routes';
import deviceRoutes from './device.routes';
import alertRoutes from './alert.routes';
import trafficRoutes from './traffic.routes';
import sessionRoutes from './session.routes';
import dashboardRoutes from './dashboard.routes';
import telemetryRoutes from './telemetry.routes';
import { authenticateToken } from '../middleware/auth.middleware';
import { syncDevices } from '../controllers/device.controller';

const router = Router();

router.use('/auth', authRoutes);
// Allow mobile scanner to sync discovered devices over LAN
router.post('/devices/sync', syncDevices);
router.use('/devices', authenticateToken, deviceRoutes);
router.use('/alerts', authenticateToken, alertRoutes);
router.use('/dashboard', authenticateToken, dashboardRoutes);
// Left open so simulator_client.py keeps working
router.use('/traffic', trafficRoutes);
router.use('/sessions', sessionRoutes);
// App navigation telemetry logging
router.use('/telemetry', telemetryRoutes);

export default router;
