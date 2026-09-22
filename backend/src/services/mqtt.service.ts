import mqtt from 'mqtt';
import dotenv from 'dotenv';

dotenv.config();

const MQTT_ENABLED = process.env.MQTT_ENABLED === 'true';
const MQTT_BROKER_URL = process.env.MQTT_BROKER_URL || 'mqtt://localhost:1883';

export class MqttService {
  private client: mqtt.MqttClient | null = null;
  private hasLoggedFailure = false;

  connect() {
    if (!MQTT_ENABLED) {
      console.log('🤖 [MQTT] MQTT integration is disabled via MQTT_ENABLED=false flag.');
      return;
    }

    try {
      console.log(`🤖 [MQTT] Attempting connection to broker at ${MQTT_BROKER_URL}...`);
      this.client = mqtt.connect(MQTT_BROKER_URL, {
        reconnectPeriod: 10000, // 10 seconds backoff instead of rapid polling
        connectTimeout: 5000
      });

      this.client.on('connect', () => {
        console.log('🤖 [MQTT] Connected successfully to MQTT Broker');
        this.hasLoggedFailure = false;
        this.client?.subscribe('iot/sensors/#');
      });

      this.client.on('message', (topic, message) => {
        console.log(`📩 [MQTT] Message [${topic}]:`, message.toString());
      });

      this.client.on('error', (err) => {
        if (!this.hasLoggedFailure) {
          console.warn('⚠️ [MQTT] Broker unavailable. Retrying with exponential backoff...');
          this.hasLoggedFailure = true;
        }
      });
    } catch (e) {
      if (!this.hasLoggedFailure) {
        console.warn('⚠️ [MQTT] Could not initialize MQTT client.');
        this.hasLoggedFailure = true;
      }
    }
  }
}

export const mqttService = new MqttService();
