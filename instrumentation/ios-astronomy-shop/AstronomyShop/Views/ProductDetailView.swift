import SwiftUI

struct ProductDetailView: View {
    let product: Product
    @EnvironmentObject var cartViewModel: CartViewModel
    @State private var quantity: Int = 1

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                // Product Image
                productImage

                // Product Info
                productInfo

                // Rating
                ratingSection

                // Description
                descriptionSection

                // Quantity Selector
                quantitySelector

                // Add to Cart Button
                addToCartButton

                Spacer()
            }
            .padding()
        }
        .navigationBarHidden(true)
    }

    private var productImage: some View {
        AsyncImage(url: URL(string: product.imageUrl)) { image in
            image
                .resizable()
                .aspectRatio(contentMode: .fill)
        } placeholder: {
            Color(.systemGray6)
        }
        .frame(height: 250)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    private var productInfo: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(product.category)
                    .font(.caption)
                    .fontWeight(.medium)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 4)
                    .background(Color.blue.opacity(0.1))
                    .foregroundColor(.blue)
                    .cornerRadius(12)

                Spacer()

                if !product.inStock {
                    Text("Out of Stock")
                        .font(.caption)
                        .fontWeight(.medium)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 4)
                        .background(Color.red.opacity(0.1))
                        .foregroundColor(.red)
                        .cornerRadius(12)
                }
            }

            Text(product.name)
                .font(.title2)
                .fontWeight(.bold)

            Text(product.formattedPrice)
                .font(.title)
                .fontWeight(.bold)
                .foregroundColor(.blue)
        }
    }

    private var ratingSection: some View {
        HStack(spacing: 4) {
            ForEach(0..<5) { index in
                Image(systemName: index < Int(product.rating) ? "star.fill" : "star")
                    .foregroundColor(.orange)
                    .font(.subheadline)
            }

            Text(String(format: "%.1f", product.rating))
                .font(.subheadline)
                .fontWeight(.medium)

            Text("(\(product.reviewCount) reviews)")
                .font(.caption)
                .foregroundColor(.gray)
        }
    }

    private var descriptionSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Description")
                .font(.headline)

            Text(product.description)
                .font(.body)
                .foregroundColor(.secondary)
        }
    }

    private var quantitySelector: some View {
        HStack {
            Text("Quantity")
                .font(.headline)

            Spacer()

            HStack(spacing: 16) {
                Button(action: {
                    if quantity > 1 { quantity -= 1 }
                }) {
                    Image(systemName: "minus.circle.fill")
                        .font(.title2)
                        .foregroundColor(quantity > 1 ? .blue : .gray)
                }
                .disabled(quantity <= 1)

                Text("\(quantity)")
                    .font(.title3)
                    .fontWeight(.semibold)
                    .frame(width: 40)

                Button(action: {
                    if quantity < 10 { quantity += 1 }
                }) {
                    Image(systemName: "plus.circle.fill")
                        .font(.title2)
                        .foregroundColor(quantity < 10 ? .blue : .gray)
                }
                .disabled(quantity >= 10)
            }
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }

    private var addToCartButton: some View {
        Button(action: addToCart) {
            HStack {
                Image(systemName: "cart.badge.plus")
                Text("Add to Cart - \(String(format: "$%.2f", product.price * Double(quantity)))")
            }
            .font(.headline)
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .padding()
            .background(product.inStock ? Color.blue : Color.gray)
            .cornerRadius(12)
        }
        .disabled(!product.inStock)
    }

    private func addToCart() {
        cartViewModel.addToCart(product, quantity: quantity)
        quantity = 1
    }
}

#Preview {
    NavigationStack {
        ProductDetailView(product: Product.sampleProducts[0])
            .environmentObject(CartViewModel())
    }
}
