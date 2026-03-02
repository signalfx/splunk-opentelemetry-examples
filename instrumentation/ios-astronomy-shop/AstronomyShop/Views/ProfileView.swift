import SwiftUI

struct ProfileView: View {
    @EnvironmentObject var productViewModel: ProductViewModel
    @EnvironmentObject var cartViewModel: CartViewModel

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    // Profile Header
                    profileHeader

                    // Menu Options
                    menuSection

                    // App Info
                    appInfoSection
                }
                .padding()
            }
            .navigationBarHidden(true)
        }
    }

    private var profileHeader: some View {
        VStack(spacing: 12) {
            ZStack {
                Circle()
                    .fill(LinearGradient(
                        colors: [.blue, .purple],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    ))
                    .frame(width: 80, height: 80)

                Image(systemName: "person.fill")
                    .font(.system(size: 36))
                    .foregroundColor(.white)
            }

            Text("Welcome, Astronomer!")
                .font(.title2)
                .fontWeight(.bold)

            Text("Your Profile")
                .font(.subheadline)
                .foregroundColor(.gray)
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(16)
    }

    private var menuSection: some View {
        VStack(spacing: 0) {
            NavigationLink(destination: OrderHistoryView()) {
                MenuRow(
                    icon: "clock.fill",
                    title: "Order History",
                    iconColor: .blue
                )
            }

            Divider().padding(.leading, 52)

            MenuRow(
                icon: "bell.fill",
                title: "Notifications",
                iconColor: .orange
            )

            Divider().padding(.leading, 52)

            MenuRow(
                icon: "gearshape.fill",
                title: "Settings",
                iconColor: .gray
            )

            Divider().padding(.leading, 52)

            MenuRow(
                icon: "questionmark.circle.fill",
                title: "Help & Support",
                iconColor: .green
            )
        }
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }

    private var appInfoSection: some View {
        VStack(spacing: 8) {
            Text("AstronomyShop")
                .font(.headline)

            Text("Version 1.0.0")
                .font(.caption)
                .foregroundColor(.gray)

            Text("February 2026")
                .font(.caption)
                .foregroundColor(.gray)
        }
        .frame(maxWidth: .infinity)
        .padding()
    }

}

struct MenuRow: View {
    let icon: String
    let title: String
    let iconColor: Color

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.title3)
                .foregroundColor(iconColor)
                .frame(width: 28)

            Text(title)
                .foregroundColor(.primary)

            Spacer()

            Image(systemName: "chevron.right")
                .font(.caption)
                .foregroundColor(.gray)
        }
        .padding()
    }
}

#Preview {
    ProfileView()
        .environmentObject(ProductViewModel())
        .environmentObject(CartViewModel())
}
