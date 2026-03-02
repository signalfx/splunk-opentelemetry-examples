# AstronomyShop iOS

This iOS AstronomyShop demo application built with SwiftUI and simulates an online store for telescopes, cameras, and astronomy accessories. The app does not include any observability tooling.


## Quick Start

### Requirements

- Xcode 15.0 or later
- iOS 17.0+ deployment target
- Swift 5.9+
- macOS Sonoma or later (for development)

Review the [SETUP-GUIDE](./SETUP-GUIDE.md) for instructions on setting up your mac to run this app.

### 1. Clone the Repository

```bash
git clone <repository-url>
```

### 2. Open in Xcode

```bash
open AstronomyShop.xcodeproj
```

Or double-click the `.xcodeproj` file in Finder.

### 3. Select Target Device

1. Choose a simulator or connected device from the scheme selector
2. Recommended: iPhone 16+ Simulators

### 4. Build and Run

Press `Cmd+R` or click the Play button.

## Features

| Screen | Features |
|--------------|---------------|
| Home | Welcome banner, Category grid navigation, Featured products carousel  |
| Products | Browse by category, Search functionality  |
| Product Detail | Product information, Quantity selector, Add to cart  |
| Shopping Cart | View cart items, Adjust quantities, Empty Cart, Order summary  |
| Checkout | hipping address form, Payment information, Order review, Place order  |
| Order History | View past orders, Order status tracking  |
| Profile | Order History, Account information, Settings access  |

## Key Dependencies

This base version has no external dependencies. It uses only:

- SwiftUI (UI Framework)
- Combine (Reactive Framework)
- Foundation (Core utilities)

## Adding Splunk RUM Instrumentation

To add Splunk Real User Monitoring to this base app, see: **[iOS-Splunk-Instrumentation-Guide.md](./iOS-Splunk-Instrumentation-Guide.md)**


The guide covers:
- Adding Swift Package dependencies
- Creating configuration files
- Initializing Splunk RUM
- Custom workflow spans
- Error tracking
- Network request monitoring

---

*DO - Version 1.0 | March 2026*
