import axios from 'axios';
import dotenv from 'dotenv';

dotenv.config();

const ML_SERVICE_URL = process.env.ML_SERVICE_URL || 'http://localhost:5005';

export class MLService {
  static async predict(trafficData: {
    packet_rate: number;
    dest_diversity: number;
    port_diversity: number;
    mqtt_freq: number;
  }) {
    try {
      const response = await axios.post(`${ML_SERVICE_URL}/predict`, trafficData);
      return response.data;
    } catch (error) {
      console.warn('⚠️ ML Service unreachable, using fallback logic');

      // Fallback logic if the Python ML service is down
      const is_anomaly = trafficData.packet_rate > 500 || trafficData.port_diversity > 50;
      return {
        is_anomaly,
        risk_score: is_anomaly ? 0.85 : 0.15
      };
    }
  }
}
