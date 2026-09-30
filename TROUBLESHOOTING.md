# IoT Security System - Troubleshooting Guide

| Issue / Error | Root Cause | Actionable Resolution |
| :--- | :--- | :--- |
| **`ECONNREFUSED 127.0.0.1:5433`** | PostgreSQL service is stopped or running on port 5432 instead of 5433. | 1. Open PowerShell as Administrator.<br>2. Run `Restart-Service -Name "postgresql*"`.<br>3. Verify port in `backend/.env` (`DATABASE_URL=postgresql://postgres:PASSWORD@127.0.0.1:5433/secureiot`). |
| **Backend port 4000 not listening** | Node.js backend server is stopped or crashed. | 1. Open terminal: `cd backend`.<br>2. Run `npm run dev`.<br>3. Confirm terminal outputs: `Backend listening on http://0.0.0.0:4000`. |
| **Phone cannot access backend** | Phone & PC on different Wi-Fi networks or wrong IP configured in settings. | 1. Connect phone to same Wi-Fi as PC.<br>2. Find PC IP via `ipconfig` (e.g. `172.30.116.78`).<br>3. Open App $\rightarrow$ **Settings** $\rightarrow$ **Backend Server IP**.<br>4. Enter IP, tap **Test Connection (/health)**, then tap **Save & Apply**. |
| **Login fails (`INVALID_CREDENTIALS`)** | User email/password not registered or typo. | 1. Open App $\rightarrow$ Tap **Don't have an account? Register Here**.<br>2. Create a new user account (e.g. `admin@enterprise.io` / `Password123!`).<br>3. Retry login. |
| **Missing database tables** | `database_setup.sql` has not been executed on the `secureiot` DB. | 1. Open pgAdmin 4 or psql.<br>2. Connect to `secureiot` database.<br>3. Run `database_setup.sql` script to create all 6 tables (`users`, `refresh_tokens`, `devices`, `network_sessions`, `traffic_events`, `alerts`). |
| **MQTT broker unavailable warnings** | Mosquitto broker is not installed or stopped while `MQTT_ENABLED=true`. | Set `MQTT_ENABLED=false` in `backend/.env`. The system will run normally without MQTT. |
