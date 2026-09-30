-- Safe and Idempotent PostgreSQL Setup Script for secureiot database

-- Enable pgcrypto extension for gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Create Users table
CREATE TABLE IF NOT EXISTS "users" (
    "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "name" TEXT NOT NULL,
    "email" TEXT UNIQUE NOT NULL,
    "password_hash" TEXT NOT NULL,
    "role" TEXT NOT NULL DEFAULT 'VIEWER',
    "is_active" BOOLEAN DEFAULT true,
    "created_at" TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Create Refresh Tokens table
CREATE TABLE IF NOT EXISTS "refresh_tokens" (
    "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "user_id" UUID REFERENCES "users"("id") ON DELETE CASCADE,
    "token_hash" TEXT NOT NULL,
    "expires_at" TIMESTAMPTZ NOT NULL,
    "revoked" BOOLEAN DEFAULT false,
    "created_at" TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Create Devices table
CREATE TABLE IF NOT EXISTS "devices" (
    "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "device_id" TEXT UNIQUE NOT NULL, -- Hardware MAC address or unique ID
    "name" TEXT NOT NULL,
    "type" TEXT NOT NULL DEFAULT 'UNKNOWN',
    "ip_address" TEXT,
    "status" TEXT NOT NULL DEFAULT 'ONLINE',
    "discovery_source" TEXT DEFAULT 'BACKEND_AUTO_DISCOVERY',
    "manufacturer" TEXT DEFAULT 'Unknown Vendor',
    "hostname" TEXT,
    "os" TEXT,
    "risk_level" TEXT DEFAULT 'LOW',
    "risk_score" INTEGER DEFAULT 0,
    "risk_reason" TEXT,
    "open_ports" TEXT DEFAULT '[]', -- JSON string array
    "services" TEXT DEFAULT '[]',   -- JSON string array
    "fingerprint_confidence" INTEGER DEFAULT 90,
    "last_seen" TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    "is_quarantined" BOOLEAN DEFAULT false
);

-- Create Network Sessions table
CREATE TABLE IF NOT EXISTS "network_sessions" (
    "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "device_id" TEXT REFERENCES "devices"("device_id"),
    "start_time" TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    "end_time" TIMESTAMPTZ,
    "protocol" TEXT NOT NULL,
    "source_port" INTEGER,
    "dest_port" INTEGER,
    "bytes_sent" INTEGER DEFAULT 0,
    "bytes_received" INTEGER DEFAULT 0,
    "status" TEXT DEFAULT 'ACTIVE'
);

-- Create Traffic Events table
CREATE TABLE IF NOT EXISTS "traffic_events" (
    "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "device_id" TEXT REFERENCES "devices"("device_id"),
    "packet_rate" DOUBLE PRECISION,
    "dest_diversity" INTEGER,
    "port_diversity" INTEGER,
    "mqtt_freq" DOUBLE PRECISION,
    "risk_score" DOUBLE PRECISION,
    "is_anomaly" BOOLEAN,
    "created_at" TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Create Alerts table
CREATE TABLE IF NOT EXISTS "alerts" (
    "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "device_id" TEXT REFERENCES "devices"("device_id"),
    "traffic_event_id" UUID REFERENCES "traffic_events"("id"),
    "risk_score" DOUBLE PRECISION,
    "attack_type" TEXT,
    "severity" TEXT DEFAULT 'MEDIUM',
    "status" TEXT DEFAULT 'OPEN',
    "created_at" TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Ensure time columns use TIMESTAMPTZ for existing installations
ALTER TABLE users ALTER COLUMN created_at TYPE TIMESTAMPTZ, ALTER COLUMN updated_at TYPE TIMESTAMPTZ;
ALTER TABLE refresh_tokens ALTER COLUMN expires_at TYPE TIMESTAMPTZ, ALTER COLUMN created_at TYPE TIMESTAMPTZ;
ALTER TABLE devices ALTER COLUMN last_seen TYPE TIMESTAMPTZ;
ALTER TABLE network_sessions ALTER COLUMN start_time TYPE TIMESTAMPTZ, ALTER COLUMN end_time TYPE TIMESTAMPTZ;
ALTER TABLE traffic_events ALTER COLUMN created_at TYPE TIMESTAMPTZ;
ALTER TABLE alerts ALTER COLUMN created_at TYPE TIMESTAMPTZ;
