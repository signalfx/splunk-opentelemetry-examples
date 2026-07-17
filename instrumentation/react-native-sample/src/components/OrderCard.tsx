import React from 'react';
import { StyleSheet, View } from 'react-native';
import { Card, Text } from 'react-native-paper';
import { Order } from '../models/types';
import { colors } from '../theme/colors';
import { formatCurrency, formatDate } from '../utils/formatters';
import { StatusChip } from './StatusChip';

interface OrderCardProps {
  order: Order;
}

export function OrderCard({ order }: OrderCardProps) {
  return (
    <Card style={styles.card}>
      <Card.Content>
        <View style={styles.header}>
          <Text variant="titleSmall" style={styles.orderId}>
            Order #{order.orderId}
          </Text>
          <StatusChip status={order.status} />
        </View>

        <Text variant="bodySmall" style={styles.date}>
          {formatDate(order.orderDate)}
        </Text>

        <View style={styles.footer}>
          <Text variant="bodyMedium">
            {order.itemCount} item{order.itemCount !== 1 ? 's' : ''}
          </Text>
          <Text variant="titleSmall" style={styles.total}>
            {formatCurrency(order.total)}
          </Text>
        </View>
      </Card.Content>
    </Card>
  );
}

const styles = StyleSheet.create({
  card: {
    marginBottom: 12,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  orderId: {
    fontWeight: '700',
  },
  date: {
    color: colors.grey600,
    marginTop: 8,
  },
  footer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 4,
  },
  total: {
    color: colors.primary,
    fontWeight: '700',
  },
});
