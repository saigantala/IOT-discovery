<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# IoT Discovery - Enterprise Network Shield

## Sprint 1: Core Infrastructure & Real-Time Dashboard

**Completed Features:**
- **Real-Time Dashboard**: Live monitoring of network health, asset counts, and security alerts.
- **Unified History Dashboard**: Integrated Device Discovery logs and Admin Login history with live status indicators.
- **Enterprise Security**: 
    - Secure Biometric & Google Workspace authentication.
    - Encrypted credential storage using AES-256.
- **Network Discovery**: Subnet scanning simulation with radar visualization.
- **IoT Fingerprinting**: Automated identification of device manufacturers and OS.
- **Reactive Architecture**: Fully powered by Kotlin Flows and MVVM for a "living" UI.

## Sprint 2: Advanced Discovery & Device Intelligence

**Completed Features:**
- **Refined Device Intelligence**: Brand new `DeviceDetails` UI with deep technical specs (OS, MAC, discovery protocol, location).
- **Proximity Filtering**: Intelligent discovery restricted to the local network access point and nearby subnet.
- **Service Mapping**: Detailed identification of open ports (22, 80, 443, etc.) and active services (SSH, HTTP, SNMP) with verified icons.
- **Enhanced Scanning UX**: New "Scan Subnet" floating action button and "Deep Inspect" functionality with enterprise-grade dark themes.
- **Event Auditing**: Device-specific event history tracking for auditing and security monitoring.
- **Real-Time API Integration**: Connected device discovery to a real-time reactive data repository.

## Run Locally
... (existing run instructions)

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device
7. If you have already published your app in AI Studio, please [request upload key reset](https://support.google.com/googleplay/android-developer/answer/9842756#zippy=%2Crequest-an-upload-key-reset) in Google Play Console.
