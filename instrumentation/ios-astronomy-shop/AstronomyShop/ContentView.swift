import SwiftUI

struct ContentView: View {
    @State private var selectedTab = 0
    @EnvironmentObject var cartViewModel: CartViewModel

    @State private var homeId = UUID()
    @State private var productsId = UUID()
    @State private var cartId = UUID()
    @State private var ordersId = UUID()
    @State private var profileId = UUID()

    var body: some View {
        TabView(selection: Binding(
            get: { selectedTab },
            set: { newTab in
                resetTab(newTab)
                selectedTab = newTab
            }
        )) {
            HomeView()
                .id(homeId)
                .tabItem {
                    Image(systemName: "house.fill")
                    Text("Home")
                }
                .tag(0)

            ProductsView()
                .id(productsId)
                .tabItem {
                    Image(systemName: "star.fill")
                    Text("Products")
                }
                .tag(1)

            CartView()
                .id(cartId)
                .tabItem {
                    Image(systemName: "cart.fill")
                    Text("Cart")
                }
                .badge(cartViewModel.itemCount)
                .tag(2)

            OrderHistoryView()
                .id(ordersId)
                .tabItem {
                    Image(systemName: "clock.fill")
                    Text("Orders")
                }
                .tag(3)

            ProfileView()
                .id(profileId)
                .tabItem {
                    Image(systemName: "person.fill")
                    Text("Profile")
                }
                .tag(4)
        }
        .accentColor(.blue)
        .onReceive(NotificationCenter.default.publisher(for: .continueShoppingTapped)) { _ in
            productsId = UUID()
            selectedTab = 1
        }
    }

    private func resetTab(_ tab: Int) {
        switch tab {
        case 0: homeId = UUID()
        case 1: productsId = UUID()
        case 2: cartId = UUID()
        case 3: ordersId = UUID()
        case 4: profileId = UUID()
        default: break
        }
    }
}

extension Notification.Name {
    static let continueShoppingTapped = Notification.Name("continueShoppingTapped")
}

#Preview {
    ContentView()
        .environmentObject(CartViewModel())
        .environmentObject(ProductViewModel())
        .environmentObject(OrderViewModel())
}
