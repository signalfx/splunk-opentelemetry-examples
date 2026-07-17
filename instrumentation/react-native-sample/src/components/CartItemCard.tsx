import React from 'react';
import { Image, StyleSheet, View } from 'react-native';
import { Card, Text } from 'react-native-paper';
import { CartItem } from '../models/types';
import { colors } from '../theme/colors';
import { formatCurrency } from '../utils/formatters';

interface CartItemCardProps {
  cartItem: CartItem;
}

export function CartItemCard({ cartItem }: CartItemCardProps) {
  return (
    <Card style={styles.card}>
      <View style={styles.row}>
        <Image source={{ uri: cartItem.productImageUrl }} style={styles.image} />
        <View style={styles.details}>
          <Text variant="titleSmall" numberOfLines={2} style={styles.name}>
            {cartItem.productName}
          </Text>
          <Text variant="bodyMedium" style={styles.price}>
            {formatCurrency(cartItem.productPrice)}
          </Text>
          <View style={styles.footer}>
            <Text variant="bodyMedium" style={styles.quantity}>
              Qty: {cartItem.quantity}
            </Text>
            <Text variant="titleSmall" style={styles.lineTotal}>
              {formatCurrency(cartItem.productPrice * cartItem.quantity)}
            </Text>
          </View>
        </View>
      </View>
    </Card>
  );
}

const styles = StyleSheet.create({
  card: {
    marginBottom: 8,
  },
  row: {
    flexDirection: 'row',
    padding: 12,
  },
  image: {
    width: 80,
    height: 80,
    borderRadius: 8,
  },
  details: {
    flex: 1,
    marginLeft: 12,
  },
  name: {
    fontWeight: '700',
  },
  price: {
    color: colors.grey600,
    marginTop: 4,
  },
  footer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 8,
  },
  quantity: {
    color: colors.grey600,
  },
  lineTotal: {
    color: colors.primary,
    fontWeight: '700',
  },
});
