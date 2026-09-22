export interface TrafficResponseDto {
  id: string;
  deviceId: string;
  packetRate: number;
  destDiversity: number;
  portDiversity: number;
  mqttFreq: number;
  riskScore: number;
  isAnomaly: boolean;
  createdAt: string;
}

export function toTrafficDto(row: any): TrafficResponseDto {
  return {
    id: row.id,
    deviceId: row.device_id,
    packetRate: Number(row.packet_rate) || 0,
    destDiversity: Number(row.dest_diversity) || 0,
    portDiversity: Number(row.port_diversity) || 0,
    mqttFreq: Number(row.mqtt_freq) || 0,
    riskScore: Number(row.risk_score) || 0,
    isAnomaly: Boolean(row.is_anomaly),
    createdAt: row.created_at ? new Date(row.created_at).toISOString() : new Date().toISOString()
  };
}
