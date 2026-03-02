package com.astronomyshop.app.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
@Entity(tableName = "products")
data class Product(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val category: String,
    val brand: String,
    val rating: Float,
    val reviewCount: Int,
    val inStock: Boolean,
    val specifications: String? = null
) : Parcelable

@Parcelize
@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val productId: String,
    val productName: String,
    val productPrice: Double,
    val productImageUrl: String,
    val quantity: Int,
    val addedAt: Long = System.currentTimeMillis()
) : Parcelable

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val productCount: Int
)

// NEW: Order Models
@Parcelize
@Entity(tableName = "orders")
data class Order(
    @PrimaryKey
    val orderId: String = "AS${System.currentTimeMillis().toString().takeLast(8)}",
    val customerName: String,
    val customerEmail: String,
    val shippingAddress: String,
    val paymentMethod: String,
    val orderDate: Long = System.currentTimeMillis(),
    val status: OrderStatus = OrderStatus.PROCESSING,
    val subtotal: Double,
    val tax: Double,
    val shipping: Double,
    val total: Double,
    val itemCount: Int,
    val estimatedDelivery: Long,
    val trackingNumber: String? = null
) : Parcelable

@Parcelize
@Entity(tableName = "order_items")
data class OrderItem(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val orderId: String,
    val productId: String,
    val productName: String,
    val productPrice: Double,
    val productImageUrl: String,
    val quantity: Int,
    val itemTotal: Double
) : Parcelable

enum class OrderStatus {
    PROCESSING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}