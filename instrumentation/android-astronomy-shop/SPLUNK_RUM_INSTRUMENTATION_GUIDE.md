# Splunk RUM Instrumentation Guide — Astronomy Shop Android App

This guide walks through instrumenting the Astronomy Shop Android app with **Splunk RUM (Real User Monitoring)** Version 2.1.5, including **Session Replay** and **Custom Workflows**. It covers dependencies, configuration, the Application class, manifest setup, and end-to-end workflow tracking for user journey in the app.
Check for any updates | changes | deviations to the instrumentation instructions in the [Official Splunk Documentation](https://help.splunk.com/en/splunk-observability-cloud/manage-data/instrument-front-end-applications/instrument-mobile-and-web-applications-for-splunk-rum/instrument-android-applications-for-splunk-rum#d5b7f2889ca8d4730a2a8f730128e18ce--en__rum-mobile-android)

---
## Prerequisites

- Android Studio Meerkat (2024.3+)
- Android project with **minSdk 24+**, Kotlin, Gradle (Kotlin DSL)
- A **Splunk RUM access token** from Splunk Observability → (Settings → Access Tokens → RUM access token)
- Your Splunk **realm** (e.g. `us1`)

---
## Step 1: Add Gradle Dependencies and Plugins

### 1.1 Root `build.gradle.kts`
Add these dependencies

```kotlin
plugins {
    id("com.android.application") version "9.0.1" apply false
    id("org.jetbrains.kotlin.android") version "2.3.10" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.9.7" apply false
}
```

### 1.2 App-level `app/build.gradle.kts`

**Plugins (auto-instrumentation):**

```kotlin
plugins {
    id("com.android.application")
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
    id("com.splunk.rum-okhttp3-auto-plugin") version "2.1.6"
    id("com.splunk.rum-httpurlconnection-auto-plugin") version "2.1.6"
}
```

**Dependencies:**

```kotlin
dependencies {
    // Splunk RUM SDK
    implementation("com.splunk:splunk-otel-android:2.1.5")

    // Core library desugaring (required for minSdk < 26)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
}
```
**Compile options (if using desugaring):**

```kotlin
android {
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
```

**Sync Gradle** after these changes.

---
## Step 2: Create Splunk Configuration

### 2.1 Create `SplunkConfiguration.kt`

Holds realm, environment, app name, and RUM access token.

**File:** `app/src/main/java/com/astronomyshop/app/utils/SplunkConfiguration.kt`

```kotlin
package com.astronomyshop.app.utils

object SplunkConfiguration {

    // Replace with your Splunk realm and RUM access token
    const val REALM = "YOUR_REALM"
    const val TOKEN = "YOUR_RUM_ACCESS_TOKEN"

    const val ENVIRONMENT = "YOUR_ENV"
    const val MOBILE_APP_VERSION = "1.0.0"
    const val APP_NAME = "YOUR_APP_NAME"

    val ENDPOINT_URL = "https://rum-ingest.$REALM.signalfx.com/v1/rum"

    val isValid: Boolean
        get() = TOKEN.isNotEmpty()
                && !TOKEN.contains("YOUR_")
                && !TOKEN.contains("_TOKEN_HERE")
                && REALM.isNotEmpty()
                && !REALM.contains("YOUR_")
}
```

Replace `YOUR_REALM` with your Splunk realm (e.g. `us1`) and `YOUR_RUM_ACCESS_TOKEN` with your actual token.

---
## Step 3: Create the Application Class and Install RUM

### 3.1 Create `SplunkSetup.kt`

This class extends `Application`, installs Splunk RUM, enables Session Replay, and provides helper methods for custom workflows and error recording.

**File:** `app/src/main/java/com/astronomyshop/app/SplunkSetup.kt`

```kotlin
package com.astronomyshop.app

import android.app.Application
import android.util.Log
import com.astronomyshop.app.utils.SplunkConfiguration
import com.splunk.rum.integration.agent.api.AgentConfiguration
import com.splunk.rum.integration.agent.api.EndpointConfiguration
import com.splunk.rum.integration.agent.api.SplunkRum
import com.splunk.rum.integration.agent.common.attributes.MutableAttributes
import com.splunk.rum.integration.customtracking.extension.customTracking
import com.splunk.rum.integration.sessionreplay.extension.sessionReplay
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.Span

class SplunkSetup : Application() {

    companion object {
        private const val TAG = "SplunkSetup"

        fun trackWorkflow(workflowName: String): Span? {
            return try {
                SplunkRum.instance.customTracking.trackWorkflow(workflowName)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to track workflow '$workflowName': ${e.message}")
                null
            }
        }

        fun recordError(
            errorType: String,
            errorMessage: String,
            attributes: Map<String, String> = emptyMap()
        ) {
            try {
                val span = SplunkRum.instance.customTracking.trackWorkflow(errorType)
                span?.setAttribute(AttributeKey.stringKey("error"), "true")
                span?.setAttribute(AttributeKey.stringKey("error.type"), errorType)
                span?.setAttribute(AttributeKey.stringKey("error.message"), errorMessage)
                attributes.forEach { (key, value) ->
                    span?.setAttribute(AttributeKey.stringKey(key), value)
                }
                span?.addEvent("error_recorded")
                span?.end()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to record error: ${e.message}")
            }
        }

        fun isConfigurationValid(): Boolean = SplunkConfiguration.isValid
    }

    override fun onCreate() {
        super.onCreate()
        initializeSplunkRum()
    }

    private fun initializeSplunkRum() {
        if (!SplunkConfiguration.isValid) {
            Log.w(TAG, "Update REALM and TOKEN in SplunkConfiguration.kt")
            return
        }

        try {
            val globalAttributes = MutableAttributes().apply {
                this["app.name"] = SplunkConfiguration.APP_NAME //Sample Only, Create your own global attributes
                this["app.state"] = "LocalRun"
            }

            SplunkRum.install(
                application = this,
                agentConfiguration = AgentConfiguration(
                    endpoint = EndpointConfiguration(
                        realm = SplunkConfiguration.REALM,
                        rumAccessToken = SplunkConfiguration.TOKEN
                    ),
                    appName = SplunkConfiguration.APP_NAME,
                    appVersion = SplunkConfiguration.MOBILE_APP_VERSION,
                    deploymentEnvironment = SplunkConfiguration.ENVIRONMENT,
                    globalAttributes = globalAttributes
                )
            )

            SplunkRum.instance.sessionReplay.start()

            Log.i(TAG, "Splunk RUM initialized — realm=${SplunkConfiguration.REALM}, env=${SplunkConfiguration.ENVIRONMENT}")
        } catch (e: Exception) {
            Log.e(TAG, "RUM installation failed: ${e.message}")
        }
    }
}
```
---
## Step 4: AndroidManifest and Permissions

### 4.1 Register the Application Class

In `app/src/main/AndroidManifest.xml`, set `android:name` on `<application>`:

```xml
<application
    android:name=".SplunkSetup"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:theme="@style/Theme.AstronomyShop"
    ...>
```

### 4.2 Permissions

Ensure these permissions are declared:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
```
---
## Step 5: Session Replay

Session Replay captures a visual replay of user sessions, allowing you to see exactly what users experienced. 

### 5.1 Enabling Session Replay

Session Replay is started in `SplunkSetup.initializeSplunkRum()` after RUM installation:

```kotlin
SplunkRum.instance.sessionReplay.start()
```

### 5.2 Privacy and Sensitive Data

Session Replay automatically masks text input fields. For the Checkout screen, this means card number, CVV, and expiry fields are masked in replays. No additional configuration is needed for the default masking behavior.

If you need to explicitly mask additional views:

```kotlin
// In CheckoutFragment.onViewCreated
view.findViewById<EditText>(R.id.editCardNumber)?.let {
    SplunkRum.instance.sessionReplay.addMaskedView(it)
}
```
---
## Step 6: Custom Workflow Tracking

Custom workflows track the duration and outcome of key user journeys. Start a span when the flow begins, add attributes, and end it when the flow completes.
Here's some samples for enabling this.. Add any other required workflows

### 6.1 Product Browsing Workflow

Track how long it takes to load and display products, including search and filter actions.

**In `ProductsFragment`:**

```kotlin
private var productLoadSpan: Span? = null

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    productLoadSpan = SplunkSetup.trackWorkflow("ProductList.Load")
    viewModel.loadProducts()

    viewModel.products.observe(viewLifecycleOwner) { products ->
        productLoadSpan?.setAttribute(AttributeKey.longKey("products.count"), products.size.toLong())
        productLoadSpan?.setAttribute(AttributeKey.booleanKey("products.empty"), products.isEmpty())
        productLoadSpan?.end()
        productLoadSpan = null
        // ... existing observer logic
    }
}

