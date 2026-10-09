import { Router } from 'express';
import { logScreenView } from '../controllers/telemetry.controller';

const router = Router();

router.post('/screen', logScreenView);

export default router;
