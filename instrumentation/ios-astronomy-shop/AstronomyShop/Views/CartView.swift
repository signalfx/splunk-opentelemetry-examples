import SwiftUI

struct CartView: View {
    @EnvironmentObject var cartViewModel: CartViewModel

    var body: some View {
        NavigationStack {
            Group {
                if cartViewModel.isEmpty {
                    emptyCartView
                } else {
                    cartContent
                }
            }
            .navigationBarHidden(true)
        }
    }

    private var emptyCartView: some View {
        VStack(spacing: 20) {
            Image(systemName: "cart")
                .font(.system(size: 64))
                .foregroundColor(.gray)

            Text("Your cart is empty")
                .font(.title2)
                .fontWeight(.medium)

            Text("Browse our products and add items to your cart")
                .font(.subheadline)
                .foregroundColor(.gray)
                .multilineTextAlignment(.center)
                .padding(.horizontal)
        }
    }

    private var cartContent: some View {
        VStack(spacing: 0) {
            // Cart Items List
            List {
                ForEach(cartViewModel.items) { item in
                    CartItemRow(item: item)
                        .swipeActions(edge: .trailing, allowsFullSwipe: true) {
                            Button(role: .destructive) {
                                cartViewModel.removeFromCart(item)
                            } label: {
                                Label("Delete", systemImage: "trash")
                            }
                        }
                }
            }
            .listStyle(PlainListStyle())

            // Order Summary
            orderSummary
        }
    }

    private var orderSummary: some View {
        VStack(spacing: 12) {
            Divider()

            VStack(spacing: 8) {
                summaryRow(title: "Subtotal", value: cartViewModel.formattedSubtotal)
                summaryRow(title: "Tax (8.25%)", value: cartViewModel.formattedTax)
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
            .padding(.horizontal)

            if cartViewModel.shipping == 0 {
                HStack {
                    Image(systemName: "shippingbox.fill")
                        .foregroundColor(.green)
                    Text("Free shipping on orders over $100!")
                        .font(.caption)
                        .foregroundColor(.green)
                }
            }

            NavigationLink(destination: CheckoutView()) {
                Text("Proceed to Checkout")
                    .font(.headline)
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.blue)
                    .cornerRadius(12)
            }
            .padding(.horizontal)
            .padding(.bottom)
        }
        .background(Color(.systemBackground))
    }

    private func summaryRow(title: String, value: String) -> some View {
        HStack {
            Text(title)
                .foregroundColor(.gray)
            Spacer()
            Text(value)
        }
    }

}

struct CartItemRow: View {
    let item: CartItem
    @EnvironmentObject var cartViewModel: CartViewModel
    @State private var quantityText: String
    @FocusState private var isQuantityFocused: Bool

    init(item: CartItem) {
        self.item = item
        self._quantityText = State(initialValue: "\(item.quantity)")
    }

    var body: some View {
        HStack(spacing: 12) {
            // Product Image
            AsyncImage(url: URL(string: item.product.imageUrl)) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } placeholder: {
                Color(.systemGray6)
            }
            .frame(width: 80, height: 80)
            .clipShape(RoundedRectangle(cornerRadius: 8))

            // Product Info
            VStack(alignment: .leading, spacing: 4) {
                Text(item.product.name)
                    .font(.subheadline)
                    .fontWeight(.medium)
                    .lineLimit(2)

                Text(item.product.formattedPrice)
                    .font(.caption)
                    .foregroundColor(.gray)

                // Quantity Controls
                HStack(spacing: 12) {
                    Button(action: {
                        cartViewModel.decrementQuantity(for: item)
                    }) {
                        Image(systemName: "minus.circle")
                            .foregroundColor(.blue)
                    }
                    .buttonStyle(.borderless)

                    TextField("", text: $quantityText)
                        .keyboardType(.numberPad)
                        .multilineTextAlignment(.center)
                        .font(.subheadline)
                        .fontWeight(.medium)
                        .frame(width: 40)
                        .padding(.vertical, 3)
                        .background(Color(.systemGray6))
                        .cornerRadius(6)
                        .focused($isQuantityFocused)
                        .onSubmit { commitQuantity() }

                    Button(action: {
                        cartViewModel.incrementQuantity(for: item)
                    }) {
                        Image(systemName: "plus.circle")
                            .foregroundColor(.blue)
                    }
                    .buttonStyle(.borderless)
                }
            }

            Spacer()

            // Subtotal
            Text(item.formattedSubtotal)
                .font(.subheadline)
                .fontWeight(.semibold)
        }
        .padding(.vertical, 4)
        .onChange(of: item.quantity) { newQuantity in
            if !isQuantityFocused {
                quantityText = "\(newQuantity)"
            }
        }
    }

    private func commitQuantity() {
        let qty = Int(quantityText) ?? 0
        cartViewModel.updateQuantity(for: item, quantity: qty)
        if qty > 0 {
            quantityText = "\(qty)"
        }
    }
}

#Preview {
    CartView()
        .environmentObject(CartViewModel())
}
