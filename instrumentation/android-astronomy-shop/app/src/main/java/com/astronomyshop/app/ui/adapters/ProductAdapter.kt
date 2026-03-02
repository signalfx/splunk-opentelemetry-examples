package com.astronomyshop.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.astronomyshop.app.R
import com.astronomyshop.app.data.models.Product
import com.astronomyshop.app.databinding.ItemProductBinding
import java.text.NumberFormat
import java.util.*

class ProductAdapter(
    private val onProductClick: (Product) -> Unit,
    private val onAddToCartClick: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductViewHolder(
        private val binding: ItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.apply {
                textProductName.text = product.name
                textProductPrice.text = NumberFormat.getCurrencyInstance(Locale.US)
                    .format(product.price)
                textProductBrand.text = product.brand
                ratingBar.rating = product.rating
                textReviewCount.text = "(${product.reviewCount})"

                // Stock status
                if (product.inStock) {
                    textStockStatus.text = "In Stock"
                    textStockStatus.setTextColor(
                        itemView.context.getColor(R.color.green_500)
                    )
                    buttonAddToCart.isEnabled = true
                } else {
                    textStockStatus.text = "Out of Stock"
                    textStockStatus.setTextColor(
                        itemView.context.getColor(R.color.red_500)
                    )
                    buttonAddToCart.isEnabled = false
                }

                // Load product image
                Glide.with(itemView.context)
                    .load(product.imageUrl)
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.placeholder_product)
                    .into(imageProduct)

                // Click listeners
                root.setOnClickListener { onProductClick(product) }
                buttonAddToCart.setOnClickListener { onAddToCartClick(product) }
            }
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}