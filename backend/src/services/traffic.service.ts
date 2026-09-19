import { PrismaClient, AlertStatus, Severity } from '@prisma/client';
import { MLService } from './ml.service';
import { socketService } from './socket.service';

const prisma = new PrismaClient();

export class TrafficService {
  static async processTraffic(data: any) {
    const { device_id, packet_rate, dest_diversity, port_diversity, mqtt_freq, attack_type } = data;

    // 1. Get ML Prediction from sidecar
    const prediction = await MLService.predict({
      packet_rate, dest_diversity, port_diversity, mqtt_freq
    });

    // 2. Persist/Update Device
    const device = await prisma.device.upsert({
      where: { deviceId: device_id },
      update: { lastSeen: new Date(), ipAddress: data.ip_address || null },
      create: {
        deviceId: device_id,
        name: `Discovered Device ${device_id}`,
        type: 'IoT node',
        status: 'ONLINE'
      }
    });

    // 3. Save Traffic Event
    const event = await prisma.trafficEvent.create({
      data: {
        deviceId: device_id,
        packetRate: packet_rate,
        destDiversity: dest_diversity,
        portDiversity: port_diversity,
        mqttFreq: mqtt_freq,
        riskScore: prediction.risk_score,
        isAnomaly: prediction.is_anomaly
      }
    });

    // 4. Handle Alert & Push to Dashboard
    if (prediction.is_anomaly) {
      const alert = await prisma.alert.create({
        data: {
          deviceId: device_id,
          trafficEventId: event.id,
          riskScore: prediction.risk_score,
          attackType: attack_type || 'AI-Detected Anomaly',
          severity: prediction.risk_score > 0.8 ? Severity.CRITICAL : Severity.HIGH,
          status: AlertStatus.OPEN
        },
        include: { device: true }
      });

      socketService.emit('new_alert', alert);
    }

    socketService.emit('new_traffic', { ...event, deviceName: device.name });
    return event;
  }
}
