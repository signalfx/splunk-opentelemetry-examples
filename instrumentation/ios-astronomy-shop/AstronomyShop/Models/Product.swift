import Foundation

struct Product: Identifiable, Codable, Equatable {
    let id: Int
    let name: String
    let description: String
    let price: Double
    let imageUrl: String
    let category: String
    let inStock: Bool
    let rating: Double
    let reviewCount: Int

    var formattedPrice: String {
        String(format: "$%.2f", price)
    }

    static func == (lhs: Product, rhs: Product) -> Bool {
        lhs.id == rhs.id
    }
}

extension Product {
    static let sampleProducts: [Product] = [
        Product(
            id: 1,
            name: "Celestron NexStar 8SE",
            description: "Computerized telescope with fully automated GoTo mount. Perfect for beginners and intermediate astronomers.",
            price: 1399.99,
            imageUrl: "https://picsum.photos/seed/nexstar8se/400/300",
            category: "Telescopes",
            inStock: true,
            rating: 4.8,
            reviewCount: 245
        ),
        Product(
            id: 2,
            name: "ZWO ASI294MC Pro",
            description: "Color astronomy camera with cooled sensor for deep sky imaging.",
            price: 1299.00,
            imageUrl: "https://picsum.photos/seed/zwocamera/400/300",
            category: "Cameras",
            inStock: true,
            rating: 4.9,
            reviewCount: 128
        ),
        Product(
            id: 3,
            name: "Sky-Watcher EQ6-R Pro",
            description: "Professional equatorial mount for astrophotography with belt-driven motors.",
            price: 1850.00,
            imageUrl: "https://picsum.photos/seed/skywatcher/400/300",
            category: "Mounts",
            inStock: true,
            rating: 4.7,
            reviewCount: 89
        ),
        Product(
            id: 4,
            name: "Baader Planetarium Filter Set",
            description: "Complete LRGB filter set for deep sky imaging.",
            price: 549.00,
            imageUrl: "https://picsum.photos/seed/baaderfilter/400/300",
            category: "Filters",
            inStock: true,
            rating: 4.6,
            reviewCount: 56
        ),
        Product(
            id: 5,
            name: "Orion StarShoot AutoGuider",
            description: "Autoguiding camera for precise long-exposure astrophotography.",
            price: 379.99,
            imageUrl: "https://picsum.photos/seed/orionguider/400/300",
            category: "Accessories",
            inStock: false,
            rating: 4.5,
            reviewCount: 167
        ),
        Product(
            id: 6,
            name: "Explore Scientific ED127",
            description: "127mm apochromatic refractor with FCD100 glass for superior color correction.",
            price: 2199.00,
            imageUrl: "https://picsum.photos/seed/ed127apo/400/300",
            category: "Telescopes",
            inStock: true,
            rating: 4.9,
            reviewCount: 78
        ),
        Product(
            id: 7,
            name: "William Optics RedCat 51",
            description: "Compact 51mm petzval astrograph for wide-field imaging.",
            price: 1099.00,
            imageUrl: "https://picsum.photos/seed/redcat51/400/300",
            category: "Telescopes",
            inStock: true,
            rating: 4.8,
            reviewCount: 203
        ),
        Product(
            id: 8,
            name: "Celestron PowerTank Lithium",
            description: "Rechargeable lithium battery for portable telescope power.",
            price: 159.95,
            imageUrl: "https://picsum.photos/seed/powertank/400/300",
            category: "Accessories",
            inStock: true,
            rating: 4.4,
            reviewCount: 312
        )
    ]
}
