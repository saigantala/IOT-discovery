# IoT Discovery & Threat Detection Platform (SecureIoT Shield)

An enterprise-grade IoT security system combining an Android Mobile App (Jetpack Compose, Room, Retrofit), a Node.js REST API Backend (Express, TypeScript, PostgreSQL), and an optional Python Machine Learning sidecar for anomaly detection.

---

## 🏗️ Architecture & Source of Truth

```
[Android UI Screen]
       ↑ (Observes Flow / StateFlow)
[Room Local Database]  <-- Caches API DTOs as Entities
       ↑
[DataRepository]
       ↑ (Retrofit HTTP JSON DTOs)
[Node.js Express REST API (0.0.0.0:4000)]
       ↑ (Parameterized SQL & Transactions)
[PostgreSQL Database (Port 5433 or 5432 / pgAdmin 4)]
```

---

## 🛠️ PostgreSQL Setup & Migration Instructions

### 1. Database Connection Details
- **Host**: `127.0.0.1`
- **Port**: `5433` (or `5432`)
- **Database**: `secureiot`
- **User**: `postgres`
- **Password**: `SecureIoT2026Local` (or your local PostgreSQL password)

### 2. Run Database Setup Script
Execute `database_setup.sql` in pgAdmin 4 (Query Tool) or via psql in PowerShell:
```powershell
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -h 127.0.0.1 -p 5433 -d secureiot -f "database_setup.sql"
```

---

## 🚀 Starting the Backend Server

```powershell
cd backend
npm install
npm run dev
```

Confirm startup terminal output:
```text
🚀 Backend running on http://0.0.0.0:4000
🏥 Health check endpoint available at http://0.0.0.0:4000/health
🐘 PostgreSQL Connected successfully to Database: [secureiot] as User: [postgres]
🤖 [MQTT] MQTT integration is disabled via MQTT_ENABLED=false flag.
🔍 [BACKEND AUTO-DISCOVERY] Starting Background Network Discovery...
```

---

## 📱 Finding PC Wi-Fi IP & Configuring Android App

### 1. Find PC IPv4 Address
In PowerShell:
```powershell
ipconfig
```
Look for **Wireless LAN adapter Wi-Fi** $\rightarrow$ **IPv4 Address** (e.g., `172.30.116.78`).

### 2. Configure Android App Settings
1. Open the **IoT Discovery** app on your physical Android device or emulator.
2. Go to **Settings** $\rightarrow$ **Server IP Address**.
3. Enter your PC Wi-Fi IP (e.g., `172.30.116.78`).
4. Tap **Test Connection (/health)**.
   - On success, it displays: `✅ Connection Successful! Server Online | Database: connected`.
5. Tap **Save & Apply**.

---

## 🧪 Postman & cURL Test Examples

### A. Health Check (`GET /health`)
```bash
curl -X GET http://localhost:4000/health
```

### B. User Registration (`POST /api/v1/auth/register`)
```bash
curl -X POST http://localhost:4000/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Security Admin",
    "email": "admin@enterprise.io",
    "password": "Password123!"
  }'
```

### C. User Login (`POST /api/v1/auth/login`)
```bash
curl -X POST http://localhost:4000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@enterprise.io",
    "password": "Password123!"
  }'
```

### D. Sync Discovered Devices (`POST /api/v1/devices/sync`)
```bash
curl -X POST http://localhost:4000/api/v1/devices/sync \
  -H "Content-Type: application/json" \
  -d '{
    "devices": [
      {
        "deviceId": "AA:BB:CC:DD:EE:99",
        "name": "Smart Security Camera",
        "type": "CAMERA",
        "ipAddress": "172.30.116.50",
        "status": "ONLINE"
      }
    ]
  }'
```

### E. Get Devices (`GET /api/v1/devices`)
```bash
curl -X GET http://localhost:4000/api/v1/devices
```

### F. Quarantine Device (`POST /api/v1/devices/:id/quarantine`)
```bash
curl -X POST http://localhost:4000/api/v1/devices/AA:BB:CC:DD:EE:99/quarantine
```

### G. Unquarantine Device (`POST /api/v1/devices/:id/unquarantine`)
```bash
curl -X POST http://localhost:4000/api/v1/devices/AA:BB:CC:DD:EE:99/unquarantine
```

### H. Dashboard Summary (`GET /api/v1/dashboard/summary`)
```bash
curl -X GET http://localhost:4000/api/v1/dashboard/summary
```

### I. Report Traffic Anomaly (`POST /api/v1/traffic/report`)
```bash
curl -X POST http://localhost:4000/api/v1/traffic/report \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": "AA:BB:CC:DD:EE:99",
    "packetRate": 850.0,
    "destDiversity": 40,
    "portDiversity": 20,
    "mqttFreq": 12.0,
    "isSimulatedAttack": true
  }'
```

### J. User Logout (`POST /api/v1/auth/logout`)
```bash
curl -X POST http://localhost:4000/api/v1/auth/logout \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN_HERE"
  }'
```

---

## ❓ Troubleshooting Matrix

| Issue | Root Cause | Resolution |
| :--- | :--- | :--- |
| **`ECONNREFUSED 127.0.0.1:5433`** | PostgreSQL service is stopped or port mismatch. | Run `Restart-Service -Name "postgresql*"` in Admin PowerShell. Check port 5433/5432 in `.env`. |
| **Backend port 4000 not listening** | Node server crashed or process already running. | Run `npm run dev` in `backend` folder. Check `DATABASE_URL` in `backend/.env`. |
| **Phone cannot access backend** | PC and Phone on different Wi-Fi networks or wrong IP. | Ensure phone & PC are on same Wi-Fi. Update IP in app Settings to match `ipconfig`. |
| **Login fails (`INVALID_CREDENTIALS`)** | Password mismatch or user not registered. | Register first via `/api/v1/auth/register` or Register screen in app. |
| **Missing database tables** | `database_setup.sql` script hasn't been run. | Execute `database_setup.sql` in pgAdmin 4 Query Tool on `secureiot` database. |
| **MQTT broker unavailable** | Mosquitto broker stopped while MQTT enabled. | Set `MQTT_ENABLED=false` in `backend/.env` to suppress MQTT connection attempts. |
