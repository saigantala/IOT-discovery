# IoT Discovery & Threat Shield - Technical Documentation

## 1. System Overview & Architecture

SecureIoT Shield is an enterprise IoT security platform that performs active subnet sweeps, anomaly detection, and device quarantine across physical network environments.

### Data Flow & Source of Truth Strategy
```
[Android UI Screen]
       ↑ (Observes StateFlow / Flow)
[Room Local Database]  <-- Caches DTOs as Entities
       ↑
[DataRepository]
       ↑ (Retrofit HTTP / JSON DTOs)
[Node.js Express REST API (0.0.0.0:4000)]
       ↑ (Parameterized SQL Queries)
[PostgreSQL Database (pgAdmin 4)]
```

---

## 2. API Endpoints & DTO Formats

### Health Check
- `GET /health`
- **Response**:
```json
{
  "status": "OK",
  "server": "online",
  "timestamp": "2026-09-22T10:00:00.000Z",
  "database": {
    "status": "connected",
    "name": "secureiot",
    "latencyMs": 4
  }
}
```

### Authentication
- `POST /api/v1/auth/register` (Body: `name`, `email`, `password`)
- `POST /api/v1/auth/login` (Body: `email`, `password`)
- `POST /api/v1/auth/refresh` (Body: `refreshToken`)
- `GET /api/v1/auth/me` (Header: `Authorization: Bearer <token>`)

### Devices
- `GET /api/v1/devices` -> Returns `DeviceResponseDto[]`
- `GET /api/v1/devices/:id` -> Returns `DeviceResponseDto`
- `POST /api/v1/devices/sync` -> Body: `{ "devices": [ { "deviceId": "...", "name": "...", "type": "CAMERA", "ipAddress": "...", "status": "ONLINE" } ] }`
- `POST /api/v1/devices/:id/quarantine` -> Updates status to `BLOCKED` and `is_quarantined = true`.

### Dashboard & Threat Detection
- `GET /api/v1/dashboard/summary` -> Returns `{ "deviceCount": 10, "alertCount": 2, "quarantinedCount": 1, "onlineCount": 8 }`
- `GET /api/v1/alerts` -> Returns `AlertResponseDto[]`
- `POST /api/v1/traffic/report` -> Body: `{ "deviceId": "...", "packetRate": 500.0, "destDiversity": 20, "portDiversity": 10, "mqttFreq": 2.0, "isSimulatedAttack": true }`

---

## 3. Supported DeviceType Enum Values
- `ROUTER`, `SWITCH`, `ACCESS_POINT`, `CAMERA`, `PRINTER`, `SPEAKER`, `THERMOSTAT`, `SMART_TV`, `SMART_BULB`, `GATEWAY`, `SERVER`, `LAPTOP`, `MOBILE`, `UNKNOWN`.

---

## 4. Logging & Diagnostics
- **Backend Auto-Discovery**: Tagged as `[BACKEND AUTO-DISCOVERY]` in Node terminal logs.
- **Android Client Activity**: Tagged as `[ANDROID CLIENT REQUEST]` and `[ANDROID CLIENT SYNC]`, capturing client IP address and payload metadata.
- **Detailed Request Logger**: Logs incoming HTTP request method, URL, timestamp, sanitized body, and response duration in ms.
