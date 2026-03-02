import SwiftUI

struct HomeView: View {
    @EnvironmentObject var productViewModel: ProductViewModel

    let columns = [
        GridItem(.flexible()),
        GridItem(.flexible())
    ]

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    // Welcome Banner
                    welcomeBanner

                    // Categories Section
                    categoriesSection

                    // Featured Products
                    featuredProductsSection
                }
                .padding()
            }
            .navigationBarHidden(true)
            .refreshable {
                productViewModel.loadProducts()
            }
        }
    }

    private var welcomeBanner: some View {
        ZStack {
            LinearGradient(
                colors: [.blue, .purple],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .cornerRadius(16)

            VStack(alignment: .leading, spacing: 8) {
                Text("Welcome, Astronomer!")
                    .font(.title2)
                    .fontWeight(.bold)
                    .foregroundColor(.white)

                Text("Explore the universe with our premium equipment")
                    .font(.subheadline)
                    .foregroundColor(.white.opacity(0.9))
            }
            .padding()
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .frame(height: 120)
    }

    private var categoriesSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Categories")
                .font(.title2)
                .fontWeight(.bold)

            LazyVGrid(columns: columns, spacing: 12) {
                ForEach(productViewModel.categories) { category in
                    CategoryCard(category: category)
                }
            }
        }
    }

    private var featuredProductsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Featured Products")
                .font(.title2)
                .fontWeight(.bold)

            if productViewModel.isLoading {
                ProgressView()
                    .frame(maxWidth: .infinity)
                    .padding()
            } else {
                LazyVGrid(columns: columns, spacing: 12) {
                    ForEach(productViewModel.products.prefix(4)) { product in
                        NavigationLink(destination: ProductDetailView(product: product)) {
                            ProductCard(product: product)
                        }
                        .buttonStyle(PlainButtonStyle())
                    }
                }
            }
        }
    }
}

struct CategoryCard: View {
    let category: Category
    @EnvironmentObject var productViewModel: ProductViewModel

    var body: some View {
        NavigationLink(destination: ProductsView(preselectedCategory: category, isEmbedded: true)) {
            VStack(spacing: 8) {
                Image(systemName: category.iconName)
                    .font(.system(size: 28))
                    .foregroundColor(.blue)

                Text(category.name)
                    .font(.subheadline)
                    .fontWeight(.medium)
                    .foregroundColor(.primary)
            }
            .frame(maxWidth: .infinity)
            .padding()
            .background(Color(.systemGray6))
            .cornerRadius(12)
        }
    }
}

#Preview {
    HomeView()
        .environmentObject(ProductViewModel())
        .environmentObject(CartViewModel())
}
