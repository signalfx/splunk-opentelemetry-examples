import SwiftUI

struct OrderDetailsView: View {
    let order: Order

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                // Status Section
                statusSection

                // Order Info
                orderInfoSection

                // Items Section
                itemsSection

                // Shipping Section
                shippingSection

                // Tracking Section
                if order.status == .shipped || order.status == .outForDelivery {
                    trackingSection
                }
            }
            .padding()
        }
        .navigationBarHidden(true)
    }

    private var statusSection: some View {
        VStack(spacing: 12) {
            StatusBadge(status: order.status)

            if let deliveryDate = order.formattedDeliveryDate {
                HStack {
                    Image(systemName: "shippingbox.fill")
                        .foregroundColor(.blue)

                    Text(order.status == .delivered ? "Delivered on \(deliveryDate)" : "Estimated delivery: \(deliveryDate)")
                        .font(.subheadline)
                }
            }
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }

    private var orderInfoSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Label("Order Information", systemImage: "info.circle.fill")
                .font(.headline)

            VStack(spacing: 8) {
                infoRow(label: "Order ID", value: order.id)
                infoRow(label: "Order Date", value: order.formattedOrderDate)
                infoRow(label: "Total", value: order.formattedTotal, highlight: true)
            }
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }

    private func infoRow(label: String, value: String, highlight: Bool = false) -> some View {
        HStack {
            Text(label)
                .foregroundColor(.gray)
            Spacer()
            Text(value)
                .fontWeight(highlight ? .semibold : .regular)
                .foregroundColor(highlight ? .blue : .primary)
        }
        .font(.subheadline)
    }

    private var itemsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Label("Items", systemImage: "bag.fill")
                .font(.headline)

            VStack(spacing: 12) {
                ForEach(order.items) { item in
                    HStack(alignment: .top) {
                        AsyncImage(url: URL(string: item.imageUrl ?? "")) { image in
                            image
                                .resizable()
                                .aspectRatio(contentMode: .fill)
                        } placeholder: {
                            Color(.systemGray5)
                        }
                        .frame(width: 50, height: 50)
                        .clipShape(RoundedRectangle(cornerRadius: 8))

                        VStack(alignment: .leading, spacing: 4) {
                            Text(item.productName)
                                .font(.subheadline)
                                .fontWeight(.medium)
                                .lineLimit(2)

                            Text("Qty: \(item.quantity)")
                                .font(.caption)
                                .foregroundColor(.gray)
                        }

                        Spacer()

                        Text(String(format: "$%.2f", item.subtotal))
                            .font(.subheadline)
                            .fontWeight(.medium)
                    }

                    if item.id != order.items.last?.id {
                        Divider()
                    }
                }
            }
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }

    private var shippingSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Label("Shipping Address", systemImage: "location.fill")
                .font(.headline)

            VStack(alignment: .leading, spacing: 4) {
                Text(order.shippingAddress.fullName)
                    .fontWeight(.medium)

                Text(order.shippingAddress.formattedAddress)
                    .font(.subheadline)
                    .foregroundColor(.gray)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }

    private var trackingSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Label("Tracking", systemImage: "location.magnifyingglass")
                .font(.headline)

            VStack(alignment: .leading, spacing: 16) {
                TrackingStep(
                    title: "Order Placed",
                    subtitle: order.formattedOrderDate,
                    isCompleted: true,
                    isCurrent: order.status == .pending
                )

                TrackingStep(
                    title: "Processing",
                    subtitle: "Order is being prepared",
                    isCompleted: order.status != .pending,
                    isCurrent: order.status == .processing
                )

                TrackingStep(
                    title: "Shipped",
                    subtitle: "On the way to you",
                    isCompleted: order.status == .shipped || order.status == .outForDelivery || order.status == .delivered,
                    isCurrent: order.status == .shipped
                )

                TrackingStep(
                    title: "Out for Delivery",
                    subtitle: "Almost there!",
                    isCompleted: order.status == .outForDelivery || order.status == .delivered,
                    isCurrent: order.status == .outForDelivery
                )

                TrackingStep(
                    title: "Delivered",
                    subtitle: order.formattedDeliveryDate ?? "Pending",
                    isCompleted: order.status == .delivered,
                    isCurrent: order.status == .delivered,
                    isLast: true
                )
            }
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }
}

struct TrackingStep: View {
    let title: String
    let subtitle: String
    let isCompleted: Bool
    let isCurrent: Bool
    var isLast: Bool = false

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            VStack {
                Circle()
                    .fill(isCompleted ? Color.green : Color.gray.opacity(0.3))
                    .frame(width: 24, height: 24)
                    .overlay(
                        Image(systemName: isCompleted ? "checkmark" : "circle")
                            .font(.caption)
                            .foregroundColor(.white)
                    )

                if !isLast {
                    Rectangle()
                        .fill(isCompleted ? Color.green : Color.gray.opacity(0.3))
                        .frame(width: 2, height: 30)
                }
            }

            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.subheadline)
                    .fontWeight(isCurrent ? .semibold : .regular)
                    .foregroundColor(isCompleted || isCurrent ? .primary : .gray)

                Text(subtitle)
                    .font(.caption)
                    .foregroundColor(.gray)
            }

            Spacer()
        }
    }
}

#Preview {
    NavigationStack {
        OrderDetailsView(order: Order.sampleOrders[1])
    }
}
