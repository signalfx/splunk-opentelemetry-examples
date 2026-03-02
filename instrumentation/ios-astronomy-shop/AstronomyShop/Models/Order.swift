import Foundation

struct Order: Identifiable, Codable {
    let id: String
    let items: [OrderItem]
    let total: Double
    let status: OrderStatus
    let shippingAddress: ShippingAddress
    let orderDate: Date
    let estimatedDelivery: Date?

    var formattedTotal: String {
        String(format: "$%.2f", total)
    }

    var formattedOrderDate: String {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        return formatter.string(from: orderDate)
    }

    var formattedDeliveryDate: String? {
        guard let delivery = estimatedDelivery else { return nil }
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        return formatter.string(from: delivery)
    }
}

struct OrderItem: Identifiable, Codable {
    let id: Int
    let productName: String
    let quantity: Int
    let price: Double

    var formattedPrice: String {
        String(format: "$%.2f", price)
    }

    var subtotal: Double {
        price * Double(quantity)
    }

    var imageUrl: String? {
        Product.sampleProducts.first { $0.id == id }?.imageUrl
    }
}

enum OrderStatus: String, Codable, CaseIterable {
    case pending = "Pending"
    case processing = "Processing"
    case shipped = "Shipped"
    case outForDelivery = "Out for Delivery"
    case delivered = "Delivered"
    case cancelled = "Cancelled"

    var color: String {
        switch self {
        case .pending: return "orange"
        case .processing: return "blue"
        case .shipped: return "purple"
        case .outForDelivery: return "teal"
        case .delivered: return "green"
        case .cancelled: return "red"
        }
    }
}

struct ShippingAddress: Codable {
    let fullName: String
    let streetAddress: String
    let city: String
    let state: String
    let zipCode: String
    let country: String

    var formattedAddress: String {
        "\(streetAddress)\n\(city), \(state) \(zipCode)\n\(country)"
    }
}

extension Order {
    static let sampleOrders: [Order] = [
        Order(
            id: "ORD-2026-001",
            items: [
                OrderItem(id: 1, productName: "Celestron NexStar 8SE", quantity: 1, price: 1399.99),
                OrderItem(id: 8, productName: "Celestron PowerTank Lithium", quantity: 2, price: 159.95)
            ],
            total: 1719.89,
            status: .delivered,
            shippingAddress: ShippingAddress(
                fullName: "John Astronomer",
                streetAddress: "123 Star Lane",
                city: "Houston",
                state: "TX",
                zipCode: "77001",
                country: "USA"
            ),
            orderDate: Calendar.current.date(byAdding: .day, value: -14, to: Date())!,
            estimatedDelivery: Calendar.current.date(byAdding: .day, value: -7, to: Date())
        ),
        Order(
            id: "ORD-2026-002",
            items: [
                OrderItem(id: 2, productName: "ZWO ASI294MC Pro", quantity: 1, price: 1299.00)
            ],
            total: 1299.00,
            status: .shipped,
            shippingAddress: ShippingAddress(
                fullName: "John Astronomer",
                streetAddress: "123 Star Lane",
                city: "Houston",
                state: "TX",
                zipCode: "77001",
                country: "USA"
            ),
            orderDate: Calendar.current.date(byAdding: .day, value: -3, to: Date())!,
            estimatedDelivery: Calendar.current.date(byAdding: .day, value: 4, to: Date())
        )
    ]
}
