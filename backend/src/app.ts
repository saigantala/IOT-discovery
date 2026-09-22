import express from 'express';
import http from 'http';
import cors from 'cors';
import helmet from 'helmet';
import morgan from 'morgan';
import dotenv from 'dotenv';
import { pool } from './config/db';
import routes from './routes';
import { errorHandler } from './middleware/error.middleware';
import { socketService } from './services/socket.service';
import { mqttService } from './services/mqtt.service';
import { DiscoveryService } from './services/discovery.service';

dotenv.config();

const app = express();
const server = http.createServer(app);

// 1. Middleware
app.use(helmet());
app.use(cors({ origin: '*', credentials: true }));
app.use(morgan('dev'));
app.use(express.json());

// 2. Real-time & Network Services
socketService.init(server);
mqttService.connect();
DiscoveryService.startAutoDiscovery();

// 3. Health Check
app.get('/health', async (req, res) => {
  try {
    await pool.query('SELECT 1');
    res.json({ status: 'OK', database: 'connected' });
  } catch (e) {
    res.status(500).json({ status: 'ERROR', database: 'disconnected' });
  }
});

// 4. Routes
app.use('/api/v1', routes);

// 5. Error Handling
app.use(errorHandler);

const PORT = Number(process.env.PORT) || 4000;
server.listen(PORT, "0.0.0.0", () => {
  console.log(`Backend running on http://0.0.0.0:${PORT}`);
});

export { app, server };
