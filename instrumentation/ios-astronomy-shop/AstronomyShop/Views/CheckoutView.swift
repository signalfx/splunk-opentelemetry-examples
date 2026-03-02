import SwiftUI

struct CheckoutView: View {
    @EnvironmentObject var cartViewModel: CartViewModel
    @EnvironmentObject var orderViewModel: OrderViewModel
    @Environment(\.dismiss) var dismiss
    @State private var showConfirmation = false
    @State private var showPaymentErrorAlert = false

    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                // Shipping Section
                shippingSection

                // Payment Section
                paymentSection

                // Order Summary
                orderSummarySection

                // Place Order Button
                placeOrderButton

                if let error = orderViewModel.errorMessage {
                    Text(error)
                        .font(.caption)
                        .foregroundColor(.red)
                        .padding()
                }
            }
            .padding()
        }
        .navigationBarHidden(true)
        .navigationDestination(isPresented: $showConfirmation) {
            OrderConfirmationView()
        }
        .alert("Credit Card Declined", isPresented: $showPaymentErrorAlert) {
            Button("Try Again") {
                showPaymentErrorAlert = true
            }
            Button("Clear Error") {
                cartViewModel.clearError()
            }
        } message: {
            Text("Your payment could not be processed. The card was declined by the issuing bank.")
        }
        .onChange(of: orderViewModel.orderPlaced) { newValue in
            if newValue {
                cartViewModel.clearCart()
                showConfirmation = true
                orderViewModel.resetOrderPlaced()
            }
        }
    }

    private var shippingSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            Label("Shipping Address", systemImage: "shippingbox.fill")
                .font(.headline)

            VStack(spacing: 12) {
                CustomTextField(
                    placeholder: "Full Name",
                    text: $orderViewModel.fullName,
                    icon: "person.fill"
                )

                CustomTextField(
                    placeholder: "Street Address",
                    text: $orderViewModel.streetAddress,
                    icon: "house.fill"
                )

                HStack(spacing: 12) {
                    CustomTextField(
                        placeholder: "City",
                        text: $orderViewModel.city,
                        icon: "building.2.fill"
                    )

                    CustomTextField(
                        placeholder: "State",
                        text: $orderViewModel.state,
                        icon: "map.fill"
                    )
                    .frame(width: 100)
                }

                HStack(spacing: 12) {
                    CustomTextField(
                        placeholder: "ZIP Code",
                        text: $orderViewModel.zipCode,
                        icon: "envelope.fill"
                    )
                    .keyboardType(.numberPad)

                    CustomTextField(
                        placeholder: "Country",
                        text: $orderViewModel.country,
                        icon: "globe"
                    )
                }
            }
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }

    private var paymentSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            Label("Payment Method", systemImage: "creditcard.fill")
                .font(.headline)

            VStack(spacing: 12) {
                CustomTextField(
                    placeholder: "Card Number",
                    text: $orderViewModel.cardNumber,
                    icon: "creditcard"
                )
                .keyboardType(.numberPad)

                HStack(spacing: 12) {
                    CustomTextField(
                        placeholder: "MM/YY",
                        text: $orderViewModel.expiryDate,
                        icon: "calendar"
                    )
                    .keyboardType(.numberPad)

                    CustomTextField(
                        placeholder: "CVV",
                        text: $orderViewModel.cvv,
                        icon: "lock.fill"
                    )
                    .keyboardType(.numberPad)
                }
            }

            HStack {
                Image(systemName: "lock.shield.fill")
                    .foregroundColor(.green)
                Text("Your payment information is secure")
                    .font(.caption)
                    .foregroundColor(.gray)
            }
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }

    private var orderSummarySection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Label("Order Summary", systemImage: "list.bullet.clipboard.fill")
                .font(.headline)

            VStack(spacing: 8) {
                ForEach(cartViewModel.items) { item in
                    HStack(spacing: 8) {
                        Text(item.product.name)
                            .font(.subheadline)
                            .lineLimit(1)

                        Spacer()

                        Button(action: { cartViewModel.decrementQuantity(for: item) }) {
                            Image(systemName: "minus.circle")
                                .font(.subheadline)
                                .foregroundColor(.blue)
                        }

                        Text("\(item.quantity)")
                            .font(.subheadline)
                            .fontWeight(.medium)
                            .frame(minWidth: 20, alignment: .center)

                        Button(action: { cartViewModel.incrementQuantity(for: item) }) {
                            Image(systemName: "plus.circle")
                                .font(.subheadline)
                                .foregroundColor(.blue)
                        }

                        Text(item.formattedSubtotal)
                            .font(.subheadline)
                            .frame(minWidth: 56, alignment: .trailing)
                    }
                }

                Divider()

                summaryRow(title: "Subtotal", value: cartViewModel.formattedSubtotal)
                summaryRow(title: "Tax", value: cartViewModel.formattedTax)
                summaryRow(title: "Shipping", value: cartViewModel.formattedShipping)

                Divider()

                HStack {
                    Text("Total")
                        .font(.headline)
                    Spacer()
                    Text(cartViewModel.formattedTotal)
                        .font(.headline)
                        .foregroundColor(.blue)
                }
            }
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }

    private func summaryRow(title: String, value: String) -> some View {
        HStack {
            Text(title)
                .font(.subheadline)
                .foregroundColor(.gray)
            Spacer()
            Text(value)
                .font(.subheadline)
        }
    }

    private var placeOrderButton: some View {
        Button(action: placeOrder) {
            if orderViewModel.isLoading {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: .white))
            } else {
                HStack {
                    Image(systemName: "checkmark.circle.fill")
                    Text("Place Order - \(cartViewModel.formattedTotal)")
                }
            }
        }
        .font(.headline)
        .foregroundColor(.white)
        .frame(maxWidth: .infinity)
        .padding()
        .background(orderViewModel.canPlaceOrder ? Color.blue : Color.gray)
        .cornerRadius(12)
        .disabled(!orderViewModel.canPlaceOrder || orderViewModel.isLoading)
    }

    private func placeOrder() {
        if cartViewModel.errorMessage != nil {
            showPaymentErrorAlert = true
            return
        }
        orderViewModel.placeOrder(
            cartItems: cartViewModel.items,
            total: cartViewModel.total
        )
    }
}

struct CustomTextField: View {
    let placeholder: String
    @Binding var text: String
    let icon: String

    var body: some View {
        HStack {
            Image(systemName: icon)
                .foregroundColor(.gray)
                .frame(width: 24)

            TextField(placeholder, text: $text)
        }
        .padding()
        .background(Color(.systemBackground))
        .cornerRadius(8)
    }
}

#Preview {
    NavigationStack {
        CheckoutView()
            .environmentObject(CartViewModel())
            .environmentObject(OrderViewModel())
    }
}
