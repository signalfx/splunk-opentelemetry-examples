import Foundation

struct CartItem: Identifiable, Equatable {
    let id: UUID
    let product: Product
    var quantity: Int

    var subtotal: Double {
        product.price * Double(quantity)
    }

    var formattedSubtotal: String {
        String(format: "$%.2f", subtotal)
    }

    init(product: Product, quantity: Int = 1) {
        self.id = UUID()
        self.product = product
        self.quantity = quantity
    }

    static func == (lhs: CartItem, rhs: CartItem) -> Bool {
        lhs.product.id == rhs.product.id
    }
}
