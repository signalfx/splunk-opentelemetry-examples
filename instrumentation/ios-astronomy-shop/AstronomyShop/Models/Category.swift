import Foundation

struct Category: Identifiable, Hashable {
    let id: String
    let name: String
    let iconName: String
    let description: String

    static let allCategories: [Category] = [
        Category(
            id: "telescopes",
            name: "Telescopes",
            iconName: "scope",
            description: "Refractors, reflectors, and catadioptric telescopes"
        ),
        Category(
            id: "cameras",
            name: "Cameras",
            iconName: "camera.fill",
            description: "Astronomy cameras for imaging"
        ),
        Category(
            id: "mounts",
            name: "Mounts",
            iconName: "mount.fill",
            description: "Equatorial and alt-az mounts"
        ),
        Category(
            id: "accessories",
            name: "Accessories",
            iconName: "wrench.and.screwdriver.fill",
            description: "Power, cases, and more"
        )
    ]
}
