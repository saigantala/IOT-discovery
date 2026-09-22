import { Router } from 'express';
import { getAlerts, updateAlertStatus } from '../controllers/alert.controller';

const router = Router();

router.get('/', getAlerts);
router.patch('/:id', updateAlertStatus);

export default router;
