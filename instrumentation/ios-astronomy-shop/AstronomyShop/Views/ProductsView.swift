import SwiftUI

struct ProductsView: View {
    @EnvironmentObject var productViewModel: ProductViewModel
    var preselectedCategory: Category?
    var isEmbedded: Bool = false

    let columns = [
        GridItem(.flexible()),
        GridItem(.flexible())
    ]

    var body: some View {
        if isEmbedded {
            content
        } else {
            NavigationStack {
                content
            }
        }
    }

    private var content: some View {
        VStack(spacing: 0) {
            // Search Bar
            searchBar

            // Category Filter
            categoryFilter

            // Products Grid
            productsGrid
        }
        .navigationBarHidden(true)
        .onAppear {
            if let category = preselectedCategory {
                productViewModel.selectCategory(category)
            } else {
                productViewModel.clearFilters()
            }
        }
        .onDisappear {
            if preselectedCategory != nil {
                productViewModel.clearFilters()
            }
        }
    }

    private var searchBar: some View {
        HStack {
            Image(systemName: "magnifyingglass")
                .foregroundColor(.gray)

            TextField("Search products...", text: $productViewModel.searchText)
                .textFieldStyle(PlainTextFieldStyle())

            if !productViewModel.searchText.isEmpty {
                Button(action: {
                    productViewModel.searchText = ""
                }) {
                    Image(systemName: "xmark.circle.fill")
                        .foregroundColor(.gray)
                }
            }
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(10)
        .padding()
    }

    private var categoryFilter: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 10) {
                FilterChip(
                    title: "All",
                    isSelected: productViewModel.selectedCategory == nil,
                    action: { productViewModel.selectCategory(nil) }
                )

                ForEach(productViewModel.categories) { category in
                    FilterChip(
                        title: category.name,
                        isSelected: productViewModel.selectedCategory?.id == category.id,
                        action: { productViewModel.selectCategory(category) }
                    )
                }
            }
            .padding(.horizontal)
        }
        .padding(.bottom, 8)
    }

    private var productsGrid: some View {
        ScrollView {
            if productViewModel.isLoading {
                ProgressView()
                    .frame(maxWidth: .infinity)
                    .padding(.top, 50)
            } else if let error = productViewModel.errorMessage {
                errorView(error)
            } else if productViewModel.filteredProducts.isEmpty {
                emptyView
            } else {
                LazyVGrid(columns: columns, spacing: 16) {
                    ForEach(productViewModel.filteredProducts) { product in
                        NavigationLink(destination: ProductDetailView(product: product)) {
                            ProductCard(product: product)
                        }
                        .buttonStyle(PlainButtonStyle())
                    }
                }
                .padding()
            }
        }
        .refreshable {
            productViewModel.loadProducts()
        }
    }

    private func errorView(_ error: String) -> some View {
        VStack(spacing: 16) {
            Image(systemName: "wifi.slash")
                .font(.system(size: 48))
                .foregroundColor(.gray)

            Text(error)
                .multilineTextAlignment(.center)
                .foregroundColor(.gray)

            Button("Try Again") {
                productViewModel.loadProducts()
            }
            .buttonStyle(.borderedProminent)
        }
        .padding()
        .frame(maxWidth: .infinity)
        .padding(.top, 50)
    }

    private var emptyView: some View {
        VStack(spacing: 16) {
            Image(systemName: "magnifyingglass")
                .font(.system(size: 48))
                .foregroundColor(.gray)

            Text("No products found")
                .font(.headline)
                .foregroundColor(.gray)

            Text("Try adjusting your search or filters")
                .font(.subheadline)
                .foregroundColor(.gray)
        }
        .padding()
        .frame(maxWidth: .infinity)
        .padding(.top, 50)
    }
}

struct FilterChip: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.subheadline)
                .fontWeight(isSelected ? .semibold : .regular)
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .background(isSelected ? Color.blue : Color(.systemGray6))
                .foregroundColor(isSelected ? .white : .primary)
                .cornerRadius(20)
        }
    }
}

#Preview {
    ProductsView()
        .environmentObject(ProductViewModel())
        .environmentObject(CartViewModel())
}
