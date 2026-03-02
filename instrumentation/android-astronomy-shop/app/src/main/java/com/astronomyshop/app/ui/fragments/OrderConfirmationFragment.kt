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

class OrderConfirmationFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order_confirmation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get order details from arguments
        val customerName = arguments?.getString("customerName") ?: "Customer"
        val customerEmail = arguments?.getString("customerEmail") ?: ""
        val shippingAddress = arguments?.getString("shippingAddress") ?: ""
        val orderTotal = arguments?.getString("orderTotal") ?: "$0.00"
        val itemCount = arguments?.getInt("itemCount") ?: 0
        val orderItems = arguments?.getString("orderItems") ?: ""
        val cardLastFour = arguments?.getString("cardLastFour") ?: "****"

        setupOrderConfirmation(view, customerName, customerEmail, shippingAddress, orderTotal, itemCount, orderItems, cardLastFour)

        // Clear cart since order is placed
        viewModel.clearCart()
    }

    private fun setupOrderConfirmation(
        view: View,
        customerName: String,
        customerEmail: String,
        shippingAddress: String,
        orderTotal: String,
        itemCount: Int,
        orderItems: String,
        cardLastFour: String
    ) {
        val textCustomerName = view.findViewById<TextView>(R.id.textCustomerName)
        val textOrderNumber = view.findViewById<TextView>(R.id.textOrderNumber)
        val textOrderDate = view.findViewById<TextView>(R.id.textOrderDate)
        val textOrderTotal = view.findViewById<TextView>(R.id.textOrderTotal)
        val textItemCount = view.findViewById<TextView>(R.id.textItemCount)
        val textOrderItems = view.findViewById<TextView>(R.id.textOrderItems)
        val textShippingAddress = view.findViewById<TextView>(R.id.textShippingAddress)
        val textPaymentMethod = view.findViewById<TextView>(R.id.textPaymentMethod)
        val textEstimatedDelivery = view.findViewById<TextView>(R.id.textEstimatedDelivery)
        val textConfirmationEmail = view.findViewById<TextView>(R.id.textConfirmationEmail)
        val buttonContinueShopping = view.findViewById<MaterialButton>(R.id.buttonContinueShopping)

        // Generate order details
        val orderNumber = "AS${System.currentTimeMillis().toString().takeLast(8)}"
        val currentDate = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.US).format(Date())
        val deliveryDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 3)
        }.time
        val estimatedDelivery = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(deliveryDate)

        // Set order confirmation details
        textCustomerName.text = "Thank you, $customerName!"
        textOrderNumber.text = "Order #$orderNumber"
        textOrderDate.text = "Placed on $currentDate"
        textOrderTotal.text = orderTotal
        textItemCount.text = if (itemCount == 1) "1 item" else "$itemCount items"
        textOrderItems.text = orderItems
        textShippingAddress.text = shippingAddress
        textPaymentMethod.text = "Card ending in ${cardLastFour.takeLast(4)}"
        textEstimatedDelivery.text = estimatedDelivery
        textConfirmationEmail.text = "A confirmation email has been sent to $customerEmail"

        // Setup buttons
        buttonContinueShopping.setOnClickListener {
            val navController = findNavController()
            navController.popBackStack(navController.graph.startDestinationId, false)
            navController.navigate(R.id.navigation_products)
        }

    }
}