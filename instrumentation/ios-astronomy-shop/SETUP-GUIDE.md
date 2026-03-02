# How to Run AstronomyShop iOS on Your Mac

A step-by-step guide to launch and run the AstronomyShop iOS app on your local Mac.

---

## Prerequisites

Before starting, ensure you have:

- **macOS Sonoma 14.0** or later (recommended)
- **Xcode 15.0** or later
- At least **10GB free disk space** (for Xcode and simulators)
- Apple ID (free account works for simulator testing)

---
## Step 1: Install Xcode

### Option A: From App Store (Recommended)

1. Open the **App Store** on your Mac
2. Search for **"Xcode"**
3. Click **Get** / **Install**
4. Wait for download to complete (~7GB)

### Option B: From Developer Portal

1. Go to: https://developer.apple.com/download/
2. Sign in with your Apple ID
3. Download Xcode 15 or later
4. Open the downloaded `.xip` file
5. Drag Xcode to your Applications folder

### Verify Installation

Open Terminal and run:
```bash
xcode-select --version
```

You should see output like: `xcode-select version 2397`

---
## Step 2: Install Command Line Tools

Open Terminal and run:

```bash
xcode-select --install
```

A dialog will appear - click **Install** and wait for completion.

---
## Step 3: Accept Xcode License

Open Terminal and run:

```bash
sudo xcodebuild -license accept
```

Enter your Mac password when prompted.

---
## Step 4: Install iOS Simulator

1. Open **Xcode**
2. Go to **Xcode > Settings** (or press `Cmd + ,`)
3. Click the **Platforms** tab
4. Click the **+** button at bottom left
5. Select **iOS 17** (or latest available)
6. Click **Download & Install**
7. Wait for download (~5GB)

### Verify Simulators

```bash
xcrun simctl list devices available
```

You should see devices like "iPhone 15 Pro", "iPhone 15", etc.

---
## Step 5: Navigate to Project Directory

Open Terminal and run:

```bash
cd ~/ios-astronomy-mobile
```

Verify project files exist:

```bash
ls -la
```

You should see:
```bash
AstronomyShop.xcodeproj/
AstronomyShop/
Package.swift
README.md
```
---
## Step 5: Open Project in Xcode

### Option A: From Terminal

```bash
open AstronomyShop.xcodeproj
```

### Option B: From Finder

1. Open Finder
2. Navigate to `~/ios-astronomy-mobile`
3. Double-click `AstronomyShop.xcodeproj`

---

## Step 7: Select Target Device

### In the Xcode Toolbar:

1. Look for the device selector (shows something like "Any iOS Device")
2. Click on it to open the dropdown
3. Under **iOS Simulators**, select a device:
   - **iPhone 15 Pro** (recommended)
   - **iPhone 15**
   - **iPhone SE** (for smaller screen testing)

![Device Selector Location: Top center of Xcode window, next to the Play button]

---
## Step 8: Build and Run

### Option A: Using Xcode UI

1. Click the **Play button** (▶) in the top-left corner
   - Or press `Cmd + R`

2. Wait for:
   - Build process (first build takes 1-2 minutes)
   - Simulator to launch
   - App to install and open

---
## Troubleshooting

### Issue: "No devices registered"

**Solution:**
1. Xcode > Settings > Platforms
2. Download iOS Simulator

### Issue: Build fails with signing error

**Solution:**
1. Select project in Navigator
2. Target > Signing & Capabilities
3. Check "Automatically manage signing"
4. Select a Team (or Personal Team)
---
## Summary Checklist

- [ ] Xcode installed
- [ ] Command line tools installed
- [ ] License accepted
- [ ] iOS Simulator downloaded
- [ ] Project opened in Xcode
- [ ] Simulator device selected
- [ ] App built and running

---
*DO - Guide Version: 1.0 | March 2026*
