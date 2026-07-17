import React from 'react';
import { FlatList, StyleSheet, View } from 'react-native';
import { Button, Card, Divider, Text } from 'react-native-paper';
import { CartItemCard } from '../components/CartItemCard';
import { EmptyState } from '../components/EmptyState';
import { ScreenHeader } from '../components/ScreenHeader';
import { SummaryRow } from '../components/SummaryRow';
import { useShop } from '../store/ShopContext';
import { colors } from '../theme/colors';
import { formatCurrency } from '../utils/formatters';
import { calculateOrderPricing } from '../utils/pricing';

interface CartScreenProps {
  onNavigateToProducts: () => void;
  onNavigateToCheckout: () => void;
}

export function CartScreen({ onNavigateToProducts, onNavigateToCheckout }: CartScreenProps) {
  const { cartItems } = useShop();
  const pricing = calculateOrderPricing(cartItems);

  if (cartItems.length === 0) {
    return (
      <View style={styles.container}>
        <ScreenHeader title="Cart" />
        <EmptyState
          icon="cart-outline"
          title="Your cart is empty"
          description="Browse our products and add items to get started"
          actionLabel="Continue Shopping"
          onAction={onNavigateToProducts}
        />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <ScreenHeader title="Cart" />
      <Text variant="titleMedium" style={styles.itemCount}>
        {pricing.itemCount === 1 ? '1 item' : `${pricing.itemCount} items`}
      </Text>

      <FlatList
        data={cartItems}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.listContent}
        renderItem={({ item }) => <CartItemCard cartItem={item} />}
      />

      <Card style={styles.summaryCard}>
        <Card.Content>
          <SummaryRow label="Subtotal" value={formatCurrency(pricing.subtotal)} />
          <SummaryRow label="Tax (8.5%)" value={formatCurrency(pricing.tax)} />
          <SummaryRow
            label="Shipping"
            value={pricing.shipping === 0 ? 'FREE' : formatCurrency(pricing.shipping)}
            valueColor={pricing.shipping === 0 ? colors.green500 : undefined}
          />
          {pricing.savings !== null ? (
            <SummaryRow
              label="You save"
              value={formatCurrency(pricing.savings)}
              valueColor={colors.green500}
            />
          ) : null}
          <Divider style={styles.divider} />
          <SummaryRow
            label="Total"
            value={formatCurrency(pricing.total)}
            bold
          />
          <Button mode="contained" onPress={onNavigateToCheckout} style={styles.checkoutButton}>
            Proceed to Checkout
          </Button>
        </Card.Content>
      </Card>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.white,
  },
  itemCount: {
    paddingHorizontal: 16,
    paddingVertical: 8,
  },
  listContent: {
    paddingHorizontal: 16,
    paddingBottom: 8,
  },
  summaryCard: {
    margin: 16,
  },
  divider: {
    marginVertical: 8,
  },
  checkoutButton: {
    marginTop: 12,
  },
});
