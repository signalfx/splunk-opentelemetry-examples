# React Native Astronomy Shop - Setup Guide

How to install dependencies and run the **Astronomy Shop** React Native app (`react-native-sample/`) on iOS and Android.

>This is an **Expo SDK 54** app. Development uses **Expo Go**. Splunk RUM instrumentation requires a **development build**
>See [MRUM-Splunk-Instrumentation-Guide.md](./MRUM-Splunk-Instrumentation-Guide.md).

---

## Prerequisites

| Platform | Required |
|---|---|
| All | Node.js 18+, npm |
| iOS (macOS only) | Xcode 15+ with iOS Simulator runtime |
| Android | Android Studio with an emulator, **or** a physical device with Expo Go from the Play Store |

Install project dependencies:

```bash
cd react-native-sample
npm install
```
---

## Run on iOS Simulator

### 1. Install Xcode and a simulator

1. Install **Xcode** from the App Store.
2. Open **Xcode → Settings → Platforms** and install an **iOS Simulator** runtime.
3. Accept the license if prompted:

```bash
sudo xcodebuild -license accept
```

Verify simulators are available:

```bash
xcrun simctl list devices available | grep iPhone
```

### 2. Start the app

```bash
cd react-native-sample
npm run ios
```

This script:

- Opens the iOS Simulator (boots an iPhone if none is running)
- Installs **Expo Go** on the simulator if missing
- Starts Metro at `http://127.0.0.1:8081` with `--localhost`
- Opens `exp://127.0.0.1:8081` in Expo Go
---

## Run on Android

### Emulator (recommended)

1. Create an AVD in **Android Studio → Device Manager** (API 24+).
2. Start the emulator, then run:

```bash
cd react-native-sample
npm run android
```

This script:

- Kills stale Metro on port 8081
- Runs `adb reverse tcp:8081 tcp:8081` so the emulator reaches Metro at `127.0.0.1`
- Starts Expo with `--localhost` (LAN URLs fail on emulators)

>**NOTE: Use Play Store Expo Go** on the emulator. The app targets **SDK 54**; an older Expo Go will show a compatibility error.

### Physical Android device

Same Wi‑Fi as your computer:

```bash
npm run android:device
```

If the device cannot reach your machine over LAN:

```bash
npm run android:tunnel
```
---

## npm scripts

| Script | Purpose |
|---|---|
| `npm start` | Metro only (`expo start --clear`) |
| `npm run ios` | iOS Simulator + Expo Go + Metro (recommended) |
| `npm run ios:install-expo-go` | Install Expo Go on booted simulator only |
| `npm run android` | Android emulator + Metro with localhost/adb reverse |
| `npm run android:device` | Physical Android on same Wi‑Fi |
| `npm run android:tunnel` | Physical Android via Expo tunnel |

---

@domuoyo-dotcom- July 2026
