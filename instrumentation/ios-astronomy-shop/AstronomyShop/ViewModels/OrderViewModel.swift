import Foundation
import Combine

@MainActor
class OrderViewModel: ObservableObject {
    @Published var orders: [Order] = []
    @Published var currentOrder: Order?
    @Published var isLoading: Bool = false
    @Published var errorMessage: String?
    @Published var orderPlaced: Bool = false

    // Shipping form fields
    @Published var fullName: String = "John Astronomer"
    @Published var streetAddress: String = "123 Star Lane"
    @Published var city: String = "Houston"
    @Published var state: String = "TX"
    @Published var zipCode: String = "77001"
    @Published var country: String = "USA"

    // Payment form fields
    @Published var cardNumber: String = "4242424242424242"
    @Published var expiryDate: String = "12/27"
    @Published var cvv: String = "123"

    var isShippingValid: Bool {
        !fullName.isEmpty &&
        !streetAddress.isEmpty &&
        !city.isEmpty &&
        !state.isEmpty &&
        zipCode.count >= 5
    }

    var isPaymentValid: Bool {
        cardNumber.count >= 15 &&
        expiryDate.count >= 4 &&
        cvv.count >= 3
    }

    var canPlaceOrder: Bool {
        isShippingValid && isPaymentValid
    }

    init() {
        loadOrders()
    }

    func loadOrders() {
        isLoading = true
        errorMessage = nil

        // Simulate network delay
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) { [weak self] in
            self?.orders = Order.sampleOrders
            self?.isLoading = false
        }
    }

    func placeOrder(cartItems: [CartItem], total: Double) {
        guard canPlaceOrder else {
            errorMessage = "Please complete all required fields"
            return
        }

        isLoading = true
        errorMessage = nil

        // Simulate order processing
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) { [weak self] in
            guard let self = self else { return }

            let orderItems = cartItems.map { item in
                OrderItem(
                    id: item.product.id,
                    productName: item.product.name,
                    quantity: item.quantity,
                    price: item.product.price
                )
            }

            let shippingAddress = ShippingAddress(
                fullName: self.fullName,
                streetAddress: self.streetAddress,
                city: self.city,
                state: self.state,
                zipCode: self.zipCode,
                country: self.country
            )

            let newOrder = Order(
                id: "ORD-2026-\(String(format: "%03d", self.orders.count + 1))",
                items: orderItems,
                total: total,
                status: .pending,
                shippingAddress: shippingAddress,
                orderDate: Date(),
                estimatedDelivery: Calendar.current.date(byAdding: .day, value: 7, to: Date())
            )

            self.currentOrder = newOrder
            self.orders.insert(newOrder, at: 0)
            self.orderPlaced = true
            self.isLoading = false
            self.clearForm()
        }
    }

    func getOrder(by id: String) -> Order? {
        orders.first { $0.id == id }
    }

    func clearForm() {
        fullName = ""
        streetAddress = ""
        city = ""
        state = ""
        zipCode = ""
        cardNumber = ""
        expiryDate = ""
        cvv = ""
    }

    func resetOrderPlaced() {
        orderPlaced = false
        currentOrder = nil
    }

    func clearError() {
        errorMessage = nil
    }
}
