import { Router } from 'express';
import { logSession } from '../controllers/session.controller';

const router = Router();

router.post('/', logSession);

export default router;
