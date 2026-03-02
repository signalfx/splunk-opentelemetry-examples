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
import java.text.SimpleDateFormat
import java.util.*

class OrderDetailsFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val orderTotal = arguments?.getString("orderTotal") ?: "$0.00"
        val itemCount = arguments?.getInt("itemCount") ?: 0
        val orderItems = arguments?.getString("orderItems") ?: ""

        setupOrderDetails(view, orderTotal, itemCount, orderItems)
    }

    private fun setupOrderDetails(view: View, orderTotal: String, itemCount: Int, orderItems: String) {
        val textOrderNumber = view.findViewById<TextView>(R.id.textOrderNumber)
        val textOrderDate = view.findViewById<TextView>(R.id.textOrderDate)
        val textOrderTotal = view.findViewById<TextView>(R.id.textOrderTotal)
        val textItemCount = view.findViewById<TextView>(R.id.textItemCount)
        val textOrderItems = view.findViewById<TextView>(R.id.textOrderItems)
        val textEstimatedDelivery = view.findViewById<TextView>(R.id.textEstimatedDelivery)
        val buttonContinueShopping = view.findViewById<MaterialButton>(R.id.buttonContinueShopping)
        val buttonViewOrders = view.findViewById<MaterialButton>(R.id.buttonViewOrders)

        // Generate order details
        val orderNumber = "AS${System.currentTimeMillis().toString().takeLast(8)}"
        val currentDate = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date())
        val deliveryDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 3)
        }.time
        val estimatedDelivery = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(deliveryDate)

        // Set order details
        textOrderNumber.text = "Order #$orderNumber"
        textOrderDate.text = "Placed on $currentDate"
        textOrderTotal.text = orderTotal
        textItemCount.text = if (itemCount == 1) "1 item" else "$itemCount items"
        textOrderItems.text = orderItems
        textEstimatedDelivery.text = "Estimated delivery: $estimatedDelivery"

        // Setup buttons
        buttonContinueShopping.setOnClickListener {
            // Clear cart and go to products
            viewModel.clearCart()
            findNavController().navigate(R.id.navigation_products)
        }

        buttonViewOrders.setOnClickListener {
            // Navigate to order history (for now, go to profile)
            findNavController().navigate(R.id.navigation_profile)
        }
    }
}