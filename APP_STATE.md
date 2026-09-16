# Project State: FarmRPG Companion

## [Implemented]
- **Streamlined Navigation Web Wrapper**: Embedded high-performance WebView loaded with FarmRPG.com. Configured with session-persistent local storage, a custom user-agent, and direct quick-jump shortcut buttons for the Farm, Workshop, Market, Town, Fishing, and Inventory.
- **Custom Touch Coordinate Tracker**: Automatically intercepts touches on the WebView to dynamically register precise click coordinates (`window.lastTouchX` and `window.lastTouchY`) directly in the JavaScript client context, allowing pinpoint macro auto-clicking.
- **Smart Macro Manager**: Features a speed controller (200ms to 5000ms delay slider) to run loops of built-in macro routines (Auto-Explore, Auto-Fishing, custom Spot Clickers, and Workshop Crafting Helpers) or custom-coded JavaScript scripts.
- **Live Terminal Console**: A real-time scrolling logger displaying live execution steps, iteration counts, and active script output.
- **Farm & Crop Alarms (Timers)**: A comprehensive timers panel featuring preset schedules for FarmRPG crops (Wheat 1m, Hops 10m, Carrots 15m, Potatoes 30m, Cabbage 1h), winery stock maturing (4h), and expeditions (8h). Includes high-contrast progress gauges and a system notification sound when ready.
- **Workshop Crafting Calculator**: Visual stock tracker (Wood, Iron, Stone, Clay, Straw, Feather) with instant calculations of total craftable items, silver revenue potentials, and specific material bottleneck notifications.
- **Centralized Data Persistence**: Integrated a standard local Room Database running on KSP to persist customized macros, active countdown timers, and material inventories.
- **Cozy Minimalist Theme**: Designed a custom earth-tone visual system featuring Deep Forest Greens, Warm Wheat Ambers, Soft Parchment canvas, and high contrast accents.
- **Custom Adaptive Launcher Icon**: Generated a custom visual asset combining a golden sprout with a copper gear, configured with a matching forest green background layer.

## [Next Up]
- Customizable audio alarm selection.
- Multi-device local backup option (JSON export and import).

## [Out of Scope]
- Cloud-hosted synchronization or multiplayer accounts (restricted to client-side offline Room persistence).
- Automatic CAPTCHA solving or invasive server-side request spamming (bypassing anti-cheat).

## [Files]
- `/README.md`
- `/app/src/main/AndroidManifest.xml`
- `/app/src/main/java/com/example/MainActivity.kt`
- `/app/src/main/java/com/example/data/MacroEntity.kt`
- `/app/src/main/java/com/example/data/TimerEntity.kt`
- `/app/src/main/java/com/example/data/CompanionDao.kt`
- `/app/src/main/java/com/example/data/CompanionDatabase.kt`
- `/app/src/main/java/com/example/data/CompanionRepository.kt`
- `/app/src/main/java/com/example/ui/MainLayout.kt`
- `/app/src/main/java/com/example/ui/components/MacroBrowser.kt`
- `/app/src/main/java/com/example/ui/components/MacroManagerTab.kt`
- `/app/src/main/java/com/example/ui/components/TimersTab.kt`
- `/app/src/main/java/com/example/ui/components/CalculatorTab.kt`
- `/app/src/main/java/com/example/ui/theme/Color.kt`
- `/app/src/main/java/com/example/ui/theme/Theme.kt`
