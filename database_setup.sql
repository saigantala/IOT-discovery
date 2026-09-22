-- Run this script in pgAdmin 4 (Query Tool) to create or update your database tables

-- Enable pgcrypto extension for gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Drop tables if they exist (Clean start for schema migration)
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

-- Create Refresh Tokens table
CREATE TABLE "refresh_tokens" (
    "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "user_id" UUID REFERENCES "users"("id") ON DELETE CASCADE,
    "token_hash" TEXT NOT NULL,
    "expires_at" TIMESTAMP NOT NULL,
    "revoked" BOOLEAN DEFAULT false,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Devices table
CREATE TABLE "devices" (
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

-- Add initial admin user (Password: Admin@123456)
INSERT INTO "users" (name, email, password_hash, role)
VALUES ('Admin User', 'admin@example.com', '$2b$10$e5XzLd8yL8qE/aHjU2Yy3.4oHjE3k5p4M8A8s7P6k5j4M3a2P1b0', 'ADMIN')
ON CONFLICT (email) DO NOTHING;
