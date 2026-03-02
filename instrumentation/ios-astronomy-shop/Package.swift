// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "AstronomyShop",
    platforms: [
        .iOS(.v16)
    ],
    products: [
        .library(
            name: "AstronomyShop",
            targets: ["AstronomyShop"]
        )
    ],
    dependencies: [
        // Add Splunk RUM when instrumenting:
        // .package(url: "https://github.com/signalfx/splunk-otel-swift", from: "1.0.0")
    ],
    targets: [
        .target(
            name: "AstronomyShop",
            dependencies: [],
            path: "AstronomyShop"
        )
    ]
)
