import Foundation
import Combine

@MainActor
class CartViewModel: ObservableObject {
    @Published var items: [CartItem] = []
    @Published var isLoading: Bool = false
    @Published var errorMessage: String?

    var itemCount: Int {
        items.reduce(0) { $0 + $1.quantity }
    }

    var subtotal: Double {
        items.reduce(0) { $0 + $1.subtotal }
    }

    var tax: Double {
        subtotal * 0.0825 // 8.25% tax
    }

    var shipping: Double {
        subtotal >= 100 ? 0 : 9.99
    }

    var total: Double {
        subtotal + tax + shipping
    }

    var formattedSubtotal: String {
        String(format: "$%.2f", subtotal)
    }

    var formattedTax: String {
        String(format: "$%.2f", tax)
    }

    var formattedShipping: String {
        shipping == 0 ? "FREE" : String(format: "$%.2f", shipping)
    }

    var formattedTotal: String {
        String(format: "$%.2f", total)
    }

    var isEmpty: Bool {
        items.isEmpty
    }

    func addToCart(_ product: Product, quantity: Int = 1) {
        if let index = items.firstIndex(where: { $0.product.id == product.id }) {
            items[index].quantity += quantity
        } else {
            items.append(CartItem(product: product, quantity: quantity))
        }
    }

    func removeFromCart(_ item: CartItem) {
        items.removeAll { $0.product.id == item.product.id }
    }

    func updateQuantity(for item: CartItem, quantity: Int) {
        if let index = items.firstIndex(where: { $0.product.id == item.product.id }) {
            if quantity <= 0 {
                items.remove(at: index)
            } else {
                items[index].quantity = quantity
            }
        }
    }

    func incrementQuantity(for item: CartItem) {
        if let index = items.firstIndex(where: { $0.product.id == item.product.id }) {
            items[index].quantity += 1
        }
    }

    func decrementQuantity(for item: CartItem) {
        if let index = items.firstIndex(where: { $0.product.id == item.product.id }) {
            if items[index].quantity > 1 {
                items[index].quantity -= 1
            } else {
                items.remove(at: index)
            }
        }
    }

    func clearCart() {
        items.removeAll()
    }

    func containsProduct(_ product: Product) -> Bool {
        items.contains { $0.product.id == product.id }
    }

    func getQuantity(for product: Product) -> Int {
        items.first { $0.product.id == product.id }?.quantity ?? 0
    }

    func clearError() {
        errorMessage = nil
    }
}
