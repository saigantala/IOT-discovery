import { Router } from 'express';
import sessionRoutes from './session.routes';
import deviceRoutes from './device.routes';
import alertRoutes from './alert.routes';
import trafficRoutes from './traffic.routes';

const router = Router();

router.use('/sessions', sessionRoutes);
router.use('/devices', deviceRoutes);
router.use('/alerts', alertRoutes);
router.use('/traffic', trafficRoutes);

export default router;
