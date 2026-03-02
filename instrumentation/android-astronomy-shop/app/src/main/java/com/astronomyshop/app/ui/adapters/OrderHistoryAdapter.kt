package com.astronomyshop.app.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.astronomyshop.app.R
import com.astronomyshop.app.data.models.Order
import com.astronomyshop.app.data.models.OrderStatus
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class OrderHistoryAdapter(
    private val onOrderClick: (Order) -> Unit,
    private val onTrackOrderClick: (Order) -> Unit,
    private val onReorderClick: (Order) -> Unit,
    private val onCancelOrderClick: (Order) -> Unit
) : ListAdapter<Order, OrderHistoryAdapter.OrderViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_history, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textOrderNumber: TextView = itemView.findViewById(R.id.textOrderNumber)
        private val chipOrderStatus: Chip = itemView.findViewById(R.id.chipOrderStatus)
        private val textOrderDate: TextView = itemView.findViewById(R.id.textOrderDate)
        private val textOrderTotal: TextView = itemView.findViewById(R.id.textOrderTotal)
        private val textItemCount: TextView = itemView.findViewById(R.id.textItemCount)
        private val textDeliveryDate: TextView = itemView.findViewById(R.id.textDeliveryDate)
        private val textOrderItems: TextView = itemView.findViewById(R.id.textOrderItems)
        private val layoutTrackingInfo: LinearLayout = itemView.findViewById(R.id.layoutTrackingInfo)
        private val textTrackingNumber: TextView = itemView.findViewById(R.id.textTrackingNumber)
        private val buttonViewDetails: MaterialButton = itemView.findViewById(R.id.buttonViewDetails)
        private val buttonTrackOrder: MaterialButton = itemView.findViewById(R.id.buttonTrackOrder)
        private val buttonReorder: MaterialButton = itemView.findViewById(R.id.buttonReorder)

        fun bind(order: Order) {
            val formatter = NumberFormat.getCurrencyInstance(Locale.US)
            val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.US)

            // Basic order info
            textOrderNumber.text = "Order #${order.orderId}"
            textOrderTotal.text = formatter.format(order.total)
            textOrderDate.text = "Ordered on ${dateFormatter.format(Date(order.orderDate))}"
            textItemCount.text = if (order.itemCount == 1) "1 item" else "${order.itemCount} items"

            // Delivery date
            val deliveryText = when (order.status) {
                OrderStatus.DELIVERED -> "Delivered ${dateFormatter.format(Date(order.estimatedDelivery))}"
                OrderStatus.SHIPPED -> "Arriving ${dateFormatter.format(Date(order.estimatedDelivery))}"
                OrderStatus.CANCELLED -> "Order cancelled"
                else -> "Expected ${dateFormatter.format(Date(order.estimatedDelivery))}"
            }
            textDeliveryDate.text = deliveryText

            // Order status chip
            updateStatusChip(order.status)

            // Order items preview (you'll need to get this from ViewModel)
            textOrderItems.text = "Order items preview..." // Will be updated with actual items

            // Tracking info
            if (order.status == OrderStatus.SHIPPED || order.status == OrderStatus.DELIVERED) {
                layoutTrackingInfo.visibility = View.VISIBLE
                textTrackingNumber.text = "Tracking: ${order.trackingNumber}"
            } else {
                layoutTrackingInfo.visibility = View.GONE
            }

            // Click listeners
            itemView.setOnClickListener { onOrderClick(order) }
            buttonViewDetails.setOnClickListener { onOrderClick(order) }
            buttonTrackOrder.setOnClickListener { onTrackOrderClick(order) }
            buttonReorder.setOnClickListener { onReorderClick(order) }

            // Update button visibility based on status
            updateButtonVisibility(order.status)
        }

        private fun updateStatusChip(status: OrderStatus) {
            when (status) {
                OrderStatus.PROCESSING -> {
                    chipOrderStatus.text = "Processing"
                    chipOrderStatus.setChipBackgroundColorResource(R.color.blue_50)
                    chipOrderStatus.setTextColor(itemView.context.getColor(R.color.design_default_color_primary))
                }
                OrderStatus.CONFIRMED -> {
                    chipOrderStatus.text = "Confirmed"
                    chipOrderStatus.setChipBackgroundColorResource(R.color.blue_50)
                    chipOrderStatus.setTextColor(itemView.context.getColor(R.color.design_default_color_primary))
                }
                OrderStatus.SHIPPED -> {
                    chipOrderStatus.text = "Shipped"
                    chipOrderStatus.setChipBackgroundColorResource(R.color.green_50)
                    chipOrderStatus.setTextColor(itemView.context.getColor(R.color.green_500))
                }
                OrderStatus.DELIVERED -> {
                    chipOrderStatus.text = "Delivered"
                    chipOrderStatus.setChipBackgroundColorResource(R.color.green_50)
                    chipOrderStatus.setTextColor(itemView.context.getColor(R.color.green_500))
                }
                OrderStatus.CANCELLED -> {
                    chipOrderStatus.text = "Cancelled"
                    chipOrderStatus.setChipBackgroundColorResource(R.color.red_50)
                    chipOrderStatus.setTextColor(itemView.context.getColor(R.color.red_500))
                }
            }
        }

        private fun updateButtonVisibility(status: OrderStatus) {
            when (status) {
                OrderStatus.DELIVERED -> {
                    buttonTrackOrder.visibility = View.GONE
                    buttonReorder.visibility = View.VISIBLE
                }
                OrderStatus.SHIPPED -> {
                    buttonTrackOrder.visibility = View.VISIBLE
                    buttonReorder.visibility = View.VISIBLE
                }
                OrderStatus.CANCELLED -> {
                    buttonTrackOrder.visibility = View.GONE
                    buttonReorder.visibility = View.VISIBLE
                }
                else -> {
                    buttonTrackOrder.visibility = View.VISIBLE
                    buttonReorder.visibility = View.VISIBLE
                }
            }
        }
    }

    class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.orderId == newItem.orderId
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }
}