# iOS Splunk RUM Instrumentation Guide

This is a step-by-step guide for adding Splunk Real User Monitoring (RUM) to an iOS SwiftUI app.

> **SDK Version:** `SplunkAgent 2.0.6` OR latest version
> **GitHub:** [signalfx/splunk-otel-ios](https://github.com/signalfx/splunk-otel-ios)

---

## Prerequisites

| Requirement | Value |
|---|---|
| Xcode | 15.0 or later |
| iOS Deployment Target | 16.0+ |
| Splunk Observability Cloud account | Required |
| RUM Access Token | From **Settings > Access Tokens** (RUM permission) |
| Realm | Your org's realm, e.g. `us1`, `eu0` |

---

## Part 1 — Install the SDK

### Step 1: Add the Package

1. Open your `.xcodeproj` in Xcode
2. Go to **File > Add Package Dependencies...**
3. Enter the repository URL:
   ```
   https://github.com/signalfx/splunk-otel-ios
   ```
4. Set **Dependency Rule** → **Exact Version** → `2.0.6`
5. Click **Add Package**
6. In the product selection dialog, check **SplunkAgent** only
7. Click **Add Package**

### Step 2: Verify Linking

1. Select your app target in Xcode
2. Go to **Build Phases > Link Binary With Libraries**
3. Confirm **SplunkAgent** is listed
4. If missing: click **+**, search `SplunkAgent`, add it

---

## Part 2 — Configure the SDK

Create two files in `YourApp/Utils/` before instrumenting any views.

### Step 3: Create `SplunkConfiguration.swift`

This file holds all constants. No SDK import needed in this file.

```swift
import Foundation

enum SplunkConfiguration {

    // MARK: - Realm
    static let realm = "YOUR_REALM" // e.g us1

    // MARK: - RUM Access Token
    static let rumAccessToken = "YOUR_RUM_ACCESS_TOKEN_HERE"

    // MARK: - App Identity
    static let appName     = "YOUR_APP_NAME"   // e.g. <yourName>-iOS
    static let environment = "YOUR_ENV"   // "production", "staging", etc.

    static var appVersion: String {
        Bundle.main.infoDictionary?["YOUR-RUM-TOKEN"] as? String ?? "1.0.0"
    }

    // MARK: - Feature Flags
    static let sessionReplayEnabled = true  // Enables Session Replay
    static let samplingRate: Double = 1.0   // 1.0 = 100%, 0.5 = 50%

    // MARK: - Validation
    static var isValid: Bool {
        !rumAccessToken.isEmpty
            && !rumAccessToken.contains("YOUR_")
            && !rumAccessToken.contains("_HERE")
    }
}
```

**Where to get your token:**
1. Log in to [Splunk Observability Cloud](https://app.signalfx.com)
2. Go to **Settings > Access Tokens**
3. Click **Create New Token**, select **RUM** as the authorization scope
4. Copy the token into `rumAccessToken` above

---

### Step 4: Create `SplunkManager.swift`

This is the central hub for all instrumentation. Wraps every SDK call in `#if canImport(SplunkAgent)` so the app compiles and runs even without the SDK installed.

```swift
import Foundation
import Combine
import SwiftUI

#if canImport(SplunkAgent)
import SplunkAgent
import OpenTelemetryApi
import OpenTelemetrySdk
#endif

final class SplunkManager: ObservableObject {

    // MARK: - Singleton
    static let shared = SplunkManager()
    private init() {}

    // MARK: - SDK Reference
    #if canImport(SplunkAgent)
    private var agent: SplunkRum?
    #endif

    // MARK: - State
    @Published var isInitialized = false

    // MARK: - Initialization

    func initialize() {
        guard SplunkConfiguration.isValid else {
            print("[Splunk] ERROR: Token not configured — update SplunkConfiguration.swift")
            return
        }
        guard !isInitialized else { return }

        #if canImport(SplunkAgent)
        do {
            let endpoint = EndpointConfiguration(
                realm: SplunkConfiguration.realm,
                rumAccessToken: SplunkConfiguration.rumAccessToken
            )

            var config = AgentConfiguration(
                endpoint: endpoint,
                appName: SplunkConfiguration.appName,
                deploymentEnvironment: SplunkConfiguration.environment
            )

            config = config.sessionConfiguration(
                SessionConfiguration(samplingRate: SplunkConfiguration.samplingRate)
            )

            agent = try SplunkRum.install(with: config)

            // Attach global attributes to every span
            SplunkRum.shared.globalAttributes[string: "app.version"] = SplunkConfiguration.appVersion

            // Enable automatic navigation tracking
            agent?.navigation.preferences.enableAutomatedTracking = true

            isInitialized = true
            print("[Splunk] Initialized — realm: \(SplunkConfiguration.realm), app: \(SplunkConfiguration.appName)")

        } catch {
            print("[Splunk] Initialization failed: \(error.localizedDescription)")
        }
        #endif
    }
}
```

---

### Step 5: Initialize at App Launch

In your app's entry point, call `initialize()` before any view renders:

```swift
import SwiftUI

@main
struct YourApp: App {
    @StateObject private var cartViewModel    = CartViewModel()
    @StateObject private var productViewModel = ProductViewModel()

    init() {
        SplunkManager.shared.initialize()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(cartViewModel)
                .environmentObject(productViewModel)
                .environmentObject(SplunkManager.shared)  // inject for views that need it
        }
    }
}
```

> Calling `initialize()` in `init()` ensures the SDK starts before any view's `onAppear` fires.

---

## Part 3 — Session Replay

Session Replay records a visual playback of the user's session so you can see exactly what they experienced.

> **Reference:** [Session Replay for Splunk RUM](https://help.splunk.com/en/splunk-observability-cloud/monitor-end-user-experience/real-user-monitoring/replay-user-sessions/record-ios-sessions)

### Step 6: Start Session Replay

Add the following inside `SplunkManager.initialize()`, immediately after `isInitialized = true`:

```swift
#if canImport(SplunkAgent)
if SplunkConfiguration.sessionReplayEnabled {
    agent?.sessionReplay.start()
    print("[Splunk] Session Replay started")
}
#endif
```

With `sessionReplayEnabled = true` in `SplunkConfiguration.swift`, replay starts on every launch automatically.

---

### Step 7: Mask Sensitive Fields

Any field containing personal or payment data must be excluded from recordings. Add a privacy modifier to `SplunkManager.swift`:

```swift
// MARK: - Session Replay Privacy

extension View {
    /// Marks this view as sensitive — it will be excluded from Session Replay recordings.
    func sessionReplaySensitive() -> some View {
        self.modifier(SessionReplaySensitiveModifier())
    }
}

struct SessionReplaySensitiveModifier: ViewModifier {
    func body(content: Content) -> some View {
        content
        // Uncomment when the SDK exposes this modifier directly:
        // content.srSensitive()
    }
}
```

Apply it in any view with sensitive data:

```swift
// Example: CheckoutView.swift
TextField("Card Number", text: $cardNumber)
    .sessionReplaySensitive()

TextField("CVV", text: $cvv)
    .sessionReplaySensitive()
```

---

### Step 8: View Replays in Splunk

1. Go to [Splunk Observability Cloud](https://app.signalfx.com)
2. Navigate to **RUM > Session Replay**
3. Select your app
4. Filter by session, user, or time range
5. Click any session to play it back

---

## Part 4 — Workflow Tracking

Workflows track multi-step user journeys as OpenTelemetry spans (e.g. browse → add to cart → checkout).

### Step 9: Add the WorkflowSpan Wrapper Class

Add this class to `SplunkManager.swift` after the main class. It wraps an OpenTelemetry span with a safe, type-checked interface:

```swift
// MARK: - WorkflowSpan

class WorkflowSpan {
    let name: String
    private var isEnded = false

    #if canImport(SplunkAgent)
    private var span: (any OpenTelemetryApi.Span)?

    init(name: String, span: (any OpenTelemetryApi.Span)?) {
        self.name = name
        self.span = span
    }
    #else
    init(name: String) { self.name = name }
    #endif

    func setAttribute(key: String, value: String) {
        guard !isEnded else { return }
        #if canImport(SplunkAgent)
        span?.setAttribute(key: key, value: .string(value))
        #endif
    }

    func setAttribute(key: String, value: Int) {
        guard !isEnded else { return }
        #if canImport(SplunkAgent)
        span?.setAttribute(key: key, value: .int(value))
        #endif
    }

    func setAttribute(key: String, value: Double) {
        guard !isEnded else { return }
        #if canImport(SplunkAgent)
        span?.setAttribute(key: key, value: .double(value))
        #endif
    }

    func addEvent(name: String) {
        guard !isEnded else { return }
        #if canImport(SplunkAgent)
        span?.addEvent(name: name, attributes: [:])
        #endif
    }

    func end() {
        guard !isEnded else { return }
        isEnded = true
        #if canImport(SplunkAgent)
        span?.end()
        #endif
    }
}
```

---

### Step 10: Add Workflow Methods to SplunkManager

Add inside the `SplunkManager` class:

```swift
// MARK: - Workflow Tracking

/// workflow.name is required for spans to appear in Splunk RUM.
func startWorkflow(_ name: String, attributes: [String: String] = [:]) -> WorkflowSpan {
    #if canImport(SplunkAgent)
    let span = makeSpan(name: name)
    span?.setAttribute(key: "workflow.name", value: .string(name))
    for (key, value) in attributes {
        span?.setAttribute(key: key, value: .string(value))
    }
    return WorkflowSpan(name: name, span: span)
    #else
    return WorkflowSpan(name: name)
    #endif
}

// Pre-built workflow helpers
func trackProductBrowsing() -> WorkflowSpan {
    startWorkflow("ProductListLoaded")
}

func trackProductDetail(productId: Int, productName: String) -> WorkflowSpan {
    startWorkflow("ProductDetail", attributes: [
        "product.id":   String(productId),
        "product.name": productName
    ])
}

func trackAddToCart(productId: Int, productName: String, price: Double) -> WorkflowSpan {
    startWorkflow("AddToCart", attributes: [
        "product.id":    String(productId),
        "product.name":  productName,
        "product.price": String(format: "%.2f", price)
    ])
} //Add Additional Workflows as needed

// MARK: - Private Helpers

#if canImport(SplunkAgent)
private func makeSpan(name: String) -> (any OpenTelemetryApi.Span)? {
    OpenTelemetry.instance.tracerProvider
        .get(instrumentationName: "YourApp", instrumentationVersion: "1.0.0")
        .spanBuilder(spanName: name)
        .startSpan()
}
#endif
```

---

## Verifying Data in Splunk

After running the instrumented app:

1. Navigate to [Splunk Observability Cloud](https://app.signalfx.com)
2. Go to **RUM** in the left sidebar
3. Select your app name

| Splunk RUM Section | What to Look For |
|--------------------|-----------------|
| **Sessions** | Full user session timelines with all spans |
| **Session Replay** | Visual playback of the session |
| **Errors** | All `recordError` calls with type, message, and attributes |
| **Tag Spotlight** | Filter sessions by `workflow.name`, `app.version` |
| **Workflows** | AddToCart, PlaceOrder, ProductSearch |

> **Allow 2–3 minutes** after first launch for data to appear in the dashboard.

---

## Quick Reference

### Key Files

| File | Purpose |
|------|---------|
| `Utils/SplunkConfiguration.swift` | All configuration constants |
| `Utils/SplunkManager.swift` | SDK wrapper, tracking methods |
| `YourApp.swift` (entry point) | `SplunkManager.shared.initialize()` |

---

## Splunk Documentation References

| Topic | Link |
|-------|------|
| Instrument iOS RUM | [Instrument iOS applications for Splunk RUM](https://help.splunk.com/en/splunk-observability-cloud/manage-data/instrument-front-end-applications/instrument-mobile-and-web-applications-for-splunk-rum/instrument-ios-applications-for-splunk-rum) |
| Session Replay | [Replay user sessions](https://help.splunk.com/en/splunk-observability-cloud/monitor-end-user-experience/real-user-monitoring/replay-user-sessions) |
| GitHub SDK repo | [signalfx/splunk-otel-ios](https://github.com/signalfx/splunk-otel-ios) |


---

*DO - Updated February 2026 — Based on SplunkAgent 2.0.6*
