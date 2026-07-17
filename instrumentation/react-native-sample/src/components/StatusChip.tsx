import React from 'react';
import { StyleSheet } from 'react-native';
import { Chip } from 'react-native-paper';
import { OrderStatus } from '../models/types';
import { colors } from '../theme/colors';

interface StatusChipProps {
  status: OrderStatus;
}

const STATUS_LABELS: Record<OrderStatus, string> = {
  [OrderStatus.PROCESSING]: 'Processing',
  [OrderStatus.CONFIRMED]: 'Confirmed',
  [OrderStatus.SHIPPED]: 'Shipped',
  [OrderStatus.DELIVERED]: 'Delivered',
  [OrderStatus.CANCELLED]: 'Cancelled',
};

const STATUS_COLORS: Record<OrderStatus, string> = {
  [OrderStatus.PROCESSING]: colors.grey600,
  [OrderStatus.CONFIRMED]: colors.primary,
  [OrderStatus.SHIPPED]: colors.green500,
  [OrderStatus.DELIVERED]: colors.green500,
  [OrderStatus.CANCELLED]: colors.red500,
};

export function StatusChip({ status }: StatusChipProps) {
  return (
    <Chip compact style={styles.chip} textStyle={{ color: STATUS_COLORS[status] }}>
      {STATUS_LABELS[status]}
    </Chip>
  );
}

const styles = StyleSheet.create({
  chip: {
    backgroundColor: 'transparent',
  },
});
