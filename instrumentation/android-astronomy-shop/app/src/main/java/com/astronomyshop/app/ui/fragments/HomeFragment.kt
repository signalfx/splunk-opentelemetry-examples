package com.astronomyshop.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.astronomyshop.app.R
import com.astronomyshop.app.data.models.Category
import com.astronomyshop.app.ui.adapters.CategoryAdapter
import com.astronomyshop.app.ui.viewmodels.MainViewModel

class HomeFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategoriesRecyclerView(view)
        observeViewModel()
        viewModel.loadProducts()
    }

    private fun setupCategoriesRecyclerView(view: View) {
        val recyclerViewCategories = view.findViewById<RecyclerView>(R.id.recyclerViewCategories)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        if (recyclerViewCategories != null) {
            categoryAdapter = CategoryAdapter { category ->
                navigateToProducts(category.name)
            }

            val displayMetrics = resources.displayMetrics
            val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
            val categorySpanCount = when {
                screenWidthDp >= 600 -> 4
                screenWidthDp >= 480 -> 3
                else -> 2
            }

            recyclerViewCategories.apply {
                adapter = categoryAdapter
                layoutManager = GridLayoutManager(context, categorySpanCount)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            if (::categoryAdapter.isInitialized) {
                categoryAdapter.submitList(categories)
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility =
                if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                viewModel.clearError()
            }
        }
    }

    private fun navigateToProducts(categoryName: String) {
        findNavController().navigate(R.id.navigation_products)
    }
}
