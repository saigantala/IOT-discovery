import { Router } from 'express';
import { getDevices, getDeviceById, syncDevices } from '../controllers/device.controller';

const router = Router();

router.get('/', getDevices);
router.get('/:id', getDeviceById);
router.post('/sync', syncDevices);

export default router;
