package com.astronomyshop.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.astronomyshop.app.R
import com.astronomyshop.app.data.models.CartItem
import com.astronomyshop.app.ui.adapters.CartAdapter
import com.astronomyshop.app.ui.viewmodels.MainViewModel
import java.text.NumberFormat
import java.util.*

class CartFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        viewModel.loadCartItems()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadCartItems()
        view?.findViewById<FrameLayout>(R.id.loadingOverlay)?.visibility = View.GONE
    }

    private fun setupViews(view: View) {
        val recyclerViewCart = view.findViewById<RecyclerView>(R.id.recyclerViewCart)
        val layoutEmptyCart = view.findViewById<LinearLayout>(R.id.layoutEmptyCart)
        val buttonContinueShopping = view.findViewById<MaterialButton>(R.id.buttonContinueShopping)
        val checkoutCard = view.findViewById<View>(R.id.checkoutCard)
        val textSubtotal = view.findViewById<TextView>(R.id.textSubtotal)
        val textTax = view.findViewById<TextView>(R.id.textTax)
        val textShipping = view.findViewById<TextView>(R.id.textShipping)
        val textTotal = view.findViewById<TextView>(R.id.textTotal)
        val buttonClearCart = view.findViewById<MaterialButton>(R.id.buttonClearCart)
        val buttonCheckout = view.findViewById<MaterialButton>(R.id.buttonCheckout)
        val textCartItemCount = view.findViewById<TextView>(R.id.textCartItemCount)
        val layoutSavings = view.findViewById<View>(R.id.layoutSavings)
        val textSavings = view.findViewById<TextView>(R.id.textSavings)

        cartAdapter = CartAdapter(
            onQuantityChanged = { cartItem, newQuantity ->
                viewModel.updateCartItemQuantity(cartItem, newQuantity)
                if (newQuantity == 0) {
                    Snackbar.make(view, "Item removed", Snackbar.LENGTH_SHORT).show()
                }
            },
            onRemoveClick = { cartItem ->
                viewModel.removeFromCart(cartItem)
                Snackbar.make(view, "Item removed from cart", Snackbar.LENGTH_SHORT).show()
            },
            onSaveForLater = { cartItem ->
                Snackbar.make(view, "Saved for later", Snackbar.LENGTH_SHORT).show()
            }
        )

        recyclerViewCart.apply {
            adapter = cartAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        buttonContinueShopping.setOnClickListener {
            val navController = findNavController()
            navController.popBackStack(navController.graph.startDestinationId, false)
            navController.navigate(R.id.navigation_products)
        }

        buttonClearCart.setOnClickListener {
            viewModel.clearCart()
        }

        buttonCheckout.setOnClickListener {
            val cartItems = viewModel.cartItems.value
            if (!cartItems.isNullOrEmpty()) {
                findNavController().navigate(R.id.checkoutFragment)
            }
        }

        viewModel.cartItems.observe(viewLifecycleOwner) { cartItems ->
            val itemCount = cartItems.sumOf { it.quantity }
            textCartItemCount.text = if (itemCount == 1) "1 item" else "$itemCount items"

            cartAdapter.submitList(cartItems)

            if (cartItems.isEmpty()) {
                layoutEmptyCart.visibility = View.VISIBLE
                recyclerViewCart.visibility = View.GONE
                checkoutCard.visibility = View.GONE
            } else {
                layoutEmptyCart.visibility = View.GONE
                recyclerViewCart.visibility = View.VISIBLE
                checkoutCard.visibility = View.VISIBLE

                updateTotals(cartItems, textSubtotal, textTax, textShipping, textTotal, layoutSavings, textSavings)
            }
        }
    }

    private fun updateTotals(
        cartItems: List<CartItem>,
        textSubtotal: TextView,
        textTax: TextView,
        textShipping: TextView,
        textTotal: TextView,
        layoutSavings: View,
        textSavings: TextView
    ) {
        val formatter = NumberFormat.getCurrencyInstance(Locale.US)
        val subtotal = cartItems.sumOf { it.productPrice * it.quantity }
        val tax = subtotal * 0.085
        val shippingCost = if (subtotal >= 50.0) 0.0 else 9.99
        val total = subtotal + tax + shippingCost

        textSubtotal.text = formatter.format(subtotal)
        textTax.text = formatter.format(tax)
        textShipping.text = if (shippingCost == 0.0) "FREE" else formatter.format(shippingCost)
        textTotal.text = formatter.format(total)

        textShipping.setTextColor(
            if (shippingCost == 0.0)
                requireContext().getColor(R.color.green_500)
            else
                requireContext().getColor(R.color.black)
        )

        if (subtotal > 200) {
            val savings = subtotal * 0.05
            textSavings.text = formatter.format(savings)
            layoutSavings.visibility = View.VISIBLE
        } else {
            layoutSavings.visibility = View.GONE
        }
    }
}
