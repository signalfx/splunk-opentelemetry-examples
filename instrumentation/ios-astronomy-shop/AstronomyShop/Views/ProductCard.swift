import SwiftUI

struct ProductCard: View {
    let product: Product
    @EnvironmentObject var cartViewModel: CartViewModel

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            // Product Image
            ZStack {
                AsyncImage(url: URL(string: product.imageUrl)) { image in
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                } placeholder: {
                    Color(.systemGray6)
                }
                .frame(height: 120)
                .clipShape(RoundedRectangle(cornerRadius: 12))

                if !product.inStock {
                    VStack {
                        HStack {
                            Spacer()
                            Text("Out of Stock")
                                .font(.caption2)
                                .fontWeight(.medium)
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(Color.red)
                                .foregroundColor(.white)
                                .cornerRadius(4)
                                .padding(6)
                        }
                        Spacer()
                    }
                }
            }
            .frame(height: 120)

            // Product Info
            VStack(alignment: .leading, spacing: 4) {
                Text(product.name)
                    .font(.subheadline)
                    .fontWeight(.medium)
                    .lineLimit(2)
                    .multilineTextAlignment(.leading)

                HStack(spacing: 2) {
                    Image(systemName: "star.fill")
                        .font(.caption2)
                        .foregroundColor(.orange)

                    Text(String(format: "%.1f", product.rating))
                        .font(.caption)
                        .foregroundColor(.gray)
                }

                Text(product.formattedPrice)
                    .font(.headline)
                    .foregroundColor(.blue)
            }

            // Add to Cart Button
            Button(action: { cartViewModel.addToCart(product, quantity: 1) }) {
                HStack(spacing: 4) {
                    Image(systemName: "cart.badge.plus")
                        .font(.caption)
                    Text("Add to Cart")
                        .font(.caption)
                        .fontWeight(.medium)
                }
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 8)
                .background(product.inStock ? Color.blue : Color.gray)
                .cornerRadius(8)
            }
            .disabled(!product.inStock)
        }
        .padding(10)
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.1), radius: 4, x: 0, y: 2)
    }
}

#Preview {
    HStack {
        ProductCard(product: Product.sampleProducts[0])
        ProductCard(product: Product.sampleProducts[4])
    }
    .padding()
}
