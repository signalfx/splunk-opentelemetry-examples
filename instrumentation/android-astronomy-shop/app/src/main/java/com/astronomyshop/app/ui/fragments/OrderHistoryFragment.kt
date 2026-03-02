package com.astronomyshop.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.astronomyshop.app.R
import com.astronomyshop.app.ui.viewmodels.MainViewModel

class OrderHistoryFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textOrderCount = view.findViewById<TextView>(R.id.textOrderCount)
        val textOrderList = view.findViewById<TextView>(R.id.textOrderList)
        val buttonContinueShopping = view.findViewById<MaterialButton>(R.id.buttonBackToProfile)

        // Load and display orders
        viewModel.loadOrders()

        viewModel.orders.observe(viewLifecycleOwner) { orders ->
            if (orders.isNotEmpty()) {
                textOrderCount.text = "You have ${orders.size} orders"

                val orderText = orders.joinToString("\n" + "─".repeat(40) + "\n\n") { order ->
                    val dateFormatter = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US)
                    val currencyFormatter = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.US)

                    "ORDER #${order.orderId}\n" +
                            "${dateFormatter.format(java.util.Date(order.orderDate))}\n" +
                            "${currencyFormatter.format(order.total)}\n" +
                            "Status: ${order.status.name}\n" +
                            "${order.itemCount} item${if (order.itemCount == 1) "" else "s"}\n" +
                            "${order.trackingNumber ?: "No tracking"}"
                }
                textOrderList.text = orderText
            } else {
                textOrderCount.text = "No orders yet"
                textOrderList.text = "No orders found.\n\nStart shopping to see your order history here!\n\nBrowse our amazing astronomy products and place your first order."
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                textOrderList.text = "Loading your orders..."
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                textOrderList.text = "Error loading orders: $it"
                viewModel.clearError()
            }
        }

        buttonContinueShopping.setOnClickListener {
            findNavController().navigate(R.id.navigation_products)
        }
    }
}