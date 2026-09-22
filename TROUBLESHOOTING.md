# IoT Discovery: Short Troubleshooting Guide

## Normal startup

Run the backend in its own terminal:

```powershell
cd "C:\Users\YETHENDRA SAI\AndroidStudioProjects\IOT\backend"
npm run dev
```

It must remain open and display both `Backend running` and `PostgreSQL Connected successfully`.

## Health check

In a second terminal, run this exact command (do not paste `PS C:\...>`, `+`, or Markdown brackets):

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1:4000/health"
```

If `Test-NetConnection 127.0.0.1 -Port 4000` is `False`, the backend is not running. Start it again in a separate terminal and leave that terminal open.

## PostgreSQL errors

`ECONNREFUSED 127.0.0.1:PORT` means the PostgreSQL port in `backend/.env` is wrong or the database service is stopped.

```powershell
Get-Service *postgres*
netstat -ano | findstr LISTENING | findstr 543
```

Set `DATABASE_URL` to the port that is actually listening. On this machine PostgreSQL was found on port `5433`; do not use `5432` unless it is listening.

## Physical Android device

Use the PC's Wi-Fi IPv4 address, not `localhost`, `127.0.0.1`, or a virtual-network address. Find it with:

```powershell
ipconfig
```

Set the Android backend address to `http://YOUR_WIFI_IP:4000/api/v1/`, then open `http://YOUR_WIFI_IP:4000/health` in the phone browser. Both devices must be on the same Wi-Fi network.

## Login verification

Register a user first, then sign in using the Android email/password form. A successful login emits terminal logs for `POST /api/v1/auth/login`, the database lookup, and the response status. Passwords and tokens are intentionally masked in the logs.

## Known project limits

- Several visual screens still display demo/mock data. They need individual API-backed ViewModels before every screen is fully live.
- Backend auto-discovery runs every two minutes and writes its own records. Those logs are server activity, not proof that the Android app reached the backend.
- MQTT is intentionally disabled when `MQTT_ENABLED=false`; enable it only after installing/configuring a broker.
