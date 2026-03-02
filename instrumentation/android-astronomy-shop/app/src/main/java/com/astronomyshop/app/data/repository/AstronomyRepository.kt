package com.astronomyshop.app.data.repository

import com.astronomyshop.app.data.database.AppDatabase
import com.astronomyshop.app.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AstronomyRepository(private val database: AppDatabase) {

    // Products
    suspend fun getAllProducts(): List<Product> = withContext(Dispatchers.IO) {
        var products = database.productDao().getAllProducts()
        if (products.isEmpty()) {
            // Initialize with sample data if database is empty
            products = getSampleProducts()
            database.productDao().insertProducts(products)
        }
        products
    }

    suspend fun getProductsByCategory(category: String): List<Product> = withContext(Dispatchers.IO) {
        database.productDao().getProductsByCategory(category)
    }

    suspend fun getProductById(id: String): Product? = withContext(Dispatchers.IO) {
        database.productDao().getProductById(id)
    }

    suspend fun searchProducts(query: String): List<Product> = withContext(Dispatchers.IO) {
        database.productDao().searchProducts(query)
    }

    // Cart
    suspend fun getAllCartItems(): List<CartItem> = withContext(Dispatchers.IO) {
        database.cartDao().getAllCartItems()
    }

    suspend fun addToCart(product: Product, quantity: Int = 1): Boolean = withContext(Dispatchers.IO) {
        try {
            val existingItem = database.cartDao().getCartItemByProductId(product.id)
            if (existingItem != null) {
                val updatedItem = existingItem.copy(quantity = existingItem.quantity + quantity)
                database.cartDao().updateCartItem(updatedItem)
            } else {
                val cartItem = CartItem(
                    productId = product.id,
                    productName = product.name,
                    productPrice = product.price,
                    productImageUrl = product.imageUrl,
                    quantity = quantity
                )
                database.cartDao().insertCartItem(cartItem)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateCartItemQuantity(cartItem: CartItem, newQuantity: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            if (newQuantity <= 0) {
                database.cartDao().deleteCartItem(cartItem)
            } else {
                val updatedItem = cartItem.copy(quantity = newQuantity)
                database.cartDao().updateCartItem(updatedItem)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun removeFromCart(cartItem: CartItem): Boolean = withContext(Dispatchers.IO) {
        try {
            database.cartDao().deleteCartItem(cartItem)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun clearCart(): Boolean = withContext(Dispatchers.IO) {
        try {
            database.cartDao().clearCart()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getCartItemCount(): Int = withContext(Dispatchers.IO) {
        database.cartDao().getCartItemCount()
    }

    // Categories
    suspend fun getAllCategories(): List<Category> = withContext(Dispatchers.IO) {
        var categories = database.categoryDao().getAllCategories()
        if (categories.isEmpty()) {
            categories = getSampleCategories()
            database.categoryDao().insertCategories(categories)
        }
        categories
    }

    private fun getSampleProducts(): List<Product> {
        return listOf(
            Product(
                id = "1",
                name = "Celestron NexStar 127SLT Telescope",
                description = "Computerized telescope with fully automated GoTo mount and SkyAlign technology",
                price = 649.99,
                imageUrl = "https://images.unsplash.com/photo-1446941611757-91d2c3bd3d45?w=500&h=500&fit=crop",
                category = "Telescopes",
                brand = "Celestron",
                rating = 4.5f,
                reviewCount = 123,
                inStock = true,
                specifications = "Focal Length: 1500mm, Aperture: 127mm, Magnification: 59x-236x"
            ),
            Product(
                id = "2",
                name = "Orion SkyQuest XT8 Classic",
                description = "8-inch Dobsonian reflector telescope perfect for deep-sky observation",
                price = 399.99,
                imageUrl = "https://images.unsplash.com/photo-1502134249126-9f3755a50d78?w=500&h=500&fit=crop",
                category = "Telescopes",
                brand = "Orion",
                rating = 4.7f,
                reviewCount = 89,
                inStock = true,
                specifications = "Focal Length: 1200mm, Aperture: 203mm, F-ratio: f/5.9"
            ),
            Product(
                id = "3",
                name = "Celestron Omni 32mm Eyepiece",
                description = "High-quality Plössl eyepiece with 4-element design",
                price = 89.99,
                imageUrl = "https://images.unsplash.com/photo-1574116294792-218bec4b9c55?w=500&h=500&fit=crop",
                category = "Eyepieces",
                brand = "Celestron",
                rating = 4.3f,
                reviewCount = 45,
                inStock = true,
                specifications = "Focal Length: 32mm, Apparent FOV: 52°, Eye Relief: 20mm"
            ),
            Product(
                id = "4",
                name = "Turn Left at Orion Book",
                description = "A beginner's guide to finding and observing celestial objects",
                price = 24.99,
                imageUrl = "https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=500&h=500&fit=crop",
                category = "Books",
                brand = "Cambridge University Press",
                rating = 4.8f,
                reviewCount = 234,
                inStock = true,
                specifications = "Pages: 280, Edition: 5th, Publisher: Cambridge University Press"
            ),
            Product(
                id = "5",
                name = "Red LED Flashlight",
                description = "Astronomy red light flashlight to preserve night vision",
                price = 15.99,
                imageUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500&h=500&fit=crop",
                category = "Accessories",
                brand = "AstroGear",
                rating = 4.2f,
                reviewCount = 67,
                inStock = true,
                specifications = "LED Type: Red, Battery: 3x AAA, Runtime: 50 hours"
            ),
            Product(
                id = "6",
                name = "Star Chart Planisphere",
                description = "Rotating star chart showing the night sky for any date and time",
                price = 12.99,
                imageUrl = "https://images.unsplash.com/photo-1419242902214-272b3f66ee7a?w=500&h=500&fit=crop",
                category = "Accessories",
                brand = "SkyGuide",
                rating = 4.1f,
                reviewCount = 34,
                inStock = true,
                specifications = "Size: 11 inch diameter, Latitude: 40-50° North, Material: Cardboard"
            )
        )
    }

    private fun getSampleCategories(): List<Category> {
        return listOf(
            Category("telescopes", "Telescopes", "Explore the universe with our premium telescopes", "https://images.unsplash.com/photo-1446941611757-91d2c3bd3d45?w=300&h=200&fit=crop", 12),
            Category("eyepieces", "Eyepieces", "Enhance your viewing experience", "https://images.unsplash.com/photo-1574116294792-218bec4b9c55?w=300&h=200&fit=crop", 8),
            Category("accessories", "Accessories", "Essential astronomy accessories", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300&h=200&fit=crop", 15),
            Category("books", "Books", "Learn about astronomy and stargazing", "https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=300&h=200&fit=crop", 6),
            Category("mounts", "Mounts", "Stable mounting solutions", "https://images.unsplash.com/photo-1502134249126-9f3755a50d78?w=300&h=200&fit=crop", 4)
        )
    }
}