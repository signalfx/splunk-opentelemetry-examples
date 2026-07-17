import React, { useEffect, useState } from 'react';
import {
  ActivityIndicator,
  Image,
  ScrollView,
  StyleSheet,
  View,
} from 'react-native';
import { Button, Chip, FAB, Text } from 'react-native-paper';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { ScreenHeader } from '../components/ScreenHeader';
import { useShop } from '../store/ShopContext';
import { colors } from '../theme/colors';
import { formatCurrency } from '../utils/formatters';

interface ProductDetailScreenProps {
  productId: string;
  onNavigateBack: () => void;
  onNavigateToCart: () => void;
}

export function ProductDetailScreen({
  productId,
  onNavigateBack,
  onNavigateToCart,
}: ProductDetailScreenProps) {
  const { products, loadProducts, addToCart } = useShop();
  const [isFavorite, setIsFavorite] = useState(false);

  useEffect(() => {
    if (products.length === 0) {
      loadProducts();
    }
  }, [loadProducts, products.length]);

  const product = products.find((item) => item.id === productId);

  if (!product) {
    return (
      <View style={styles.container}>
        <ScreenHeader title="Product Details" onBack={onNavigateBack} />
        <View style={styles.centered}>
          <ActivityIndicator size="large" color={colors.primary} />
        </View>
      </View>
    );
  }

  const specs = product.specifications
    ?.split(',')
    .map((spec) => spec.trim())
    .filter(Boolean);

  return (
    <View style={styles.container}>
      <ScreenHeader title="Product Details" onBack={onNavigateBack} />
      <ScrollView contentContainerStyle={styles.scrollContent}>
        <View style={styles.imageContainer}>
          <Image source={{ uri: product.imageUrl }} style={styles.image} />
          <FAB
            icon={isFavorite ? 'heart' : 'heart-outline'}
            style={styles.favoriteFab}
            color={isFavorite ? colors.red500 : colors.onSurface}
            onPress={() => setIsFavorite((prev) => !prev)}
          />
        </View>

        <View style={styles.content}>
          <View style={styles.headerRow}>
            <View style={styles.titleBlock}>
              <Text variant="headlineSmall" style={styles.name}>
                {product.name}
              </Text>
              <Text variant="bodyMedium" style={styles.brand}>
                by {product.brand}
              </Text>
            </View>
            <Chip
              style={[
                styles.stockChip,
                { backgroundColor: product.inStock ? colors.green50 : colors.red50 },
              ]}
              textStyle={{ color: product.inStock ? colors.green500 : colors.red500 }}
            >
              {product.inStock ? 'In Stock' : 'Out of Stock'}
            </Chip>
          </View>

          <View style={styles.ratingRow}>
            {Array.from({ length: 5 }).map((_, index) => (
              <MaterialCommunityIcons
                key={index}
                name="star"
                size={20}
                color={index < Math.floor(product.rating) ? colors.primary : colors.grey300}
              />
            ))}
            <Text variant="bodyMedium" style={styles.ratingValue}>
              {product.rating.toFixed(1)}
            </Text>
            <Text variant="bodySmall" style={styles.reviewCount}>
              ({product.reviewCount} reviews)
            </Text>
          </View>

          <View style={styles.priceRow}>
            <Text variant="headlineMedium" style={styles.price}>
              {formatCurrency(product.price)}
            </Text>
            {product.price >= 50 ? (
              <Text variant="labelMedium" style={styles.freeShipping}>
                Free Shipping
              </Text>
            ) : null}
          </View>

          <View style={styles.divider} />

          <Text variant="titleMedium" style={styles.sectionTitle}>
            Description
          </Text>
          <Text variant="bodyMedium" style={styles.bodyText}>
            {product.description}
          </Text>

          <Text variant="titleMedium" style={styles.sectionTitle}>
            Specifications
          </Text>
          {specs && specs.length > 0 ? (
            specs.map((spec) => (
              <Text key={spec} variant="bodyMedium" style={styles.bodyText}>
                • {spec}
              </Text>
            ))
          ) : (
            <Text variant="bodyMedium" style={styles.bodyText}>
              No specifications available
            </Text>
          )}

          <View style={styles.actions}>
            <Button
              mode="outlined"
              icon="cart-outline"
              disabled={!product.inStock}
              style={styles.actionButton}
              onPress={() => addToCart(product)}
            >
              {product.inStock ? 'Add to Cart' : 'Out of Stock'}
            </Button>
            <Button
              mode="contained"
              disabled={!product.inStock}
              style={styles.actionButton}
              onPress={() => {
                addToCart(product);
                onNavigateToCart();
              }}
            >
              {product.inStock ? 'Buy Now' : 'Unavailable'}
            </Button>
          </View>
        </View>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.white,
  },
  centered: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  scrollContent: {
    paddingBottom: 24,
  },
  imageContainer: {
    position: 'relative',
  },
  image: {
    width: '100%',
    height: 300,
  },
  favoriteFab: {
    position: 'absolute',
    right: 16,
    bottom: 16,
    backgroundColor: colors.white,
  },
  content: {
    padding: 16,
  },
  headerRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    gap: 12,
  },
  titleBlock: {
    flex: 1,
  },
  name: {
    fontWeight: '700',
  },
  brand: {
    color: colors.grey600,
    marginTop: 4,
  },
  stockChip: {
    alignSelf: 'flex-start',
  },
  ratingRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    marginTop: 12,
  },
  ratingValue: {
    marginLeft: 8,
    fontWeight: '700',
  },
  reviewCount: {
    color: colors.grey600,
  },
  priceRow: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    gap: 12,
    marginTop: 16,
  },
  price: {
    color: colors.primary,
    fontWeight: '700',
  },
  freeShipping: {
    color: colors.green500,
  },
  divider: {
    height: 1,
    backgroundColor: colors.grey300,
    marginVertical: 16,
  },
  sectionTitle: {
    fontWeight: '700',
    marginBottom: 8,
  },
  bodyText: {
    color: colors.grey600,
    marginBottom: 4,
  },
  actions: {
    flexDirection: 'row',
    gap: 12,
    marginTop: 24,
  },
  actionButton: {
    flex: 1,
  },
});
