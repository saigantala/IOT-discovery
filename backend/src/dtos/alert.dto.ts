export interface AlertResponseDto {
  id: string;
  deviceId: string;
  deviceName: string;
  trafficEventId: string | null;
  riskScore: number;
  attackType: string;
  severity: string;
  status: string;
  createdAt: string;
}

export function toAlertDto(row: any): AlertResponseDto {
  return {
    id: row.id,
    deviceId: row.device_id,
    deviceName: row.device_name || row.device_id || 'Device',
    trafficEventId: row.traffic_event_id || null,
    riskScore: Number(row.risk_score) || 0,
    attackType: row.attack_type || 'Unspecified Threat',
    severity: (row.severity || 'MEDIUM').toUpperCase(),
    status: (row.status || 'OPEN').toUpperCase(),
    createdAt: row.created_at ? new Date(row.created_at).toISOString() : new Date().toISOString()
  };
}
