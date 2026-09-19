import find from 'local-devices';
import { PrismaClient } from '@prisma/client';
import { socketService } from './socket.service';

const prisma = new PrismaClient();

export class DiscoveryService {
  static async scanNetwork() {
    console.log('🔍 Starting Real-Time Network Discovery...');

    try {
      const devices = await find();
      console.log(`✅ Scan complete. Found ${devices.length} real devices on network.`);

      for (const d of devices) {
        const deviceId = d.mac.toUpperCase();

        const updatedDevice = await prisma.device.upsert({
          where: { deviceId: deviceId },
          update: {
            ipAddress: d.ip,
            name: d.name !== '?' ? d.name : `Unknown Device (${d.ip})`,
            lastSeen: new Date(),
            status: 'ONLINE'
          },
          create: {
            deviceId: deviceId,
            name: d.name !== '?' ? d.name : `New Asset (${d.ip})`,
            ipAddress: d.ip,
            type: 'Hardware Node',
            status: 'ONLINE',
            isQuarantined: false
          }
        });

        socketService.emit('device_discovered', updatedDevice);
      }
    } catch (error) {
      console.error('Error during network discovery:', error);
    }
  }

  static startAutoDiscovery() {
    // Initial scan
    this.scanNetwork();

    // Repeat every 2 minutes
    setInterval(() => {
      this.scanNetwork();
    }, 120000);
  }
}
