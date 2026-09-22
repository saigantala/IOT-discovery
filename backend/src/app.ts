import express from 'express';
import http from 'http';
import cors from 'cors';
import helmet from 'helmet';
import morgan from 'morgan';
import dotenv from 'dotenv';
import { pool } from './config/db';
import routes from './routes';
import { errorHandler } from './middleware/error.middleware';
import { detailedRequestLogger } from './middleware/logger.middleware';
import { socketService } from './services/socket.service';
import { mqttService } from './services/mqtt.service';
import { DiscoveryService } from './services/discovery.service';

dotenv.config();

const app = express();
const server = http.createServer(app);

// 1. Core Security & Parsing Middleware
app.use(helmet());
app.use(cors({ origin: '*', credentials: true }));
app.use(morgan('dev')); // Keep morgan logging as requested
app.use(express.json());

// 2. Custom Detailed Request Logging Middleware (Requirement 4)
app.use(detailedRequestLogger);

// 3. Real-time & Network Services
socketService.init(server);
mqttService.connect();
DiscoveryService.startAutoDiscovery();

// 4. Health Check Endpoint (Requirement 11)
app.get('/health', async (req, res) => {
  const startTime = Date.now();
  try {
    const dbResult = await pool.query('SELECT current_database(), now() as current_time');
    const latencyMs = Date.now() - startTime;

    return res.json({
      status: 'OK',
      server: 'online',
      timestamp: new Date().toISOString(),
      database: {
        status: 'connected',
        name: dbResult.rows[0].current_database,
        time: dbResult.rows[0].current_time,
        latencyMs
      }
    });
  } catch (e: any) {
    return res.status(500).json({
      status: 'ERROR',
      server: 'online',
      timestamp: new Date().toISOString(),
      database: {
        status: 'disconnected',
        error: e.message
      }
    });
  }
});

// 5. API Routes
app.use('/api/v1', routes);

// 6. Global Error Handling
app.use(errorHandler);

const PORT = Number(process.env.PORT) || 4000;
server.listen(PORT, '0.0.0.0', () => {
  console.log(`\n🚀 Backend running on http://0.0.0.0:${PORT}`);
  console.log(`🏥 Health check endpoint available at http://0.0.0.0:${PORT}/health`);
});

export { app, server };