// Track search
private fun performSearch(query: String) {
    val searchSpan = SplunkSetup.trackWorkflow("Products.Search")
    searchSpan?.setAttribute(AttributeKey.stringKey("search.query"), query)
    viewModel.searchProducts(query)

    viewModel.products.observe(viewLifecycleOwner) { results ->
        searchSpan?.setAttribute(AttributeKey.longKey("search.results.count"), results.size.toLong())
        searchSpan?.end()
    }
}

// Track filter selection
private fun applyFilter(category: String) {
    val filterSpan = SplunkSetup.trackWorkflow("Products.Filter")
    filterSpan?.setAttribute(AttributeKey.stringKey("filter.category"), category)
    viewModel.loadProducts(category)
    filterSpan?.end()
}
```

### 6.2 Cart Workflow

Track cart interactions: quantity changes, item removal, and proceeding to checkout.

**In `CartFragment`:**

```kotlin
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val cartViewSpan = SplunkSetup.trackWorkflow("Cart.View")
    viewModel.cartItems.observe(viewLifecycleOwner) { items ->
        cartViewSpan?.setAttribute(AttributeKey.longKey("cart.item_count"), items.size.toLong())
        cartViewSpan?.setAttribute(
            AttributeKey.doubleKey("cart.total"),
            items.sumOf { it.price * it.quantity }
        )
        cartViewSpan?.end()
        // ... existing observer logic
    }
}

