import SwiftUI

@main
struct AstronomyShopApp: App {
    @StateObject private var cartViewModel = CartViewModel()
    @StateObject private var productViewModel = ProductViewModel()
    @StateObject private var orderViewModel = OrderViewModel()

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(cartViewModel)
                .environmentObject(productViewModel)
                .environmentObject(orderViewModel)
        }
    }
}
