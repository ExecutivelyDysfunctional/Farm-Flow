# FarmRPG Companion

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%2C%20Compose-Material%203-blue.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**FarmRPG Companion** is a feature-rich, cozy Android application designed to elevate your FarmRPG experience. Built with modern Kotlin and Jetpack Compose, it combines an integrated, session-persistent WebView with powerful convenience tools, automated macro helpers, precise harvest timers, and workshop crafting calculators.

---

## 📖 Part 1: User Guide

### ✨ Key Features

1. **Embedded Web Companion & Quick Navigation**
   - High-performance WebView pre-configured for FarmRPG.com with session cookie persistence and custom user-agent support.
   - One-tap quick-jump shortcut buttons for the **Farm**, **Workshop**, **Market**, **Town**, **Fishing**, and **Inventory**.

2. **Smart Macro Manager & Touch Coordinates**
   - Built-in speed controller (200ms to 5000ms delay slider) for automated loops (Auto-Explore, Auto-Fishing, custom spot clickers, and workshop crafting helpers).
   - Custom touch coordinate tracker that intercepts WebView interactions to register precise click coordinates (`lastTouchX` and `lastTouchY`) for pinpoint automation.
   - **Live Terminal Console**: Real-time scrolling logger displaying execution steps, iteration counts, and active script outputs.

3. **Farm & Crop Alarms (Timers)**
   - Pre-configured countdown timers for staple FarmRPG crops and activities:
     - Crops: Wheat (1m), Hops (10m), Carrots (15m), Potatoes (30m), Cabbage (1h)
     - Production: Winery Stock (4h), Expeditions (8h)
   - High-contrast progress gauges and audio notification cues when harvests are ready.

4. **Workshop Crafting Calculator**
   - Real-time stock tracker for essential materials (Wood, Iron, Stone, Clay, Straw, Feather).
   - Instant calculation of total craftable items, silver revenue potentials, and specific material bottleneck alerts.

5. **Cozy Minimalist Theme**
   - Custom earth-tone palette featuring Deep Forest Greens, Warm Wheat Ambers, Soft Parchment canvas, and Material Design 3 surfaces.

---

### 🚀 Getting Started & Usage

1. **Installation**: Download the latest `app-debug.apk` release from the repository or build locally. Install on any Android 8.0+ device.
2. **Browsing**: Launch the app to open the integrated FarmRPG game view. Use the top/bottom navigation bar to access quick shortcuts.
3. **Running Timers**: Tap the **Timers** tab to start harvest countdowns. You will receive visual and audio prompts when crops are ready.
4. **Calculations**: Use the **Calculator** tab to input your current workshop material inventory and instantly see maximum craft potentials and silver valuations.
5. **Automation**: Use the **Macro** tab to configure delay speeds and execute automated routines or custom JavaScript snippets.

---

## 🛠️ Part 2: Developer Guide

### 🧱 Tech Stack & Architecture

- **Language**: 100% Kotlin
- **UI Framework**: Jetpack Compose with Material Design 3 (M3)
- **Architecture**: Model-View-ViewModel (MVVM) with unidirectional data flow
- **Local Persistence**: Room Database powered by KSP (Kotlin Symbol Processing)
- **Async Processing**: Kotlin Coroutines & Flows
- **Web Integration**: Android WebView with JavaScript Bridge & Touch Event Interception

### 📂 Project Structure

```tree
app/
├── src/main/java/com/example/
│   ├── MainActivity.kt                  # Main entry point & window setup
│   ├── data/
│   │   ├── CompanionDao.kt              # Room DAO for macros and timers
│   │   ├── CompanionDatabase.kt         # Room Database definition & converters
│   │   └── CompanionRepository.kt       # Repository data abstraction layer
│   └── ui/
│       ├── MainLayout.kt                # Primary screen scaffolding & navigation
│       ├── components/                  # Feature tabs (Calculator, Timers, Macros, WebView)
│       ├── theme/                       # M3 Colors, Typography, and Theme
│       └── viewmodel/                   # Companion & FarmRPG ViewModels
```

### ⚙️ Building and Running Locally

1. **Prerequisites**:
   - Android Studio Koala / Ladybug or newer.
   - JDK 17 or higher.
   - Android SDK with API 34 (UpsideDownCake).

2. **Clone & Open**:
   ```bash
   git clone https://github.com/your-username/farmrpg-companion.git
   cd farmrpg-companion
   ```
   Open the root folder in Android Studio.

3. **Build via Gradle**:
   ```bash
   gradle :app:assembleDebug
   ```
   The compiled debug APK will be located at `app/build/outputs/apk/debug/app-debug.apk`.

4. **Running Unit & Robolectric Tests**:
   ```bash
   gradle :app:testDebugUnitTest
   ```

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit issues or pull requests for bug fixes, performance optimizations, or new helper utilities.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.