// Track quantity update
private fun onQuantityChanged(cartItem: CartItem, newQuantity: Int) {
    val span = SplunkSetup.trackWorkflow("Cart.UpdateQuantity")
    span?.setAttribute(AttributeKey.stringKey("product.id"), cartItem.productId)
    span?.setAttribute(AttributeKey.longKey("cart.old_quantity"), cartItem.quantity.toLong())
    span?.setAttribute(AttributeKey.longKey("cart.new_quantity"), newQuantity.toLong())
    viewModel.updateCartItemQuantity(cartItem, newQuantity)
    span?.end()
}

// Track item removal
private fun onRemoveItem(cartItem: CartItem) {
    val span = SplunkSetup.trackWorkflow("Cart.RemoveItem")
    span?.setAttribute(AttributeKey.stringKey("product.id"), cartItem.productId)
    span?.setAttribute(AttributeKey.stringKey("product.name"), cartItem.name)
    viewModel.removeFromCart(cartItem)
    span?.end()
}

// Track cart clear
private fun onClearCart() {
    val span = SplunkSetup.trackWorkflow("Cart.Clear")
    span?.setAttribute(AttributeKey.longKey("cart.items_cleared"), 
        (viewModel.cartItems.value?.size ?: 0).toLong())
    viewModel.clearCart()
    span?.end()
}

// Track checkout initiation
private fun onProceedToCheckout() {
    val span = SplunkSetup.trackWorkflow("Checkout.Initiated")
    span?.setAttribute(AttributeKey.doubleKey("cart.total"), viewModel.getCartTotal())
    span?.setAttribute(AttributeKey.longKey("cart.item_count"),
        (viewModel.cartItems.value?.size ?: 0).toLong())
    span?.end()
    findNavController().navigate(R.id.checkoutFragment)
}
```
---

## Step 7: Error Tracking

Record errors at every failure point so they appear in Splunk RUM dashboards.

### 7.1 Form Validation Errors

**In `CheckoutFragment`:**

```kotlin
private fun validateForm(): Boolean {
    val errors = mutableListOf<String>()
    if (name.isEmpty()) errors.add("name")
    if (email.isEmpty() || !email.contains("@")) errors.add("email")
    if (cardNumber.length != 16) errors.add("card_number")
    if (cvv.length !in 3..4) errors.add("cvv")

    if (errors.isNotEmpty()) {
        SplunkSetup.recordError(
            errorType = "FormValidationError",
            errorMessage = "Checkout validation failed: ${errors.joinToString(", ")}",
            attributes = mapOf(
                "validation.failed_fields" to errors.joinToString(","),
                "validation.field_count" to errors.size.toString()
            )
        )
        return false
    }
    return true
}
```
---
## Step 8: Verify Build and Run

1. **Sync Gradle** and fix any missing imports or package names.
2. **Build:** `./gradlew assembleDebug` (or build from Android Studio).
3. **Run** the app on a device or emulator.
4. In **Splunk Observability** (RUM), confirm:
   - Your app name and version appear.
   - Custom workflows appear: `ProductList.Load`, `AddToCart`,  etc.
   - Errors appear: `ValidationError`, etc.
   - Session Replay recordings are available for playback.

### Logcat Filters

```bash
~/Library/Android/sdk/platform-tools/adb logcat -s "SplunkSetup" "AstronomyShop"
```
---

DO — March, 2026
