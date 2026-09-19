import os
import joblib
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

app = FastAPI(title="SecureIoT ML Sidecar")

# Path to the Isolation Forest model
MODEL_PATH = "rogue_detector.joblib"

if os.path.exists(MODEL_PATH):
    model = joblib.load(MODEL_PATH)
else:
    model = None
    print(f"WARNING: {MODEL_PATH} not found. Ensure you run train_rogue_model.py first.")

class TrafficFeatures(BaseModel):
    packet_rate: float
    dest_diversity: int
    port_diversity: int
    mqtt_freq: float

@app.post("/predict")
async def predict(data: TrafficFeatures):
    if not model:
        # Emergency fallback if model file is missing
        is_anomaly = data.packet_rate > 500 or data.port_diversity > 50
        return {"is_anomaly": bool(is_anomaly), "risk_score": 0.85 if is_anomaly else 0.15}

    features = [[data.packet_rate, data.dest_diversity, data.port_diversity, data.mqtt_freq]]

    prediction = model.predict(features)[0]
    is_anomaly = bool(prediction == -1)

    decision = model.decision_function(features)[0]
    # Scale decision function to 0-1 risk score
    risk_score = float(max(0.0, min(1.0, 0.5 - decision)))

    return {"is_anomaly": is_anomaly, "risk_score": risk_score}

@app.get("/health")
def health():
    return {"status": "ok", "model_loaded": model is not None}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=5005)
