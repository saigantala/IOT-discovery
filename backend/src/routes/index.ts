import { Router } from 'express';
import authRoutes from './auth.routes';
import deviceRoutes from './device.routes';
import alertRoutes from './alert.routes';
import trafficRoutes from './traffic.routes';
import sessionRoutes from './session.routes';
import dashboardRoutes from './dashboard.routes';

const router = Router();

router.use('/auth', authRoutes);
router.use('/devices', deviceRoutes);
router.use('/alerts', alertRoutes);
router.use('/traffic', trafficRoutes);
router.use('/sessions', sessionRoutes);
router.use('/dashboard', dashboardRoutes);

export default router;
