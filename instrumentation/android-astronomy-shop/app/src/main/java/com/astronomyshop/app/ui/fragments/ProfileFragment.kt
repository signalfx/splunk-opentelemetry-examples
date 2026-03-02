package com.astronomyshop.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.astronomyshop.app.R
import com.astronomyshop.app.ui.viewmodels.MainViewModel

class ProfileFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRegularClickListeners(view)
        observeViewModel()
    }

    private fun setupRegularClickListeners(view: View) {
        view.findViewById<MaterialButton>(R.id.buttonOrderHistory)?.setOnClickListener {
            findNavController().navigate(R.id.navigation_orders)
        }
    }

    private fun observeViewModel() {
        viewModel.error.observe(viewLifecycleOwner) { }
        viewModel.loading.observe(viewLifecycleOwner) { }
    }
}
