# Astronomy Shop — Android App

AstronomyShop is a sample Android application that simulates an online store for telescopes, cameras, and astronomy accessories. Built with Kotlin, Jetpack Navigation, and MVVM architecture.

---

## Features

### Navigation
Five-tab bottom navigation: **Home → Products → Cart → Orders → Profile**

| Screen | Description |
|---|---|
| **Home** | Welcome banner and category grid (Telescopes, Eyepieces, Accessories, Books, Mounts). Responsive layout adapts columns to screen width. |
| **Products** | Browsable product list with search, category filter chips, pull-to-refresh, and add-to-cart. Products are fetched from the DummyJSON API with astronomy-themed mapping. |
| **Product Detail** | Full product view with image, brand, stock status, rating, price, free shipping badge (orders $50+), description, specs, and quantity selector. |
| **Cart** | Shopping cart with quantity controls, remove/save-for-later, order summary (subtotal, 8.5% tax, shipping), free shipping at $50, 5% savings at $200, and checkout. |
| **Checkout** | Shipping and payment form with validation and pre-filled test data. Submitting places an order and navigates to confirmation. |
| **Order Confirmation** | Post-checkout summary with order number, date, total, item count, shipping address, payment details, and estimated delivery. |
| **Orders** | Order history listing all past orders with ID, date, total, status, item count, and tracking number. |
| **Profile** | User profile with quick access to Order History, Account, and Settings. |

---

## Prerequisites

| Tool | Minimum Version | Notes |
|---|---|---|
| Android Studio | Meerkat (2024.3+) | Includes bundled JDK and SDK Manager |
| Android SDK | API 35 | Install via SDK Manager |
| Java / JDK | 8+ (source compat) | Android Studio's bundled JDK is sufficient |
| Gradle | 9.1.0 | Downloaded automatically via wrapper |

> See [SETUP.md](./SETUP.md) for the full compatibility matrix, SDK components, and dependency versions.

---

## Clone the Repo / Open the Project

```bash
git clone <repository-url>
```

---

## Run via Android Studio

### Step 1: Open Project

1. Open Android Studio and select **Open Project**
2. Navigate to the `base-android-astronomy-shop` directory
3. Wait for Gradle sync to complete

### Step 2: Set Up an Emulator

```
Tools → Device Manager → Add New Device → Create Virtual Device → (Select a Device) → Next → Finish
```

Start the emulator:

```
Tools → Device Manager → (select an AVD) → click ▶ Play
```

### Step 3: Run the App

1. Select the device or emulator from the toolbar
2. Click **Run ▶** (`Control + R`)

Android Studio handles building, installing, and launching in one step.

---

## View Logs (Logcat)

From the terminal:

```bash
~/Library/Android/sdk/platform-tools/adb logcat -s "AstronomyShop"
```

From Android Studio: open the **Logcat** panel at the bottom of the IDE.

---

DO — March, 2026
