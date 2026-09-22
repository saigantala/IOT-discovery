# IoT Discovery & Threat Detection System (SecureIoT Shield)

An end-to-end enterprise IoT Security application combining an Android Mobile App (Jetpack Compose, Room, Retrofit), a Node.js REST API Backend (Express, TypeScript, PostgreSQL), and a Python Machine Learning sidecar for anomaly detection.

---

## Architecture Overview

```
Android UI (Jetpack Compose)
       ↓
DataRepository (Room DB = Offline Cache)
       ↓
Retrofit REST API
       ↓
Node.js Express Backend (Port 4000, 0.0.0.0)
       ↓
PostgreSQL Database (pgAdmin 4 / Port 5432)
```

---

## 🛠️ Testing & Setup Instructions

### 1. Find PC Wi-Fi IPv4 Address
In PowerShell or Command Prompt, run:
```powershell
ipconfig
```
Look for **Wireless LAN adapter Wi-Fi** -> **IPv4 Address** (e.g., `172.30.116.78` or `192.168.1.50`).

---

### 2. Start PostgreSQL & Run Database Setup
Make sure PostgreSQL service is running, then run `database_setup.sql` in pgAdmin 4 or via psql:
```powershell
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -h 127.0.0.1 -d secureiot -f "database_setup.sql"
```

---

### 3. Start the Backend Server
Navigate to the `backend` directory and start the server in development mode:
```powershell
cd backend
npm install
npm run dev
```
Confirm output shows:
```
🚀 Backend running on http://0.0.0.0:4000
🏥 Health check endpoint available at http://0.0.0.0:4000/health
🐘 PostgreSQL Connected successfully to Database: [secureiot] as User: [postgres]
```

---

### 4. Verify Health Endpoint
Open in browser or terminal:
- From PC: `http://localhost:4000/health`
- From Phone/Browser on same Wi-Fi: `http://172.30.116.78:4000/health`

Expected JSON response:
```json
{
  "status": "OK",
  "server": "online",
  "timestamp": "2026-09-22T10:00:00.000Z",
  "database": {
    "status": "connected",
    "name": "secureiot",
    "latencyMs": 5
  }
}
```

---

## 🧪 Postman / cURL Test Examples

### A. User Registration (`POST /api/v1/auth/register`)
```bash
curl -X POST http://localhost:4000/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "Password123!"
  }'
```

### B. User Login (`POST /api/v1/auth/login`)
```bash
curl -X POST http://localhost:4000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "Password123!"
  }'
```

### C. Device Sync (`POST /api/v1/devices/sync`)
```bash
curl -X POST http://localhost:4000/api/v1/devices/sync \
  -H "Content-Type: application/json" \
  -d '{
    "devices": [
      {
        "deviceId": "AA:BB:CC:DD:EE:01",
        "name": "Living Room Smart Cam",
        "type": "CAMERA",
        "ipAddress": "192.168.1.45",
        "status": "ONLINE"
      }
    ]
  }'
```

### D. Get All Devices (`GET /api/v1/devices`)
```bash
curl -X GET http://localhost:4000/api/v1/devices
```

### E. Quarantine Device (`POST /api/v1/devices/:id/quarantine`)
```bash
curl -X POST http://localhost:4000/api/v1/devices/AA:BB:CC:DD:EE:01/quarantine
```

### F. Get Dashboard Summary (`GET /api/v1/dashboard/summary`)
```bash
curl -X GET http://localhost:4000/api/v1/dashboard/summary
```

### G. Report Traffic Anomaly (`POST /api/v1/traffic/report`)
```bash
curl -X POST http://localhost:4000/api/v1/traffic/report \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": "AA:BB:CC:DD:EE:01",
    "packetRate": 850.5,
    "destDiversity": 45,
    "portDiversity": 30,
    "mqttFreq": 12.0,
    "isSimulatedAttack": true,
    "attackType": "DDoS Flood"
  }'
```

---

## 🔒 Production HTTPS & Security Preparation
For production deployment:
1. Update `backend/.env` with production SSL certificates / reverse proxy (Nginx).
2. Configure Android Network Security Config (`res/xml/network_security_config.xml`) specifying domain pin / HTTPS enforced endpoints.

---

## ✅ Final End-to-End Verification Checklist

1. **Android Request**: Tap **Start Deep Scan** in Android App.
2. **Node Terminal Log**: Observe terminal printing:
   `📱 [ANDROID CLIENT SYNC] Received POST /api/v1/devices/sync request from Android device (172.30.116.X)`
   `💾 [ANDROID CLIENT SYNC] Saved/Updated device from Android client...`
3. **Node API**: API responds with HTTP `200 OK` `{ "success": true, "count": N }`.
4. **PostgreSQL Row**: Run `SELECT * FROM devices WHERE discovery_source = 'ANDROID_APP_SYNC';` in pgAdmin 4. Rows appear!
5. **Android UI Update**: Android UI updates device list and dashboard counters dynamically via Room cached stream fed by REST API responses.
