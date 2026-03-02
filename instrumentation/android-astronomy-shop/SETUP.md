# SETUP.md — Technical Setup & Compatibility Reference

This document covers all required systems, versions, SDK components, and configurations needed to build and run the **Astronomy Shop** Android app on a Mac.

---

# Compatibility Matrix Summary

| Component | Minimum | Recommended | Notes |
|---|---|---|---|
| macOS | 12 Monterey | 15 Sequoia | Tested on Sequoia |
| Android Studio | Hedgehog (2023.1) | Meerkat (2024.3) | For Kotlin 2.x support |
| AGP | 8.x | 9.0.1 | Must match Gradle 9.x |
| Gradle | 8.x | 9.1.0 | Auto-downloaded via wrapper |
| Kotlin | 1.9.x | 2.3.10 | Kotlin 2.x K2 compiler |
| Java source compat | 8 | 8 | Set in `compileOptions` |
| Android device | API 24 (Android 7.0) | API 35 (Android 15) | `minSdk` to `targetSdk` |

## System Requirements

### macOS

| Requirement | Value |
|---|---|
| OS | macOS 12 Monterey or later |
| Architecture | Apple Silicon (arm64) or Intel (x86_64) |
| Tested on | macOS 15 Sequoia (Darwin 25.3.0, arm64) |

---

## Development Tools

### Android Studio

| Property | Value |
|---|---|
| Recommended version | Android Studio Meerkat (2024.3.1) or later |
| Download | https://developer.android.com/studio |
| Includes | Bundled JDK, SDK Manager, Emulator, ADB |

Android Studio is the recommended IDE. It handles JDK, Gradle, and SDK management automatically.

---

### Java / JDK

| Property | Value |
|---|---|
| Source compatibility | Java 8 (`JavaVersion.VERSION_1_8`) |
| Target compatibility | Java 8 (`JavaVersion.VERSION_1_8`) |
| JVM args (Gradle daemon) | `-Xmx2048m -Dfile.encoding=UTF-8` |

The app uses **core library desugaring** (`isCoreLibraryDesugaringEnabled = true`) to backport Java 8+ APIs to older Android versions. The bundled JDK inside Android Studio is sufficient.

If using a system JDK (e.g., via Homebrew), Java 11 or 17 LTS is recommended for Gradle compatibility. Avoid Java 21+ unless Gradle 9.x compatibility is confirmed for your specific build.

To check your active Java version:

```bash
java -version
```

To use Android Studio's bundled JDK in the terminal, set:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
```

---

### Kotlin

| Property | Value |
|---|---|
| Kotlin Android plugin version | `2.3.10` |

Defined in `build.gradle.kts`:

```kotlin
id("org.jetbrains.kotlin.android") version "2.3.10" apply false
```

---

### Gradle

| Property | Value |
|---|---|
| Gradle version | 9.1.0 |
| Distribution | `gradle-9.1.0-bin.zip` (auto-downloaded via wrapper) |
| Parallel builds | Enabled (`org.gradle.parallel=true`) |
| Build cache | Enabled (`org.gradle.caching=true`) |
| JVM heap | Min 2 GB (`-Xmx2048m`) |

The Gradle wrapper (`./gradlew`) downloads Gradle automatically. No manual Gradle installation is required.

Wrapper configuration: `gradle/wrapper/gradle-wrapper.properties`

---

## Android SDK Components

Install the following via **Android Studio → SDK Manager** (or `sdkmanager` CLI):

### SDK Platforms

| Platform | API Level | Required |
|---|---|---|
| Android 15 | API 35 | Yes — `compileSdk` and `targetSdk` |
| Android 7.0 | API 24 | Yes — `minSdk` (minimum supported device) |

> The app compiles against API 35 and supports devices from API 24 (Android 7.0 Nougat) and up.

### Build Tools

| Version | Status |
|---|---|
| 35.0.0 | Recommended (matches `compileSdk 35`) |
| 36.0.0 | Optional but useful |

### Platform Tools

| Tool | Version |
|---|---|
| ADB (Android Debug Bridge) | 1.0.41 (36.0.2-14143358) |
| Location | `~/Android/sdk/platform-tools/` |

### Emulator

Required for running the app without a physical device. Install via SDK Manager → SDK Tools → Android Emulator.

---

## Android Plugin (AGP)

| Property | Value |
|---|---|
| Android Gradle Plugin (AGP) | `9.0.1` |
| Plugin ID | `com.android.application` |

Defined in root `build.gradle.kts`:

```kotlin
id("com.android.application") version "9.0.1" apply false
```

---

## App Configuration

| Property | Value |
|---|---|
| Application ID | `com.astronomyshop.app` |
| Min SDK | 24 (Android 7.0 Nougat) |
| Target SDK | 35 (Android 15) |
| Compile SDK | 35 |
| Version Code | 1 |
| Version Name | 1.0 |

---

## Key Dependencies & Versions

### AndroidX & UI

| Library | Version |
|---|---|
| `androidx.core:core-ktx` | 1.12.0 |
| `androidx.appcompat:appcompat` | 1.6.1 |
| `com.google.android.material:material` | 1.11.0 |
| `androidx.constraintlayout:constraintlayout` | 2.1.4 |
| `androidx.cardview:cardview` | 1.0.0 |
| `androidx.recyclerview:recyclerview` | 1.3.2 |
| `androidx.swiperefreshlayout:swiperefreshlayout` | 1.1.0 |

### Architecture Components

| Library | Version |
|---|---|
| `androidx.lifecycle:lifecycle-viewmodel-ktx` | 2.7.0 |
| `androidx.lifecycle:lifecycle-livedata-ktx` | 2.7.0 |
| `androidx.activity:activity-ktx` | 1.8.2 |
| `androidx.fragment:fragment-ktx` | 1.6.2 |

### Navigation Component

| Library | Version |
|---|---|
| `androidx.navigation:navigation-fragment-ktx` | 2.7.6 |
| `androidx.navigation:navigation-ui-ktx` | 2.7.6 |
| Safe Args plugin | `2.9.7` |

### Networking

| Library | Version |
|---|---|
| `com.squareup.retrofit2:retrofit` | 2.9.0 |
| `com.squareup.retrofit2:converter-gson` | 2.9.0 |
| `com.squareup.okhttp3:logging-interceptor` | 4.12.0 |
| `com.google.code.gson:gson` | 2.10.1 |

### Coroutines

| Library | Version |
|---|---|
| `org.jetbrains.kotlinx:kotlinx-coroutines-android` | 1.7.3 |

## Android Permissions

| Permission | Classification | Purpose |
|---|---|---|
| `INTERNET` | Normal | API calls and network requests |
| `ACCESS_NETWORK_STATE` | Normal | Connectivity detection |
| `ACCESS_WIFI_STATE` | Normal | Network type detection |
| `ACCESS_COARSE_LOCATION` | Dangerous | Approximate location services |
| `ACCESS_FINE_LOCATION` | Dangerous | Precise location services |
| `SYSTEM_ALERT_WINDOW` | Special | Overlay windows |
| `VIBRATE` | Normal | Haptic feedback |

---

DO — March, 2026
