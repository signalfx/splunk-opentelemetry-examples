package com.astronomyshop.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.snackbar.Snackbar
import com.astronomyshop.app.R
import com.astronomyshop.app.data.models.Product
import com.astronomyshop.app.ui.adapters.ProductAdapter
import com.astronomyshop.app.ui.viewmodels.MainViewModel

class ProductsFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var productAdapter: ProductAdapter
    private var currentCategory: String? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var editTextSearch: TextInputEditText
    private lateinit var textInputLayoutSearch: TextInputLayout
    private lateinit var chipGroup: ChipGroup
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var progressBar: ProgressBar
    private var errorSnackbar: Snackbar? = null
    private lateinit var chipAll: Chip
    private lateinit var chipTelescopes: Chip
    private lateinit var chipEyepieces: Chip
    private lateinit var chipAccessories: Chip
    private lateinit var chipBooks: Chip

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_products, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            initViews(view)
            setupRecyclerView()
            setupSearch()
            setupFilters()
            observeViewModel()
            viewModel.loadProducts(currentCategory)
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewProducts)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        editTextSearch = view.findViewById(R.id.editTextSearch)
        textInputLayoutSearch = view.findViewById(R.id.textInputLayoutSearch)
        chipGroup = view.findViewById(R.id.chipGroup)
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState)
        progressBar = view.findViewById(R.id.progressBar)
        chipAll = view.findViewById(R.id.chipAll)
        chipTelescopes = view.findViewById(R.id.chipTelescopes)
        chipEyepieces = view.findViewById(R.id.chipEyepieces)
        chipAccessories = view.findViewById(R.id.chipAccessories)
        chipBooks = view.findViewById(R.id.chipBooks)
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            onProductClick = { product ->
                navigateToProductDetail(product)
            },
            onAddToCartClick = { product ->
                viewModel.addToCart(product)
            }
        )
        recyclerView.adapter = productAdapter
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadProducts(currentCategory)
        }
    }

    private fun setupSearch() {
        editTextSearch.doOnTextChanged { text, _, _, _ ->
            text?.toString()?.let { query ->
                if (query.length >= 2) {
                    viewModel.searchProducts(query)
                } else if (query.isEmpty()) {
                    viewModel.loadProducts(currentCategory)
                }
            }
        }

        textInputLayoutSearch.setEndIconOnClickListener {
            val query = editTextSearch.text.toString()
            if (query.isNotEmpty()) {
                viewModel.searchProducts(query)
            }
        }
    }

    private fun setupFilters() {
        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val selectedCategory = when {
                checkedIds.contains(R.id.chipTelescopes) -> "Telescopes"
                checkedIds.contains(R.id.chipEyepieces) -> "Eyepieces"
                checkedIds.contains(R.id.chipAccessories) -> "Accessories"
                checkedIds.contains(R.id.chipBooks) -> "Books"
                else -> null
            }

            currentCategory = selectedCategory
            viewModel.loadProducts(selectedCategory)
        }

        when (currentCategory) {
            "Telescopes" -> chipTelescopes.isChecked = true
            "Eyepieces" -> chipEyepieces.isChecked = true
            "Accessories" -> chipAccessories.isChecked = true
            "Books" -> chipBooks.isChecked = true
            else -> chipAll.isChecked = true
        }
    }

    private fun observeViewModel() {
        viewModel.products.observe(viewLifecycleOwner) { products ->
            productAdapter.submitList(products)
            swipeRefreshLayout.isRefreshing = false

            if (products.isNotEmpty()) {
                errorSnackbar?.dismiss()
                errorSnackbar = null
            }

            if (products.isEmpty()) {
                layoutEmptyState.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                layoutEmptyState.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (!swipeRefreshLayout.isRefreshing) {
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                swipeRefreshLayout.isRefreshing = false
                errorSnackbar?.dismiss()
                errorSnackbar = Snackbar.make(requireView(), "Network error: Unable to load products", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Try Again") {
                        viewModel.clearError()
                        viewModel.loadProducts(currentCategory)
                    }
                errorSnackbar?.show()
            }
        }
    }

    private fun navigateToProductDetail(product: Product) {
        val bundle = Bundle().apply {
            putString("productId", product.id)
        }

        try {
            findNavController().navigate(R.id.productDetailFragment, bundle)
        } catch (e: Exception) {
        }
    }
}
