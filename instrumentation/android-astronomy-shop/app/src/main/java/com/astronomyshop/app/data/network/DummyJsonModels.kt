package com.astronomyshop.app.data.network

import com.astronomyshop.app.data.models.Category
import com.astronomyshop.app.data.models.Product

private val PRODUCT_IMAGES = listOf(
    "https://images.unsplash.com/photo-1446941611757-91d2c3bd3d45?w=500&h=500&fit=crop", // telescope
    "https://images.unsplash.com/photo-1502134249126-9f3755a50d78?w=500&h=500&fit=crop", // dobsonian
    "https://images.unsplash.com/photo-1454789548928-9efd52dc4031?w=500&h=500&fit=crop", // space optics
    "https://images.unsplash.com/photo-1419242902214-272b3f66ee7a?w=500&h=500&fit=crop", // star chart
    "https://images.unsplash.com/photo-1462331940025-496dfbfc7564?w=500&h=500&fit=crop", // nebula
    "https://images.unsplash.com/photo-1444703686981-a3abbc4d4fe3?w=500&h=500&fit=crop", // stars
    "https://images.unsplash.com/photo-1465101162946-4377e57745c3?w=500&h=500&fit=crop", // deep space
    "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=500&h=500&fit=crop", // milky way
    "https://images.unsplash.com/photo-1543722530-d2c3201371e7?w=500&h=500&fit=crop", // galaxy
    "https://images.unsplash.com/photo-1608178398319-48f814d0750c?w=500&h=500&fit=crop", // optics
    "https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=500&h=500&fit=crop", // astronomy book
    "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500&h=500&fit=crop"  // red flashlight
)

private val CATEGORY_IMAGES = listOf(
    "https://images.unsplash.com/photo-1446941611757-91d2c3bd3d45?w=300&h=200&fit=crop", // telescopes
    "https://images.unsplash.com/photo-1454789548928-9efd52dc4031?w=300&h=200&fit=crop", // eyepieces
    "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300&h=200&fit=crop", // accessories
    "https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=300&h=200&fit=crop", // books
    "https://images.unsplash.com/photo-1502134249126-9f3755a50d78?w=300&h=200&fit=crop"  // mounts
)

private data class ProductTemplate(
    val name: String,
    val description: String,
    val category: String,
    val brand: String,
    val specifications: String
)

