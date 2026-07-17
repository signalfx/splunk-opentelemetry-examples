# Astronomy Shop — Splunk MRUM Instrumentation Guide

How to add **Splunk Real User Monitoring (RUM)** to the Astronomy Shop React Native app. These instructions are only to be used as a guideline - please refer to the official Splunk Observability Cloud documentation for latest instructions and latest agent versions.
> **SDK:** [`@splunk/otel-react-native`](https://github.com/signalfx/splunk-otel-react-native)  

---

## Prerequisites

| Requirement | Astronomy Shop value |
|---|---|
| React Native | 0.81.5 ✓ |
| React | 19.1.0 ✓ |
| Android | API 24+ |
| iOS | 15+; `USE_FRAMEWORKS=dynamic` in Podfile (after prebuild) |
| Splunk Observability Cloud | Account with RUM access token |
| Realm | e.g. `us0`, `us1`, `eu0` |

Create a RUM token: Splunk Observability Cloud → Settings → Access Tokens(RUM scope).

```bash
# .env.local (gitignored)
EXPO_PUBLIC_SPLUNK_REALM=your-realm
EXPO_PUBLIC_SPLUNK_RUM_TOKEN=your-rum-token
EXPO_PUBLIC_SPLUNK_APP_NAME=astronomy-shop-rn
EXPO_PUBLIC_SPLUNK_ENV=dev-astronomy-shop-rn
```
---

## Part 1 - Install the SDK

```bash
cd react-native-sample
npx expo install @splunk/otel-react-native
```

Add the Expo config plugin in `app.json`:

```json
{
  "expo": {
    "name": "AstronomyShop",
    "plugins": [
      "expo-font",
      "@splunk/otel-react-native"
    ]
  }
}
```

Generate native projects and install iOS pods:

```bash
npx expo prebuild
```

After prebuild, confirm `ios/Podfile` includes:

```ruby
ENV['USE_FRAMEWORKS'] = 'dynamic'
```

Then:

```bash
cd ios && pod install && cd ..
```

Android: ensure `minSdkVersion` is 24+ and core library desugaring is enabled in `android/app/build.gradle`

---

## Part 2 - Configuration

Create `src/config/splunkConfig.ts`:

```typescript
const token = process.env.EXPO_PUBLIC_SPLUNK_RUM_TOKEN ?? '';

export const splunkConfig = {
  endpoint: {
    realm: process.env.EXPO_PUBLIC_SPLUNK_REALM ?? 'your-realm',
    rumAccessToken: token,
  },
  appName: process.env.EXPO_PUBLIC_SPLUNK_APP_NAME ?? 'astronomy-shop-rn',
  deploymentEnvironment: process.env.EXPO_PUBLIC_SPLUNK_ENV ?? 'dev-astronomy-shop-rn',
  appVersion: '1.0.0', 
};

export const isSplunkConfigured =
  token.length > 0 && !token.includes('your-rum-token');
```

---

## Part 3 - Initialize in App.tsx

Wrap the existing provider tree with `SplunkRumProvider`. The app entry point is `App.tsx`:

```tsx
import { SplunkRumProvider } from '@splunk/otel-react-native';
import { isSplunkConfigured, splunkConfig } from './src/config/splunkConfig';

export default function App() {
  const content = (
    <GestureHandlerRootView style={styles.root}>
      {/* existing SafeAreaProvider → ErrorBoundary → PaperProvider → ShopProvider → NavigationContainer */}
    </GestureHandlerRootView>
  );

  if (!isSplunkConfigured) {
    return content;
  }

  return (
    <SplunkRumProvider agentConfiguration={splunkConfig}>
      {content}
    </SplunkRumProvider>
  );
}
```

---

## Part 4 - Custom workflows

Use `SplunkRum.instance.customTracking` for e-commerce flows. Instrument at `src/store/ShopContext.tsx` where business logic already lives.

Suggested workflows:

| Workflow name | Trigger | Suggested attributes |
|---|---|---|
| `ProductListLoaded` | `loadProducts()` success | `product.count` |
| `ProductSearch` | `searchProducts(query)` | `search.query` |
| `AddToCart` | `addToCart(product)` | `product.id`, `product.name`, `product.price` |
| `PlaceOrder` | `createOrder()` success | `order.total`, `order.item_count` |
| `PaymentError` | `checkPaymentError()` returns error | `error.type` |

Create Example helper - `src/utils/splunkTracking.ts`:

```typescript
import { SplunkRum } from '@splunk/otel-react-native';

export async function trackAddToCart(product: {
  id: string;
  name: string;
  price: number;
}) {
  await SplunkRum.instance?.customTracking.trackCustomEvent('AddToCart', {
    'product.id': product.id,
    'product.name': product.name,
    'product.price': product.price,
  });
}

export async function trackPlaceOrder(total: number, itemCount: number) {
  await SplunkRum.instance?.customTracking.trackCustomEvent('PlaceOrder', {
    'order.total': total,
    'order.item_count': itemCount,
  });
}
```

Call from `ShopContext.tsx`:

```typescript
// inside addToCart
void trackAddToCart(product);

// inside createOrder, after order is created
void trackPlaceOrder(order.total, order.itemCount);
```

For multi-step flows with duration:

```typescript
const workflow = await SplunkRum.instance.customTracking.startWorkflow('Checkout');
// ... user completes checkout ...
await workflow.end();
```

---

## Part 5 - Network and errors

**Auto-instrumented without extra code:**

- HTTP calls to `https://dummyjson.com` (`src/api/productsApi.ts`)
- Unsplash image URLs (`src/constants/productImages.ts`)
- JavaScript errors and native crashes

---

## Part 6 - Sensitive data (Checkout)

`CheckoutScreen.tsx` displays demo PII and payment fields (name, email, address, card number, CVV).

---

## Part 7 - Verify in Splunk

1. Build and run a **development build**:

```bash
npx expo run:ios
# or
npx expo run:android
```

2. Explore the app: browse products → add to cart → checkout → view orders.
3. Open Splunk Observability Cloud → **RUM** → select app `astronomy-shop-rn`.

| RUM section | What to look for |
|---|---|
| **Sessions** | Navigation across tabs and stacks |
| **Network** | Requests to `dummyjson.com` |
| **Errors** | JS errors from ErrorBoundary |
| **Tag Spotlight** | Filter by `AddToCart`, `PlaceOrder`, `ProductSearch` |

Allow **2–3 minutes** after first launch for data to appear.

---

@domuoyo-dotcom - Updated July 2026 
