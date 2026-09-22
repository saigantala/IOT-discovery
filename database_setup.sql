-- Run this script in pgAdmin 4 (Query Tool) to create or update your database tables

-- Drop tables if they exist (Clean start)
DROP TABLE IF EXISTS "alerts" CASCADE;
DROP TABLE IF EXISTS "network_sessions" CASCADE;
DROP TABLE IF EXISTS "traffic_events" CASCADE;
DROP TABLE IF EXISTS "refresh_tokens" CASCADE;
DROP TABLE IF EXISTS "users" CASCADE;
DROP TABLE IF EXISTS "devices" CASCADE;

-- Create Users table
CREATE TABLE "users" (
    "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "name" TEXT NOT NULL,
    "email" TEXT UNIQUE NOT NULL,
    "password_hash" TEXT NOT NULL,
    "role" TEXT NOT NULL DEFAULT 'VIEWER',
    "is_active" BOOLEAN DEFAULT true,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Devices table
CREATE TABLE "devices" (
    "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "device_id" TEXT UNIQUE NOT NULL, -- Hardware MAC address or unique ID
    "name" TEXT NOT NULL,
    "type" TEXT NOT NULL,
    "ip_address" TEXT,
    "status" TEXT NOT NULL DEFAULT 'ONLINE',
    "discovery_source" TEXT DEFAULT 'BACKEND_AUTO_DISCOVERY', -- BACKEND_AUTO_DISCOVERY or ANDROID_APP_SYNC
    "last_seen" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "is_quarantined" BOOLEAN DEFAULT false
);

-- Create Network Sessions table
CREATE TABLE "network_sessions" (
    "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "device_id" TEXT REFERENCES "devices"("device_id"),
    "start_time" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "end_time" TIMESTAMP,
    "protocol" TEXT NOT NULL,
    "source_port" INTEGER,
    "dest_port" INTEGER,
    "bytes_sent" INTEGER DEFAULT 0,
    "bytes_received" INTEGER DEFAULT 0,
    "status" TEXT DEFAULT 'ACTIVE'
);

-- Create Traffic Events table
CREATE TABLE "traffic_events" (
    "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "device_id" TEXT REFERENCES "devices"("device_id"),
    "packet_rate" DOUBLE PRECISION,
    "dest_diversity" INTEGER,
    "port_diversity" INTEGER,
    "mqtt_freq" DOUBLE PRECISION,
    "risk_score" DOUBLE PRECISION,
    "is_anomaly" BOOLEAN,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Alerts table
CREATE TABLE "alerts" (
    "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "device_id" TEXT REFERENCES "devices"("device_id"),
    "traffic_event_id" UUID REFERENCES "traffic_events"("id"),
    "risk_score" DOUBLE PRECISION,
    "attack_type" TEXT,
    "severity" TEXT DEFAULT 'MEDIUM',
    "status" TEXT DEFAULT 'OPEN',
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add initial sample user
INSERT INTO "users" (name, email, password_hash, role)
VALUES ('Admin User', 'admin@example.com', '$2b$10$YourHashedPasswordHere', 'ADMIN');
