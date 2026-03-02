package com.astronomyshop.app.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.astronomyshop.app.R
import com.astronomyshop.app.data.models.CartItem
import java.text.NumberFormat
import java.util.*

class CartAdapter(
    private val onQuantityChanged: (CartItem, Int) -> Unit,
    private val onRemoveClick: (CartItem) -> Unit,
    private val onSaveForLater: (CartItem) -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(CartDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageProduct: ImageView = itemView.findViewById(R.id.imageProduct)
        private val buttonRemove: ImageButton = itemView.findViewById(R.id.buttonRemove)
        private val textProductName: TextView = itemView.findViewById(R.id.textProductName)
        private val textProductPrice: TextView = itemView.findViewById(R.id.textProductPrice)
        private val chipStockStatus: Chip = itemView.findViewById(R.id.chipStockStatus)
        private val buttonDecrease: ImageButton = itemView.findViewById(R.id.buttonDecrease)
        private val textQuantity: TextView = itemView.findViewById(R.id.textQuantity)
        private val buttonIncrease: ImageButton = itemView.findViewById(R.id.buttonIncrease)
        private val textTotalPrice: TextView = itemView.findViewById(R.id.textTotalPrice)
        private val textSaveForLater: TextView = itemView.findViewById(R.id.textSaveForLater)

        fun bind(cartItem: CartItem) {
            val formatter = NumberFormat.getCurrencyInstance(Locale.US)

            // Load product image
            Glide.with(itemView.context)
                .load(cartItem.productImageUrl)
                .placeholder(R.drawable.placeholder_product)
                .error(R.drawable.placeholder_product)
                .into(imageProduct)

            // Set product details
            textProductName.text = cartItem.productName
            textProductPrice.text = formatter.format(cartItem.productPrice)
            textQuantity.text = cartItem.quantity.toString()
            textTotalPrice.text = formatter.format(cartItem.productPrice * cartItem.quantity)

            // Stock status (assuming in stock for cart items)
            chipStockStatus.text = "In Stock"
            chipStockStatus.setChipBackgroundColorResource(R.color.green_50)
            chipStockStatus.setTextColor(itemView.context.getColor(R.color.green_500))

            // Quantity controls
            buttonDecrease.setOnClickListener {
                val newQuantity = cartItem.quantity - 1
                onQuantityChanged(cartItem, newQuantity)
            }

            buttonIncrease.setOnClickListener {
                val newQuantity = cartItem.quantity + 1
                onQuantityChanged(cartItem, newQuantity)
            }

            // Remove button
            buttonRemove.setOnClickListener {
                onRemoveClick(cartItem)
            }

            // Save for later
            textSaveForLater.setOnClickListener {
                onSaveForLater(cartItem)
            }

            // Enable/disable decrease button
            buttonDecrease.isEnabled = cartItem.quantity > 1
            buttonDecrease.alpha = if (cartItem.quantity > 1) 1.0f else 0.5f
        }
    }

    class CartDiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem == newItem
        }
    }
}