import { Router } from 'express';
import { getDevices, getDeviceById, syncDevices, quarantineDevice, unquarantineDevice } from '../controllers/device.controller';

const router = Router();

router.get('/', getDevices);
router.get('/:id', getDeviceById);
router.post('/sync', syncDevices);
router.post('/:id/quarantine', quarantineDevice);
router.post('/:id/unquarantine', unquarantineDevice);

export default router;
