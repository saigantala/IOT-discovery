# IoT Discovery - Enterprise Network Shield
## Project Documentation

### 1. Overview
**IoT Discovery** is an enterprise-grade Android application designed for real-time local network scanning, IoT asset mapping, and threat intelligence. It provides network administrators with a "living" dashboard to monitor network health, identify rogue devices, and assess security risks across a local subnet.

### 2. Architecture
The project follows modern Android development best practices with a clean, reactive architecture:
- **UI Layer**: Built entirely with **Jetpack Compose** and **Material 3**. It uses a **reactive UI model** where screens observe state from ViewModels.
- **ViewModel Layer**: Implements the **MVVM pattern**. ViewModels consume data streams (Flows) from the Repository and maintain UI state.
- **Data Layer (Repository Pattern)**:
    - `DataRepository`: Acts as the single source of truth, providing real-time data streams for devices, alerts, and system status.
    - `CredentialsManager`: Handles secure, encrypted storage of sensitive session data.
    - `MockData`: Provides a high-fidelity simulation of enterprise network data for testing and demonstration.
- **Dependency Injection**: A simplified `AppModule` provides centralized access to repositories and managers.

### 3. Core Features

#### Sprint 1: Infrastructure & Real-Time Monitoring
- **Real-Time Dashboard**: A comprehensive overview of network health, security scores, and quick statistics.
- **Unified History Dashboard**: A tabbed interface merging **Device Discovery Logs** and **Admin Login History** with live status indicators.
- **Security Compliance**: Automated report generation and recommendations for hardening IoT devices.
- **Activity Timeline**: A global log of network events (connections, disconnections, flaggings).

#### Sprint 2: Advanced Discovery & Intelligence
- **Intelligent Subnet Scanning**: Simulated discovery restricted to the local network access point (`192.168.1.*`).
- **High-Fidelity Device Details**: Deep technical specs for every asset, including:
    - **Risk Assessment**: Score-based risk leveling with detailed reasoning.
    - **Fingerprinting**: 99% confidence matching for device manufacturers and OS versions.
    - **Service Mapping**: Identification of open ports (22, 80, 443, etc.) and active protocols (SSH, SNMP, etc.).
- **Enterprise Design System**: A custom-themed UI using "Enterprise Dark" (#111D25) and "Security Blue" accents.
- **Interactive Scanning**: Floating action buttons for subnet sweeps and "Deep Inspect" tools.

### 4. Security Implementation
- **Authentication**: Supports Biometric (Fingerprint/Face) and Google Workspace login.
- **Data Encryption**: Uses **AES-256 GCM** encryption via `EncryptedSharedPreferences` for all local storage (API tokens, session data).
- **Isolation**: Identifies and flags "Rogue Assets" for immediate network isolation.

### 5. Technical Stack
- **Language**: Kotlin 2.1.0
- **UI**: Jetpack Compose (Compose BOM 2024.09.00)
- **Concurrency**: Kotlin Coroutines & Flows
- **Navigation**: Compose Navigation
- **Networking**: Retrofit 2.12.0 (Configured for API integration)
- **Serialization**: Moshi 1.15.2
- **Persistence**: Room Database 2.7.0 & EncryptedSharedPreferences

### 6. Setup & Execution
1. **Prerequisites**: Android Studio Ladybug or later.
2. **Environment**: Create a `.env` file in the root directory with your `GEMINI_API_KEY`.
3. **Build**: Run `./gradlew assembleDebug` or use the "Run" button in Android Studio.
4. **Authentication**: Use the "Login with Biometrics" or "Sign in with Google" options to access the dashboard.

### 7. Future Roadmap (Sprint 3+)
- **AI-Powered Threat Detection**: Integration with Gemini API for automated traffic analysis.
- **Network Topology Graph**: Interactive 2D/3D mapping of device relationships.
- **Push Notifications**: Real-time FCM alerts for critical security breaches.
- **Export Capabilities**: PDF/CSV export for enterprise compliance audits.
