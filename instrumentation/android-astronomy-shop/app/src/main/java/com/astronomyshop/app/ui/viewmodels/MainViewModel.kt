package com.astronomyshop.app.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.astronomyshop.app.data.models.*
import com.astronomyshop.app.data.network.ASTRONOMY_CATEGORIES
import com.astronomyshop.app.data.network.RetrofitClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _cartItems = MutableLiveData<List<CartItem>>()
    val cartItems: LiveData<List<CartItem>> = _cartItems

    private val _cartItemCount = MutableLiveData<Int>()
    val cartItemCount: LiveData<Int> = _cartItemCount

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>> = _orders

    private val _orderItems = MutableLiveData<Map<String, List<OrderItem>>>()
    val orderItems: LiveData<Map<String, List<OrderItem>>> = _orderItems

    // In-memory storage
    private val cartItemsList = mutableListOf<CartItem>()
    private val ordersList = mutableListOf<Order>()
    private val orderItemsMap = mutableMapOf<String, List<OrderItem>>()

    // Error simulation flags
    private var simulateNetworkError = false
    private var simulatePaymentError = false
    private var simulateSlowLoading = false

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _loading.value = true
                loadProducts()
                loadCategories()
                loadCartItems()
                updateCartItemCount()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadProducts(category: String? = null) {
        viewModelScope.launch {
            try {
                _loading.value = true

                // Simulate slow loading
                if (simulateSlowLoading) {
                    delay(5000)
                }

                // Simulate network error
                if (simulateNetworkError) {
                    _loading.value = false
                    throw Exception("Network Error: Unable to connect to server")
                }

                val productList = try {
                    RetrofitClient.apiService.getProducts(limit = 15).products.map { it.toProduct() }
                } catch (e: Exception) {
                    getSampleProducts()
                }

                _products.value = if (category.isNullOrEmpty()) {
                    productList
                } else {
                    productList.filter { it.category.equals(category, ignoreCase = true) }
                }

            } catch (e: Exception) {
                _error.value = e.message
                _products.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    fun searchProducts(query: String) {
        viewModelScope.launch {
            try {
                _loading.value = true

                val results = try {
                    RetrofitClient.apiService.searchProducts(query).products.map { it.toProduct() }
                } catch (e: Exception) {
                    val allProducts = getSampleProducts()
                    allProducts.filter {
                        it.name.contains(query, ignoreCase = true) ||
                                it.description.contains(query, ignoreCase = true) ||
                                it.brand.contains(query, ignoreCase = true)
                    }
                }

                _products.value = results

            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                try { RetrofitClient.apiService.getCategories() } catch (e: Exception) { /* fallback */ }
                _categories.value = ASTRONOMY_CATEGORIES
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun loadCartItems() {
        viewModelScope.launch {
            try {
                _cartItems.value = cartItemsList.toList()
                updateCartItemCount()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun addToCart(product: Product, quantity: Int = 1) {
        viewModelScope.launch {
            try {
                // Categories loaded

                val existingItemIndex = cartItemsList.indexOfFirst { it.productId == product.id }
                if (existingItemIndex != -1) {
                    val existingItem = cartItemsList[existingItemIndex]
                    cartItemsList[existingItemIndex] = existingItem.copy(
                        quantity = existingItem.quantity + quantity
                    )
                } else {
                    val cartItem = CartItem(
                        id = "${product.id}_${System.currentTimeMillis()}",
                        productId = product.id,
                        productName = product.name,
                        productPrice = product.price,
                        productImageUrl = product.imageUrl,
                        quantity = quantity
                    )
                    cartItemsList.add(cartItem)
                }

                loadCartItems()

            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun createOrder(
        customerName: String,
        customerEmail: String,
        shippingAddress: String,
        paymentMethod: String
    ) {
        viewModelScope.launch {
            try {
                val cartItems = _cartItems.value ?: return@launch
                if (cartItems.isEmpty()) return@launch

                val subtotal = cartItems.sumOf { it.productPrice * it.quantity }
                val tax = subtotal * 0.085
                val shipping = if (subtotal >= 50.0) 0.0 else 9.99
                val total = subtotal + tax + shipping

                val order = Order(
                    customerName = customerName,
                    customerEmail = customerEmail,
                    shippingAddress = shippingAddress,
                    paymentMethod = paymentMethod,
                    subtotal = subtotal,
                    tax = tax,
                    shipping = shipping,
                    total = total,
                    itemCount = cartItems.sumOf { it.quantity },
                    estimatedDelivery = System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000),
                    trackingNumber = "TRK${System.currentTimeMillis().toString().takeLast(10)}"
                )

                val items = cartItems.map { cartItem ->
                    OrderItem(
                        orderId = order.orderId,
                        productId = cartItem.productId,
                        productName = cartItem.productName,
                        productPrice = cartItem.productPrice,
                        productImageUrl = cartItem.productImageUrl,
                        quantity = cartItem.quantity,
                        itemTotal = cartItem.productPrice * cartItem.quantity
                    )
                }

                ordersList.add(0, order)
                orderItemsMap[order.orderId] = items

                _orders.postValue(ordersList.toList())
                _orderItems.postValue(orderItemsMap.toMap())

            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Order creation failed: ${e.message}")
                _error.postValue("Order creation failed: ${e.message}")
            }
        }
    }

    // Error simulation methods - simplified tracking
    fun testNetworkErrorNow() {
        simulateNetworkError = true
        _loading.value = false
        _products.value = emptyList()
        _error.value = "NETWORK ERROR: Connection failed (Simulated)"

        android.util.Log.i("MainViewModel", "Network error simulation activated")
    }

    fun testPaymentErrorNow() {
        simulatePaymentError = true
        _error.value = "PAYMENT ERROR: Credit card declined (Simulated)"

        android.util.Log.i("MainViewModel", "Payment error simulation activated")
    }

    fun testSlowLoadingNow() {
        simulateSlowLoading = true
        android.util.Log.i("MainViewModel", "Slow loading simulation activated")
        viewModelScope.launch {
            _loading.value = true
            _error.value = "SLOW LOADING: 5-second delay simulation..."
            delay(5000)
            _loading.value = false
            _error.value = "SLOW LOADING: Completed"
        }
    }

    // Crash simulation methods - no tracking
    fun triggerCrashTest() {
        android.util.Log.w("MainViewModel", "💥 INTENTIONAL CRASH TEST")
        throw RuntimeException("INTENTIONAL CRASH: Testing crash reporting!")
    }

    fun triggerNullPointerCrash() {
        android.util.Log.w("MainViewModel", "NULL POINTER CRASH TEST")
        val nullString: String? = null
        val length = nullString!!.length
    }

    fun triggerIndexOutOfBoundsCrash() {
        android.util.Log.w("MainViewModel", "INDEX BOUNDS CRASH TEST")
        val list = listOf("test")
        val item = list[10]
    }

    fun triggerClassCastCrash() {
        android.util.Log.w("MainViewModel", "CLASS CAST CRASH TEST")
        val obj: Any = "test"
        val number = obj as Int
    }

    // Utility methods
    fun isPaymentErrorActive(): Boolean = simulatePaymentError

    fun checkPaymentError(): String? {
        return if (simulatePaymentError) {
            "Payment Failed: Credit card declined (Error Code: 4001)"
        } else null
    }

    fun clearError() {
        _error.value = null
        clearErrorSimulations()
    }

    fun clearErrorSimulations() {
        simulateNetworkError = false
        simulatePaymentError = false
        simulateSlowLoading = false
        android.util.Log.i("MainViewModel", "All error simulations cleared")
    }

    fun getCartTotal(): Double {
        return cartItemsList.sumOf { it.productPrice * it.quantity }
    }

    fun updateCartItemQuantity(cartItem: CartItem, newQuantity: Int) {
        viewModelScope.launch {
            try {
                if (newQuantity <= 0) {
                    cartItemsList.removeAll { it.id == cartItem.id }
                } else {
                    val index = cartItemsList.indexOfFirst { it.id == cartItem.id }
                    if (index != -1) {
                        cartItemsList[index] = cartItem.copy(quantity = newQuantity)
                    }
                }
                loadCartItems()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun removeFromCart(cartItem: CartItem) {
        viewModelScope.launch {
            try {
                cartItemsList.removeAll { it.id == cartItem.id }
                android.util.Log.d("MainViewModel", "Item removed: ${cartItem.productName}")
                loadCartItems()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            try {
                val itemCount = cartItemsList.sumOf { it.quantity }
                cartItemsList.clear()
                android.util.Log.d("MainViewModel", "Cart cleared: $itemCount items removed")
                loadCartItems()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    private fun updateCartItemCount() {
        viewModelScope.launch {
            try {
                val count = cartItemsList.sumOf { it.quantity }
                _cartItemCount.value = count
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun loadOrders() {
        viewModelScope.launch {
            try {
                _loading.value = true
                if (ordersList.isEmpty()) {
                    createSampleOrders()
                }
                _orders.value = ordersList.toList()
                _orderItems.value = orderItemsMap.toMap()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        viewModelScope.launch {
            try {
                val orderIndex = ordersList.indexOfFirst { it.orderId == orderId }
                if (orderIndex != -1) {
                    ordersList[orderIndex] = ordersList[orderIndex].copy(status = newStatus)
                    _orders.value = ordersList.toList()
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun getOrderById(orderId: String): Order? {
        return ordersList.find { it.orderId == orderId }
    }

    fun getOrderItems(orderId: String): List<OrderItem> {
        return orderItemsMap[orderId] ?: emptyList()
    }

    private fun createSampleOrders() {
        try {
            val sampleOrder1 = Order(
                orderId = "AS00123456",
                customerName = "Alex Johnson",
                customerEmail = "alex.johnson@email.com",
                shippingAddress = "123 Observatory Drive\nSydney, NSW 2000\nAustralia",
                paymentMethod = "**** **** **** 9012",
                orderDate = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000),
                status = OrderStatus.SHIPPED,
                subtotal = 649.99,
                tax = 55.25,
                shipping = 0.0,
                total = 705.24,
                itemCount = 1,
                estimatedDelivery = System.currentTimeMillis() + (1 * 24 * 60 * 60 * 1000),
                trackingNumber = "TRK1234567890"
            )

            val sampleItems1 = listOf(
                OrderItem(
                    orderId = "AS00123456",
                    productId = "1",
                    productName = "Celestron NexStar 127SLT Telescope",
                    productPrice = 649.99,
                    productImageUrl = "https://images.unsplash.com/photo-1446941611757-91d2c3bd3d45?w=500&h=500&fit=crop",
                    quantity = 1,
                    itemTotal = 649.99
                )
            )

            ordersList.add(sampleOrder1)
            orderItemsMap[sampleOrder1.orderId] = sampleItems1

        } catch (e: Exception) {
            android.util.Log.e("MainViewModel", "Error creating sample orders: ${e.message}")
        }
    }

    // Sample data methods
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
                imageUrl = "https://images.unsplash.com/photo-1608178398319-48f814d0750c?w=500&h=500&fit=crop&auto=format",
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
            ),
            // ADD MORE EYEPIECES
            Product(
                id = "7",
                name = "Orion 25mm Plössl Eyepiece",
                description = "Classic 4-element Plössl design with excellent color correction",
                price = 69.99,
                imageUrl = "https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=500&h=500&fit=crop&auto=format",
                category = "Eyepieces",
                brand = "Orion",
                rating = 4.4f,
                reviewCount = 78,
                inStock = true,
                specifications = "Focal Length: 25mm, Apparent FOV: 50°, Eye Relief: 17mm"
            ),
            Product(
                id = "8",
                name = "Meade Super Plössl 15mm Eyepiece",
                description = "Premium multi-coated eyepiece for crisp, clear views",
                price = 95.99,
                imageUrl = "https://images.unsplash.com/photo-1574116294792-218bec4b9c55?w=500&h=500&fit=crop&auto=format",
                category = "Eyepieces",
                brand = "Meade",
                rating = 4.6f,
                reviewCount = 92,
                inStock = true,
                specifications = "Focal Length: 15mm, Apparent FOV: 52°, Eye Relief: 12mm"
            )
        )
    }

    private fun getSampleCategories(): List<Category> {
        return listOf(
            Category("telescopes", "Telescopes", "Explore the universe with our premium telescopes",
                "https://images.unsplash.com/photo-1446941611757-91d2c3bd3d45?w=300&h=200&fit=crop", 2),
            Category("eyepieces", "Eyepieces", "Enhance your viewing experience",
                "https://images.unsplash.com/photo-1608178398319-48f814d0750c?w=300&h=200&fit=crop&auto=format", 3),
            Category("accessories", "Accessories", "Essential astronomy accessories",
                "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300&h=200&fit=crop", 2),
            Category("books", "Books", "Learn about astronomy and stargazing",
                "https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=300&h=200&fit=crop", 1),
            Category("mounts", "Mounts", "Stable mounting solutions",
                "https://images.unsplash.com/photo-1502134249126-9f3755a50d78?w=300&h=200&fit=crop", 0)
        )
    }
}