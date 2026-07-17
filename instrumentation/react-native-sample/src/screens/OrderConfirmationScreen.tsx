import React from 'react';
import { ScrollView, StyleSheet, View } from 'react-native';
import { Button, Card, Text } from 'react-native-paper';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { ScreenHeader } from '../components/ScreenHeader';
import { colors } from '../theme/colors';
import { formatDate, formatDateTime } from '../utils/formatters';

interface OrderConfirmationScreenProps {
  customerName: string;
  customerEmail: string;
  orderTotal: string;
  onContinueShopping: () => void;
}

function ConfirmationDetail({ label, value }: { label: string; value: string }) {
  return (
    <View style={styles.detailRow}>
      <Text variant="bodyMedium" style={styles.detailLabel}>
        {label}
      </Text>
      <Text variant="bodyMedium" style={styles.detailValue}>
        {value}
      </Text>
    </View>
  );
}

export function OrderConfirmationScreen({
  customerName,
  orderTotal,
  onContinueShopping,
}: OrderConfirmationScreenProps) {
  const orderNumber = `AS${Date.now().toString().slice(-8)}`;
  const currentDate = formatDateTime(Date.now());
  const estimatedDelivery = formatDate(Date.now() + 3 * 24 * 60 * 60 * 1000);

  return (
    <View style={styles.container}>
      <ScreenHeader title="Order Confirmation" />
      <ScrollView contentContainerStyle={styles.content}>
        <MaterialCommunityIcons name="check-circle" size={80} color={colors.green500} />

        <Text variant="headlineSmall" style={styles.title}>
          Thank you, {customerName}!
        </Text>
        <Text variant="bodyLarge" style={styles.subtitle}>
          Your order has been placed successfully
        </Text>

        <Card style={styles.card}>
          <Card.Content>
            <ConfirmationDetail label="Order Number" value={`#${orderNumber}`} />
            <ConfirmationDetail label="Date" value={currentDate} />
            <ConfirmationDetail label="Total" value={orderTotal} />
            <ConfirmationDetail label="Estimated Delivery" value={estimatedDelivery} />
          </Card.Content>
        </Card>

        <Button mode="contained" onPress={onContinueShopping} style={styles.button}>
          Continue Shopping
        </Button>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.white,
  },
  content: {
    alignItems: 'center',
    padding: 24,
  },
  title: {
    marginTop: 16,
    fontWeight: '700',
    textAlign: 'center',
  },
  subtitle: {
    marginTop: 8,
    textAlign: 'center',
    color: colors.grey600,
  },
  card: {
    width: '100%',
    marginTop: 24,
  },
  detailRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingVertical: 6,
  },
  detailLabel: {
    color: colors.grey600,
  },
  detailValue: {
    fontWeight: '700',
  },
  button: {
    marginTop: 32,
    alignSelf: 'stretch',
  },
});