private val ASTRONOMY_PRODUCTS = listOf(
    // Telescopes
    ProductTemplate(
        "Celestron NexStar 127SLT Telescope",
        "Computerized GoTo telescope with SkyAlign technology for quick, easy star-finding",
        "Telescopes", "Celestron",
        "Aperture: 127mm, Focal Length: 1500mm, Mount: Computerized Alt-Az"
    ),
    ProductTemplate(
        "Orion SkyQuest XT8 Classic Dobsonian",
        "8-inch reflector delivering stunning deep-sky views at an exceptional value",
        "Telescopes", "Orion",
        "Aperture: 203mm, Focal Length: 1200mm, F-ratio: f/5.9"
    ),
    ProductTemplate(
        "Sky-Watcher 10\" Collapsible Dobsonian",
        "Collapsible truss design for easy transport with impressive light-gathering power",
        "Telescopes", "Sky-Watcher",
        "Aperture: 254mm, Focal Length: 1200mm, F-ratio: f/4.7"
    ),
    // Eyepieces
    ProductTemplate(
        "Celestron Omni 32mm Plössl Eyepiece",
        "Wide-field 4-element Plössl design with excellent edge sharpness for panoramic views",
        "Eyepieces", "Celestron",
        "Focal Length: 32mm, Apparent FOV: 52°, Eye Relief: 20mm"
    ),
    ProductTemplate(
        "Orion 25mm Plössl Eyepiece",
        "Classic Plössl design delivering crisp, high-contrast views with comfortable eye relief",
        "Eyepieces", "Orion",
        "Focal Length: 25mm, Apparent FOV: 50°, Eye Relief: 17mm"
    ),
    ProductTemplate(
        "Meade Super Plössl 9mm Eyepiece",
        "Premium multi-coated high-magnification eyepiece for detailed planetary observation",
        "Eyepieces", "Meade",
        "Focal Length: 9mm, Apparent FOV: 52°, Eye Relief: 6mm"
    ),
    // Accessories
    ProductTemplate(
        "Baader Moon and Skyglow Filter",
        "Reduces light pollution and enhances contrast on emission nebulae and star clusters",
        "Accessories", "Baader Planetarium",
        "Filter Type: Broadband, Thread: 1.25\", Transmission: 90%"
    ),
    ProductTemplate(
        "Celestron Polar Alignment Scope",
        "High-precision illuminated reticle scope for accurate polar alignment of EQ mounts",
        "Accessories", "Celestron",
        "Reticle: Illuminated, Magnification: 6x, Compatibility: Standard EQ mounts"
    ),
    ProductTemplate(
        "Astrozap Flexible Dew Shield",
        "Neoprene dew shield prevents moisture build-up on corrector plates during long sessions",
        "Accessories", "Astrozap",
        "Material: Neoprene, Fits: 8\" SCT, Weight: 120g"
    ),
    // Books
    ProductTemplate(
        "Turn Left at Orion — 5th Edition",
        "The definitive beginner's guide to finding and observing over 100 celestial objects",
        "Books", "Cambridge University Press",
        "Pages: 280, Format: Spiral-bound, Edition: 5th"
    ),
    ProductTemplate(
        "NightWatch: A Practical Guide",
        "Comprehensive guide to viewing the universe featuring full-colour star charts",
        "Books", "Firefly Books",
        "Pages: 192, Format: Hardcover, Star Charts: Full-colour"
    ),
    ProductTemplate(
        "The Backyard Astronomer's Guide",
        "Essential reference covering equipment, sky tours, astrophotography, and more",
        "Books", "Firefly Books",
        "Pages: 352, Format: Hardcover, Edition: 3rd"
    ),
    // Mounts
    ProductTemplate(
        "Sky-Watcher EQ5 Equatorial Mount",
        "Sturdy manual equatorial mount ideal for visual observing and introductory imaging",
        "Mounts", "Sky-Watcher",
        "Payload: 10kg, RA Motor: Optional add-on, Dec Motor: Optional add-on"
    ),
    ProductTemplate(
        "iOptron CEM25P GoTo EQ Mount",
        "Center-balanced equatorial mount with built-in GPS, Wi-Fi, and full GoTo capability",
        "Mounts", "iOptron",
        "Payload: 11kg, GoTo: Yes, GPS: Built-in, Connectivity: Wi-Fi"
    ),
    ProductTemplate(
        "Celestron Advanced VX Mount",
        "Versatile computerized EQ mount suitable for both visual observing and astrophotography",
        "Mounts", "Celestron",
        "Payload: 13.6kg, GoTo: Yes, Guide Port: ST-4, Ports: 2x AUX"
    )
)

val ASTRONOMY_CATEGORIES = listOf(
    Category("telescopes", "Telescopes", "Explore the universe with our premium telescopes", CATEGORY_IMAGES[0], 3),
    Category("eyepieces", "Eyepieces", "Enhance your viewing experience with precision optics", CATEGORY_IMAGES[1], 3),
    Category("accessories", "Accessories", "Essential accessories for every astronomer", CATEGORY_IMAGES[2], 3),
    Category("books", "Books", "Learn and explore with astronomy literature", CATEGORY_IMAGES[3], 3),
    Category("mounts", "Mounts", "Stable mounting solutions for your telescope", CATEGORY_IMAGES[4], 3)
)

data class DummyProductsResponse(
    val products: List<DummyProduct>,
    val total: Int
)

data class DummyProduct(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    val rating: Double,
    val stock: Int,
    val brand: String?,
    val category: String,
    val thumbnail: String
) {
    fun toProduct(): Product {
        val template = ASTRONOMY_PRODUCTS[(id - 1) % ASTRONOMY_PRODUCTS.size]
        return Product(
            id = id.toString(),
            name = template.name,
            description = template.description,
            price = price,
            imageUrl = PRODUCT_IMAGES[id % PRODUCT_IMAGES.size],
            category = template.category,
            brand = template.brand,
            rating = rating.toFloat(),
            reviewCount = 0,
            inStock = stock > 0,
            specifications = template.specifications
        )
    }
}

data class DummyCategory(
    val slug: String,
    val name: String,
    val url: String
)
