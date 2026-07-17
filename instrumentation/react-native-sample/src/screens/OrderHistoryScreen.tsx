import React, { useEffect } from 'react';
import { ActivityIndicator, FlatList, StyleSheet, View } from 'react-native';
import { Text } from 'react-native-paper';
import { EmptyState } from '../components/EmptyState';
import { OrderCard } from '../components/OrderCard';
import { ScreenHeader } from '../components/ScreenHeader';
import { useShop } from '../store/ShopContext';
import { colors } from '../theme/colors';

interface OrderHistoryScreenProps {
  onNavigateToProducts: () => void;
}

export function OrderHistoryScreen({ onNavigateToProducts }: OrderHistoryScreenProps) {
  const { orders, loading, loadOrders } = useShop();

  useEffect(() => {
    loadOrders();
  }, [loadOrders]);

  return (
    <View style={styles.container}>
      <ScreenHeader title="Order History" />

      {loading && orders.length === 0 ? (
        <View style={styles.centered}>
          <ActivityIndicator size="large" color={colors.primary} />
        </View>
      ) : orders.length === 0 ? (
        <EmptyState
          icon="receipt"
          title="No orders yet"
          description="Start shopping to see your order history here!"
          actionLabel="Browse Products"
          onAction={onNavigateToProducts}
        />
      ) : (
        <FlatList
          data={orders}
          keyExtractor={(item) => item.orderId}
          contentContainerStyle={styles.listContent}
          ListHeaderComponent={
            <Text variant="titleMedium" style={styles.header}>
              You have {orders.length} order{orders.length !== 1 ? 's' : ''}
            </Text>
          }
          renderItem={({ item }) => <OrderCard order={item} />}
        />
      )}
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
  listContent: {
    padding: 16,
  },
  header: {
    fontWeight: '700',
    marginBottom: 12,
  },
});
