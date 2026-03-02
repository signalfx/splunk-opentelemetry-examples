import Foundation
import Combine

@MainActor
class ProductViewModel: ObservableObject {
    @Published var products: [Product] = []
    @Published var filteredProducts: [Product] = []
    @Published var categories: [Category] = Category.allCategories
    @Published var selectedCategory: Category?
    @Published var searchText: String = ""
    @Published var isLoading: Bool = false
    @Published var errorMessage: String?

    private var cancellables = Set<AnyCancellable>()

    init() {
        setupSearchSubscription()
        loadProducts()
    }

    private func setupSearchSubscription() {
        $searchText
            .debounce(for: .milliseconds(300), scheduler: RunLoop.main)
            .sink { [weak self] searchText in
                self?.filterProducts()
            }
            .store(in: &cancellables)
    }

    func loadProducts() {
        isLoading = true
        errorMessage = nil

        // Simulate network delay
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) { [weak self] in
            self?.products = Product.sampleProducts
            self?.filterProducts()
            self?.isLoading = false
        }
    }

    func selectCategory(_ category: Category?) {
        selectedCategory = category
        filterProducts()
    }

    private func filterProducts() {
        var filtered = products

        // Filter by category
        if let category = selectedCategory {
            filtered = filtered.filter { $0.category.lowercased() == category.name.lowercased() }
        }

        // Filter by search text
        if !searchText.isEmpty {
            filtered = filtered.filter {
                $0.name.localizedCaseInsensitiveContains(searchText) ||
                $0.description.localizedCaseInsensitiveContains(searchText)
            }
        }

        filteredProducts = filtered
    }

    func clearFilters() {
        selectedCategory = nil
        searchText = ""
        filterProducts()
    }

    func getProduct(by id: Int) -> Product? {
        products.first { $0.id == id }
    }

    func getProductsByCategory(_ categoryName: String) -> [Product] {
        products.filter { $0.category.lowercased() == categoryName.lowercased() }
    }

    func clearError() {
        errorMessage = nil
    }
}
