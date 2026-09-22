import requests
import time
import random
import uuid

BACKEND_URL = "http://localhost:4000/api/v1"
DEVICE_ID = "IOT-MOCK-DEVICE-001"

def simulate_network_session():
    # Simulate a detailed network session
    protocols = ["TCP", "UDP", "MQTT", "HTTP"]
    session_data = {
        "deviceId": DEVICE_ID,
        "protocol": random.choice(protocols),
        "sourcePort": random.randint(1024, 65535),
        "destPort": random.choice([80, 443, 1883, 8080]),
        "bytesSent": random.randint(100, 5000),
        "bytesReceived": random.randint(100, 10000),
        "status": "ACTIVE"
    }

    print(f"📡 Sending Network Session: {session_data['protocol']} from {DEVICE_ID} port {session_data['sourcePort']} -> {session_data['destPort']}")

    try:
        # Note: You'll need to create this endpoint in your backend routes
        response = requests.post(f"{BACKEND_URL}/sessions", json=session_data)
        if response.status_code == 201 or response.status_code == 200:
            print("   ✅ Session logged successfully")
        else:
            print(f"   ❌ Backend error: {response.status_code} - {response.text}")
    except Exception as e:
        print(f"   ❌ Connection Failed: {e}")

if __name__ == "__main__":
    print("🚀 SecureIoT Shield Network Session Simulator Started")
    print(f"Targeting Backend: {BACKEND_URL}")

    # Ensure the mock device exists in the DB first (this depends on your backend logic)
    # For now, we just start sending sessions.

    try:
        while True:
            simulate_network_session()
            time.sleep(random.randint(2, 8)) # Random delay between sessions
    except KeyboardInterrupt:
        print("\n🛑 Simulator stopped.")
