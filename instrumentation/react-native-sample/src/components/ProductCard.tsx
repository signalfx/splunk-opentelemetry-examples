import React from 'react';
import { Image, Pressable, StyleSheet, View } from 'react-native';
import { Button, Card, Text } from 'react-native-paper';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Product } from '../models/types';
import { colors } from '../theme/colors';
import { formatCurrency } from '../utils/formatters';

interface ProductCardProps {
  product: Product;
  onPress: () => void;
  onAddToCart: () => void;
}

export function ProductCard({ product, onPress, onAddToCart }: ProductCardProps) {
  return (
    <Pressable onPress={onPress} style={styles.wrapper}>
      <Card style={styles.card}>
        <Image source={{ uri: product.imageUrl }} style={styles.image} />
        <Card.Content style={styles.content}>
          <Text variant="titleSmall" numberOfLines={2} style={styles.name}>
            {product.name}
          </Text>
          <Text variant="bodySmall" style={styles.brand}>
            {product.brand}
          </Text>

          <View style={styles.ratingRow}>
            <MaterialCommunityIcons name="star" size={16} color={colors.primary} />
            <Text variant="bodySmall" style={styles.ratingText}>
              {product.rating.toFixed(1)}
            </Text>
            <Text variant="bodySmall" style={styles.reviewCount}>
              ({product.reviewCount})
            </Text>
          </View>

          <Text
            variant="labelSmall"
            style={{ color: product.inStock ? colors.green500 : colors.red500 }}
          >
            {product.inStock ? 'In Stock' : 'Out of Stock'}
          </Text>

          <View style={styles.footer}>
            <Text variant="titleMedium" style={styles.price}>
              {formatCurrency(product.price)}
            </Text>
            <Button
              mode="contained-tonal"
              compact
              disabled={!product.inStock}
              onPress={(event) => {
                event.stopPropagation?.();
                onAddToCart();
              }}
            >
              Add
            </Button>
          </View>
        </Card.Content>
      </Card>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  wrapper: {
    flex: 1,
    minWidth: '45%',
    maxWidth: '50%',
  },
  card: {
    marginBottom: 12,
  },
  image: {
    width: '100%',
    height: 140,
    borderTopLeftRadius: 12,
    borderTopRightRadius: 12,
  },
  content: {
    paddingTop: 12,
    gap: 4,
  },
  name: {
    fontWeight: '700',
  },
  brand: {
    color: colors.grey600,
  },
  ratingRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
  },
  ratingText: {
    marginLeft: 2,
  },
  reviewCount: {
    color: colors.grey600,
  },
  footer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 8,
  },
  price: {
    color: colors.primary,
    fontWeight: '700',
  },
});
