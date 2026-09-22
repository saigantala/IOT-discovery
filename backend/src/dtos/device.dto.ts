export interface DeviceResponseDto {
  id: string;
  deviceId: string;
  name: string;
  type: string;
  ipAddress: string;
  status: string;
  discoverySource: string;
  manufacturer: string;
  hostname: string;
  os: string;
  riskLevel: string;
  riskScore: number;
  riskReason: string;
  openPorts: number[];
  services: string[];
  fingerprintConfidence: number;
  lastSeen: string;
  isQuarantined: boolean;
}

// Map valid Android DeviceType enum strings
export function mapDeviceTypeToEnum(typeString: string | null | undefined): string {
  if (!typeString) return 'UNKNOWN';
  const upper = typeString.toUpperCase().trim();
  const validTypes = [
    'ROUTER', 'SWITCH', 'ACCESS_POINT', 'CAMERA', 'PRINTER',
    'SPEAKER', 'THERMOSTAT', 'SMART_TV', 'SMART_BULB', 'GATEWAY',
    'SERVER', 'LAPTOP', 'MOBILE', 'UNKNOWN'
  ];

  if (validTypes.includes(upper)) return upper;

  // Common mapping fallbacks
  if (upper.includes('GATEWAY') || upper.includes('ROUTER') || upper.includes('INFRASTRUCTURE')) return 'ROUTER';
  if (upper.includes('CAM')) return 'CAMERA';
  if (upper.includes('PRINT')) return 'PRINTER';
  if (upper.includes('TV')) return 'SMART_TV';
  if (upper.includes('PHONE') || upper.includes('ANDROID') || upper.includes('IOS') || upper.includes('MOBILE')) return 'MOBILE';
  if (upper.includes('DESKTOP') || upper.includes('PC') || upper.includes('LAPTOP') || upper.includes('MAC')) return 'LAPTOP';
  if (upper.includes('SERVER')) return 'SERVER';

  return 'UNKNOWN';
}

export function toDeviceDto(row: any): DeviceResponseDto {
  let openPorts: number[] = [];
  let services: string[] = [];

  try {
    if (typeof row.open_ports === 'string') openPorts = JSON.parse(row.open_ports);
    else if (Array.isArray(row.open_ports)) openPorts = row.open_ports;
  } catch (e) { openPorts = []; }

  try {
    if (typeof row.services === 'string') services = JSON.parse(row.services);
    else if (Array.isArray(row.services)) services = row.services;
  } catch (e) { services = []; }

  return {
    id: row.id || row.device_id,
    deviceId: row.device_id || row.id,
    name: row.name || 'Unverified Device',
    type: mapDeviceTypeToEnum(row.type),
    ipAddress: row.ip_address || '0.0.0.0',
    status: (row.status || 'ONLINE').toUpperCase(),
    discoverySource: row.discovery_source || 'BACKEND_AUTO_DISCOVERY',
    manufacturer: row.manufacturer || 'Unknown Vendor',
    hostname: row.hostname || row.ip_address || 'Host',
    os: row.os || 'Embedded Network Stack',
    riskLevel: (row.risk_level || 'LOW').toUpperCase(),
    riskScore: Number(row.risk_score) || 0,
    riskReason: row.risk_reason || 'Subnet Verified',
    openPorts: openPorts,
    services: services,
    fingerprintConfidence: Number(row.fingerprint_confidence) || 90,
    lastSeen: row.last_seen ? new Date(row.last_seen).toISOString() : new Date().toISOString(),
    isQuarantined: Boolean(row.is_quarantined)
  };
}
