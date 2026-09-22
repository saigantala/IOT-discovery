import { Router } from 'express';
import { reportTraffic } from '../controllers/traffic.controller';

const router = Router();

router.post('/report', reportTraffic);

export default router;
