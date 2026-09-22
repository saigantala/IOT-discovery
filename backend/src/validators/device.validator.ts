import { z } from 'zod';

export const syncDeviceItemSchema = z.object({
  deviceId: z.string().min(1, 'deviceId is required'),
  name: z.string().optional(),
  type: z.string().optional(),
  ipAddress: z.string().optional(),
  status: z.string().optional()
});

export const syncDevicesSchema = z.object({
  devices: z.array(syncDeviceItemSchema)
});

export const trafficReportSchema = z.object({
  deviceId: z.string().min(1, 'deviceId is required'),
  packetRate: z.number().optional().default(0),
  destDiversity: z.number().optional().default(0),
  portDiversity: z.number().optional().default(0),
  mqttFreq: z.number().optional().default(0),
  isSimulatedAttack: z.boolean().optional().default(false),
  attackType: z.string().optional().nullable()
});

export const sessionLogSchema = z.object({
  deviceId: z.string().min(1, 'deviceId is required'),
  protocol: z.string().optional().default('TCP'),
  sourcePort: z.number().optional().default(0),
  destPort: z.number().optional().default(0),
  bytesSent: z.number().optional().default(0),
  bytesReceived: z.number().optional().default(0),
  status: z.string().optional().default('ACTIVE')
});
