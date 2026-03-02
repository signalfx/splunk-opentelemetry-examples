package com.astronomyshop.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.astronomyshop.app.R

class TrackingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tracking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val orderId = arguments?.getString("orderId") ?: "Unknown"
        val trackingNumber = arguments?.getString("trackingNumber") ?: "No tracking"

        view.findViewById<TextView>(R.id.textTrackingTitle).text = "Tracking Order #$orderId"
        view.findViewById<TextView>(R.id.textTrackingNumber).text = "Tracking Number: $trackingNumber"

        view.findViewById<MaterialButton>(R.id.buttonBackToOrders).setOnClickListener {
            findNavController().navigate(R.id.navigation_orders)
        }
    }
}