import SwiftUI

struct OrderConfirmationView: View {
    @EnvironmentObject var orderViewModel: OrderViewModel
    @Environment(\.dismiss) var dismiss

    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                // Success Icon
                successHeader

                // Order Details
                if let order = orderViewModel.currentOrder {
                    orderDetails(order)
                }

                // Continue Shopping Button
                continueButton
            }
            .padding()
        }
        .navigationBarHidden(true)
        .navigationBarBackButtonHidden(true)
    }

    private var successHeader: some View {
        VStack(spacing: 16) {
            ZStack {
                Circle()
                    .fill(Color.green.opacity(0.1))
                    .frame(width: 100, height: 100)

                Image(systemName: "checkmark.circle.fill")
                    .font(.system(size: 60))
                    .foregroundColor(.green)
            }

            Text("Thank You!")
                .font(.title)
                .fontWeight(.bold)

            Text("Your order has been placed successfully")
                .font(.subheadline)
                .foregroundColor(.gray)
                .multilineTextAlignment(.center)
        }
        .padding(.top, 20)
    }

    private func orderDetails(_ order: Order) -> some View {
        VStack(alignment: .leading, spacing: 16) {
            // Order ID
            HStack {
                Label("Order ID", systemImage: "number")
                    .foregroundColor(.gray)
                Spacer()
                Text(order.id)
                    .fontWeight(.semibold)
            }

            Divider()

            // Order Date
            HStack {
                Label("Order Date", systemImage: "calendar")
                    .foregroundColor(.gray)
                Spacer()
                Text(order.formattedOrderDate)
            }

            // Estimated Delivery
            if let deliveryDate = order.formattedDeliveryDate {
                HStack {
                    Label("Est. Delivery", systemImage: "shippingbox")
                        .foregroundColor(.gray)
                    Spacer()
                    Text(deliveryDate)
                }
            }

            Divider()

            // Items
            Text("Items")
                .font(.headline)

            ForEach(order.items) { item in
                HStack {
                    Text("\(item.quantity)x")
                        .foregroundColor(.gray)
                    Text(item.productName)
                        .lineLimit(1)
                    Spacer()
                    Text(item.formattedPrice)
                }
                .font(.subheadline)
            }

            Divider()

            // Total
            HStack {
                Text("Total")
                    .font(.headline)
                Spacer()
                Text(order.formattedTotal)
                    .font(.headline)
                    .foregroundColor(.blue)
            }

            Divider()

            // Shipping Address
            VStack(alignment: .leading, spacing: 8) {
                Text("Shipping To")
                    .font(.headline)

                Text(order.shippingAddress.fullName)
                    .fontWeight(.medium)

                Text(order.shippingAddress.formattedAddress)
                    .font(.subheadline)
                    .foregroundColor(.gray)
            }
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }

    private var continueButton: some View {
        Button(action: {
            NotificationCenter.default.post(name: .continueShoppingTapped, object: nil)
        }) {
            HStack {
                Image(systemName: "arrow.left")
                Text("Continue Shopping")
            }
            .font(.headline)
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .padding()
            .background(Color.blue)
            .cornerRadius(12)
        }
    }
}

#Preview {
    NavigationStack {
        OrderConfirmationView()
            .environmentObject(OrderViewModel())
    }
}
