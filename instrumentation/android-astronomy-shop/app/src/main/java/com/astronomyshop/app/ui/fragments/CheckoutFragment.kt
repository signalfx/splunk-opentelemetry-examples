package com.astronomyshop.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.astronomyshop.app.R
import com.astronomyshop.app.ui.viewmodels.MainViewModel
import java.text.NumberFormat
import java.util.*

class CheckoutFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

    // View references
    private lateinit var editFullName: TextInputEditText
    private lateinit var editEmail: TextInputEditText
    private lateinit var editPhone: TextInputEditText
    private lateinit var editAddress: TextInputEditText
    private lateinit var editCity: TextInputEditText
    private lateinit var editState: TextInputEditText
    private lateinit var editZipCode: TextInputEditText
    private lateinit var editCardNumber: TextInputEditText
    private lateinit var editExpiryDate: TextInputEditText
    private lateinit var editCvv: TextInputEditText
    private lateinit var editCardName: TextInputEditText
    private lateinit var spinnerCountry: Spinner
    private lateinit var textOrderSubtotal: TextView
    private lateinit var textOrderTax: TextView
    private lateinit var textOrderShipping: TextView
    private lateinit var textOrderTotal: TextView
    private lateinit var textItemCount: TextView
    private lateinit var buttonPlaceOrder: MaterialButton
    private lateinit var checkboxSavePayment: CheckBox
    private lateinit var checkboxSameAsShipping: CheckBox

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_checkout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupCountrySpinner()
        prefillForm()
        setupClickListeners()
        updateOrderSummary()
    }

    private fun initViews(view: View) {
        // Initialize all view references
        editFullName = view.findViewById(R.id.editFullName)
        editEmail = view.findViewById(R.id.editEmail)
        editPhone = view.findViewById(R.id.editPhone)
        editAddress = view.findViewById(R.id.editAddress)
        editCity = view.findViewById(R.id.editCity)
        editState = view.findViewById(R.id.editState)
        editZipCode = view.findViewById(R.id.editZipCode)
        editCardNumber = view.findViewById(R.id.editCardNumber)
        editExpiryDate = view.findViewById(R.id.editExpiryDate)
        editCvv = view.findViewById(R.id.editCvv)
        editCardName = view.findViewById(R.id.editCardName)
        spinnerCountry = view.findViewById(R.id.spinnerCountry)
        textOrderSubtotal = view.findViewById(R.id.textOrderSubtotal)
        textOrderTax = view.findViewById(R.id.textOrderTax)
        textOrderShipping = view.findViewById(R.id.textOrderShipping)
        textOrderTotal = view.findViewById(R.id.textOrderTotal)
        textItemCount = view.findViewById(R.id.textItemCount)
        buttonPlaceOrder = view.findViewById(R.id.buttonPlaceOrder)
        checkboxSavePayment = view.findViewById(R.id.checkboxSavePayment)
        checkboxSameAsShipping = view.findViewById(R.id.checkboxSameAsShipping)
    }

    private fun prefillForm() {
        // Prefill form data
        editFullName.setText("Alexia Johnson")
        editEmail.setText("alexia.johnson@somerandomemail.com")
        editPhone.setText("+1 (555) 123-4567")
        editAddress.setText("123 Observatory Drive")
        editCity.setText("Sydney")
        editState.setText("NSW")
        editZipCode.setText("2000")
        editCardNumber.setText("4532 1234 5678 9012")
        editExpiryDate.setText("12/28")
        editCvv.setText("123")
        editCardName.setText("Alexia Johnson")

        checkboxSavePayment.isChecked = true
        checkboxSameAsShipping.isChecked = true
    }

    private fun setupCountrySpinner() {
        val countries = arrayOf(
            "United States", "Canada", "United Kingdom", "Australia",
            "Germany", "France", "Japan", "Other"
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            countries
        )

        spinnerCountry.adapter = adapter
        spinnerCountry.setSelection(3) // Australia
    }

    private fun setupClickListeners() {
        buttonPlaceOrder.setOnClickListener {
            if (validateForm()) {
                processOrder()
            }
        }

        checkboxSameAsShipping.setOnCheckedChangeListener { _, _ -> }

        // Format card number as user types
        editCardNumber.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                formatCardNumber()
            }
        }

        // Format expiry date
        editExpiryDate.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                formatExpiryDate()
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true
        val requiredFields = mapOf(
            editFullName to "Full name is required",
            editEmail to "Email is required",
            editPhone to "Phone number is required",
            editAddress to "Address is required",
            editCity to "City is required",
            editState to "State is required",
            editZipCode to "ZIP code is required",
            editCardNumber to "Card number is required",
            editExpiryDate to "Expiry date is required",
            editCvv to "CVV is required",
            editCardName to "Cardholder name is required"
        )

        // Clear previous errors
        requiredFields.keys.forEach { editText ->
            (editText.parent.parent as? TextInputLayout)?.error = null
        }

        // Validate required fields
        requiredFields.forEach { (editText, errorMessage) ->
            if (editText.text.isNullOrBlank()) {
                (editText.parent.parent as? TextInputLayout)?.error = errorMessage
                isValid = false
            }
        }

        // Validate email format
        val email = editEmail.text.toString()
        if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            (editEmail.parent.parent as? TextInputLayout)?.error = "Invalid email format"
            isValid = false
        }

        // Validate card number
        val cardNumber = editCardNumber.text.toString().replace(" ", "")
        if (cardNumber.isNotEmpty() && cardNumber.length < 16) {
            (editCardNumber.parent.parent as? TextInputLayout)?.error = "Invalid card number"
            isValid = false
        }

        // Validate CVV
        val cvv = editCvv.text.toString()
        if (cvv.isNotEmpty() && (cvv.length < 3 || cvv.length > 4)) {
            (editCvv.parent.parent as? TextInputLayout)?.error = "Invalid CVV"
            isValid = false
        }

        if (!isValid) {
        } else {
        }

        return isValid
    }

    private fun formatCardNumber() {
        val cardNumber = editCardNumber.text.toString().replace(" ", "")
        if (cardNumber.length >= 4) {
            val formatted = cardNumber.chunked(4).joinToString(" ")
            editCardNumber.setText(formatted)
            editCardNumber.setSelection(formatted.length)
        }
    }

    private fun formatExpiryDate() {
        val expiry = editExpiryDate.text.toString().replace("/", "")
        if (expiry.length >= 2) {
            val formatted = "${expiry.substring(0, 2)}/${expiry.substring(2, minOf(expiry.length, 4))}"
            editExpiryDate.setText(formatted)
            editExpiryDate.setSelection(formatted.length)
        }
    }

    private fun updateOrderSummary() {
        viewModel.cartItems.observe(viewLifecycleOwner) { cartItems ->
            if (cartItems.isNotEmpty()) {
                val formatter = NumberFormat.getCurrencyInstance(Locale.US)
                val subtotal = cartItems.sumOf { it.productPrice * it.quantity }
                val tax = subtotal * 0.085
                val shippingCost = if (subtotal >= 50.0) 0.0 else 9.99
                val total = subtotal + tax + shippingCost
                val itemCount = cartItems.sumOf { it.quantity }

                textOrderSubtotal.text = formatter.format(subtotal)
                textOrderTax.text = formatter.format(tax)
                textOrderShipping.text = if (shippingCost == 0.0) "FREE" else formatter.format(shippingCost)
                textOrderTotal.text = formatter.format(total)
                textItemCount.text = if (itemCount == 1) "1 item" else "$itemCount items"

                // Update shipping color
                textOrderShipping.setTextColor(
                    if (shippingCost == 0.0)
                        requireContext().getColor(R.color.green_500)
                    else
                        requireContext().getColor(R.color.black)
                )
            }
        }
    }

    private fun processOrder() {
        try {
            buttonPlaceOrder.isEnabled = false
            buttonPlaceOrder.text = "Processing Payment..."

            val paymentError = viewModel.checkPaymentError()

            if (paymentError != null) {
                requireView().postDelayed({
                    buttonPlaceOrder.isEnabled = true
                    buttonPlaceOrder.text = "Place Order"

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Payment Declined")
                        .setMessage("Card Declined - Error Code: 4001\n\n(This is a test error)")
                        .setPositiveButton("Try Again") { _, _ -> }
                        .setNegativeButton("Clear Error") { _, _ ->
                            viewModel.clearError()
                        }
                        .show()
                }, 1500)
                return
            }

            // Payment successful
            requireView().postDelayed({
                viewModel.createOrder(
                    customerName = editFullName.text.toString(),
                    customerEmail = editEmail.text.toString(),
                    shippingAddress = buildShippingAddress(),
                    paymentMethod = getCardLastFour()
                )

                // Navigate to confirmation
                requireView().postDelayed({
                    val bundle = Bundle().apply {
                        putString("customerName", editFullName.text.toString())
                        putString("customerEmail", editEmail.text.toString())
                        putString("orderTotal", textOrderTotal.text.toString())
                    }
                    findNavController().navigate(R.id.orderConfirmationFragment, bundle)
                }, 500)

            }, 2000)

        } catch (e: Exception) {
            buttonPlaceOrder.isEnabled = true
            buttonPlaceOrder.text = "Place Order"
        }
    }

    private fun buildShippingAddress(): String {
        return "${editAddress.text}\n${editCity.text}, ${editState.text} ${editZipCode.text}\n${spinnerCountry.selectedItem}"
    }

    private fun getCardLastFour(): String {
        val cardNumber = editCardNumber.text.toString().replace(" ", "")
        return if (cardNumber.length >= 4) {
            "**** **** **** ${cardNumber.takeLast(4)}"
        } else {
            "****"
        }
    }
}