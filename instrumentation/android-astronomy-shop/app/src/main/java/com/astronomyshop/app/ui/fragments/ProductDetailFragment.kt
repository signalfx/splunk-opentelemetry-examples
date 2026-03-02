package com.astronomyshop.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.astronomyshop.app.R
import com.astronomyshop.app.data.models.Product
import com.astronomyshop.app.ui.viewmodels.MainViewModel
import java.text.NumberFormat
import java.util.*
import android.widget.*
import com.google.android.material.button.MaterialButton

class ProductDetailFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private var currentProduct: Product? = null
    private var quantity = 1
    private var isFavorite = false

    private lateinit var imageProduct: ImageView
    private lateinit var fabFavorite: FloatingActionButton
    private lateinit var textProductName: TextView
    private lateinit var textBrand: TextView
    private lateinit var chipStockStatus: Chip
    private lateinit var ratingBar: RatingBar
    private lateinit var textRating: TextView
    private lateinit var textReviewCount: TextView
    private lateinit var textViewReviews: TextView
    private lateinit var textPrice: TextView
    private lateinit var textFreeShipping: TextView
    private lateinit var textDescription: TextView
    private lateinit var textSpecifications: TextView
    private lateinit var textQuantity: TextView
    private lateinit var buttonDecreaseQuantity: MaterialButton
    private lateinit var buttonIncreaseQuantity: MaterialButton
    private lateinit var buttonAddToCart: MaterialButton
    private lateinit var buttonBuyNow: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupClickListeners()

        val productId = arguments?.getString("productId")
        if (productId != null) {
            loadProductDetails(productId)
        } else {
            showError("Product not found")
        }
    }

    private fun initViews(view: View) {
        imageProduct = view.findViewById(R.id.imageProduct)
        fabFavorite = view.findViewById(R.id.fabFavorite)
        textProductName = view.findViewById(R.id.textProductName)
        textBrand = view.findViewById(R.id.textBrand)
        chipStockStatus = view.findViewById(R.id.chipStockStatus)
        ratingBar = view.findViewById(R.id.ratingBar)
        textRating = view.findViewById(R.id.textRating)
        textReviewCount = view.findViewById(R.id.textReviewCount)
        textViewReviews = view.findViewById(R.id.textViewReviews)
        textPrice = view.findViewById(R.id.textPrice)
        textFreeShipping = view.findViewById(R.id.textFreeShipping)
        textDescription = view.findViewById(R.id.textDescription)
        textSpecifications = view.findViewById(R.id.textSpecifications)
        textQuantity = view.findViewById(R.id.textQuantity)
        buttonDecreaseQuantity = view.findViewById(R.id.buttonDecreaseQuantity)
        buttonIncreaseQuantity = view.findViewById(R.id.buttonIncreaseQuantity)
        buttonAddToCart = view.findViewById(R.id.buttonAddToCart)
        buttonBuyNow = view.findViewById(R.id.buttonBuyNow)
    }

    private fun setupClickListeners() {
        fabFavorite.setOnClickListener {
            toggleFavorite()
        }

        buttonDecreaseQuantity.setOnClickListener {
            if (quantity > 1) {
                quantity--
                updateQuantityDisplay()
            }
        }

        buttonIncreaseQuantity.setOnClickListener {
            quantity++
            updateQuantityDisplay()
        }

        buttonAddToCart.setOnClickListener {
            addToCart()
        }

        buttonBuyNow.setOnClickListener {
            buyNow()
        }

        textViewReviews.setOnClickListener {
            viewReviews()
        }
    }

    private fun loadProductDetails(productId: String) {
        viewModel.products.observe(viewLifecycleOwner) { products ->
            val product = products.find { it.id == productId }
            if (product != null) {
                currentProduct = product
                displayProductDetails(product)
            } else {
                showError("Product not found")
            }
        }

        if (viewModel.products.value.isNullOrEmpty()) {
            viewModel.loadProducts()
        }
    }

    private fun displayProductDetails(product: Product) {
        Glide.with(this)
            .load(product.imageUrl)
            .placeholder(R.drawable.placeholder_product)
            .error(R.drawable.error_product)
            .into(imageProduct)

        textProductName.text = product.name
        textBrand.text = "by ${product.brand}"
        textDescription.text = product.description

        val formatter = NumberFormat.getCurrencyInstance(Locale.US)
        textPrice.text = formatter.format(product.price)

        textFreeShipping.visibility = if (product.price >= 50) View.VISIBLE else View.GONE

        updateStockStatus(product)

        ratingBar.rating = product.rating
        textRating.text = String.format("%.1f", product.rating)
        textReviewCount.text = "(${product.reviewCount} reviews)"

        textSpecifications.text = formatSpecifications(product.specifications)

        updateQuantityDisplay()
        updateButtonStates(product)
    }

    private fun updateStockStatus(product: Product) {
        if (product.inStock) {
            chipStockStatus.text = "In Stock"
            chipStockStatus.setChipBackgroundColorResource(R.color.green_50)
            chipStockStatus.setChipStrokeColorResource(R.color.green_500)
            chipStockStatus.setTextColor(requireContext().getColor(R.color.green_500))
        } else {
            chipStockStatus.text = "Out of Stock"
            chipStockStatus.setChipBackgroundColorResource(R.color.red_50)
            chipStockStatus.setChipStrokeColorResource(R.color.red_500)
            chipStockStatus.setTextColor(requireContext().getColor(R.color.red_500))
        }
    }

    private fun formatSpecifications(specs: String?): String {
        return if (specs.isNullOrEmpty()) {
            "No specifications available"
        } else {
            specs.split(",").joinToString("\n") { "• ${it.trim()}" }
        }
    }

    private fun updateQuantityDisplay() {
        textQuantity.text = quantity.toString()
        buttonDecreaseQuantity.isEnabled = quantity > 1

        val primaryColor = requireContext().getColor(R.color.design_default_color_primary)
        val disabledColor = requireContext().getColor(android.R.color.darker_gray)

        buttonDecreaseQuantity.setTextColor(if (quantity > 1) primaryColor else disabledColor)
    }

    private fun updateButtonStates(product: Product) {
        val inStock = product.inStock
        buttonAddToCart.isEnabled = inStock
        buttonBuyNow.isEnabled = inStock

        if (!inStock) {
            buttonAddToCart.text = "Out of Stock"
            buttonBuyNow.text = "Unavailable"
        }
    }

    private fun toggleFavorite() {
        isFavorite = !isFavorite

        val iconRes = if (isFavorite) R.drawable.ic_favorite_24 else R.drawable.ic_favorite_border_24
        fabFavorite.setImageResource(iconRes)
    }

    private fun addToCart() {
        currentProduct?.let { product ->
            viewModel.addToCart(product, quantity)

            view?.let { v ->
                com.google.android.material.snackbar.Snackbar.make(
                    v,
                    "Added to cart successfully",
                    com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                ).setAction("View Cart") {
                    findNavController().navigate(R.id.navigation_cart)
                }.show()
            }
        }
    }

    private fun buyNow() {
        currentProduct?.let { product ->
            viewModel.addToCart(product, quantity)
            findNavController().navigate(R.id.navigation_cart)
        }
    }

    private fun viewReviews() {
    }

    private fun showError(message: String) {
        textProductName.text = "Error"
        textDescription.text = message
        textPrice.text = "N/A"

        buttonAddToCart.isEnabled = false
        buttonBuyNow.isEnabled = false
        fabFavorite.isEnabled = false
    }
}
