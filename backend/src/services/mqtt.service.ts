import mqtt from 'mqtt';
import dotenv from 'dotenv';

dotenv.config();

const MQTT_BROKER_URL = process.env.MQTT_BROKER_URL || 'mqtt://localhost:1883';

export class MqttService {
  private client: mqtt.MqttClient | null = null;

  connect() {
    try {
      this.client = mqtt.connect(MQTT_BROKER_URL);

      this.client.on('connect', () => {
        console.log('🤖 Connected to MQTT Broker');
        this.client?.subscribe('iot/sensors/#');
      });

      this.client.on('message', (topic, message) => {
        console.log(`📩 MQTT Message [${topic}]:`, message.toString());
        // Handle IoT data here
      });

      this.client.on('error', (err) => {
        console.warn('⚠️ MQTT Connection failed, ensure Mosquitto is running');
      });
    } catch (e) {
      console.warn('⚠️ MQTT not available');
    }
  }
}

export const mqttService = new MqttService();
