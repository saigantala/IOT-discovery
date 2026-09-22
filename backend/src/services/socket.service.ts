import { Server } from 'socket.io';
import { Server as HttpServer } from 'http';

export class SocketService {
  private io: Server | null = null;

  init(server: HttpServer) {
    this.io = new Server(server, {
      cors: {
        origin: '*',
        methods: ['GET', 'POST']
      }
    });

    this.io.on('connection', (socket) => {
      console.log('📱 App connected via WebSocket:', socket.id);

      socket.on('disconnect', () => {
        console.log('📱 App disconnected');
      });
    });

    console.log('🔌 Socket.io Service Initialized');
  }

  emit(event: string, data: any) {
    if (this.io) {
      this.io.emit(event, data);
      console.log(`📡 WebSocket Emit [${event}]:`, data.deviceId || data.id || 'system');
    }
  }
}

export const socketService = new SocketService();
