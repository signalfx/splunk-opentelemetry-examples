import SwiftUI

struct OrderHistoryView: View {
    @EnvironmentObject var orderViewModel: OrderViewModel

    var body: some View {
        NavigationStack {
            Group {
                if orderViewModel.isLoading {
                    ProgressView()
                } else if orderViewModel.orders.isEmpty {
                    emptyOrdersView
                } else {
                    ordersList
                }
            }
            .navigationBarHidden(true)
            .refreshable {
                orderViewModel.loadOrders()
            }
        }
    }

    private var emptyOrdersView: some View {
        VStack(spacing: 20) {
            Image(systemName: "clock")
                .font(.system(size: 64))
                .foregroundColor(.gray)

            Text("No orders yet")
                .font(.title2)
                .fontWeight(.medium)

            Text("Your order history will appear here")
                .font(.subheadline)
                .foregroundColor(.gray)
        }
    }

    private var ordersList: some View {
        List {
            ForEach(orderViewModel.orders) { order in
                NavigationLink(destination: OrderDetailsView(order: order)) {
                    OrderRow(order: order)
                }
            }
        }
        .listStyle(PlainListStyle())
    }
}

struct OrderRow: View {
    let order: Order

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(order.id)
                    .font(.subheadline)
                    .fontWeight(.semibold)

                Spacer()

                StatusBadge(status: order.status)
            }

            Text(order.formattedOrderDate)
                .font(.caption)
                .foregroundColor(.gray)

            HStack {
                Text("\(order.items.count) item(s)")
                    .font(.caption)
                    .foregroundColor(.gray)

                Spacer()

                Text(order.formattedTotal)
                    .font(.subheadline)
                    .fontWeight(.semibold)
                    .foregroundColor(.blue)
            }
        }
        .padding(.vertical, 4)
    }
}

struct StatusBadge: View {
    let status: OrderStatus

    var body: some View {
        Text(status.rawValue)
            .font(.caption)
            .fontWeight(.medium)
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(backgroundColor)
            .foregroundColor(foregroundColor)
            .cornerRadius(8)
    }

    private var backgroundColor: Color {
        switch status {
        case .pending: return Color.orange.opacity(0.2)
        case .processing: return Color.blue.opacity(0.2)
        case .shipped: return Color.purple.opacity(0.2)
        case .outForDelivery: return Color.teal.opacity(0.2)
        case .delivered: return Color.green.opacity(0.2)
        case .cancelled: return Color.red.opacity(0.2)
        }
    }

    private var foregroundColor: Color {
        switch status {
        case .pending: return .orange
        case .processing: return .blue
        case .shipped: return .purple
        case .outForDelivery: return .teal
        case .delivered: return .green
        case .cancelled: return .red
        }
    }
}

#Preview {
    OrderHistoryView()
        .environmentObject(OrderViewModel())
}
