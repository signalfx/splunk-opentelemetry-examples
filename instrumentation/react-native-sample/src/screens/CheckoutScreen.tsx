import React, { useState } from 'react';
import { ScrollView, StyleSheet, View } from 'react-native';
import { Button, Card, Divider, Portal, Text, TextInput } from 'react-native-paper';
import { ScreenHeader } from '../components/ScreenHeader';
import { SummaryRow } from '../components/SummaryRow';
import { useShop } from '../store/ShopContext';
import { colors } from '../theme/colors';
import { formatCurrency } from '../utils/formatters';
import { calculateOrderPricing } from '../utils/pricing';

interface CheckoutScreenProps {
  onNavigateBack: () => void;
  onOrderPlaced: (customerName: string, customerEmail: string, orderTotal: string) => void;
}

function InfoField({ label, value }: { label: string; value: string }) {
  return (
    <View style={styles.infoField}>
      <Text variant="labelMedium" style={styles.infoLabel}>
        {label}
      </Text>
      <Text variant="bodyLarge">{value}</Text>
    </View>
  );
}

export function CheckoutScreen({ onNavigateBack, onOrderPlaced }: CheckoutScreenProps) {
  const { cartItems, createOrder, checkPaymentError, clearError } = useShop();
  const [fullName, setFullName] = useState('Alexia Johnson');
  const [phone, setPhone] = useState('+1 (555) 123-4567');
  const [isProcessing, setIsProcessing] = useState(false);
  const [showPaymentError, setShowPaymentError] = useState(false);

  const email = 'alexia.johnson@somerandomemail.com';
  const address = '123 Observatory Drive';
  const city = 'Sydney';
  const state = 'NSW';
  const zipCode = '2000';
  const country = 'Australia';
  const cardNumber = '4532 1234 5678 9012';
  const expiryDate = '12/28';
  const cvv = '123';
  const cardName = 'Alexia Johnson';

  const pricing = calculateOrderPricing(cartItems);

  const handlePlaceOrder = async () => {
    setIsProcessing(true);

    const paymentError = checkPaymentError();
    if (paymentError) {
      await new Promise((resolve) => setTimeout(resolve, 1500));
      setIsProcessing(false);
      setShowPaymentError(true);
      return;
    }

    await new Promise((resolve) => setTimeout(resolve, 2000));

    const shippingAddress = `${address}\n${city}, ${state} ${zipCode}\n${country}`;
    const lastFour = cardNumber.replace(/\s/g, '').slice(-4);
    const paymentMethod = `**** **** **** ${lastFour}`;

    createOrder(fullName, email, shippingAddress, paymentMethod);

    await new Promise((resolve) => setTimeout(resolve, 500));
    setIsProcessing(false);
    onOrderPlaced(fullName, email, formatCurrency(pricing.total));
  };

  return (
    <View style={styles.container}>
      <ScreenHeader title="Secure Checkout" onBack={onNavigateBack} />
      <ScrollView contentContainerStyle={styles.content}>
        <Text variant="titleMedium" style={styles.sectionTitle}>
          Shipping Information
        </Text>

        <TextInput
          label="Full Name"
          value={fullName}
          onChangeText={setFullName}
          mode="outlined"
          style={styles.input}
        />
        <InfoField label="Email" value={email} />
        <TextInput
          label="Phone"
          value={phone}
          onChangeText={setPhone}
          mode="outlined"
          keyboardType="phone-pad"
          style={styles.input}
        />
        <InfoField label="Address" value={address} />

        <View style={styles.row}>
          <View style={styles.halfField}>
            <InfoField label="City" value={city} />
          </View>
          <View style={styles.halfField}>
            <InfoField label="State" value={state} />
          </View>
        </View>

        <View style={styles.row}>
          <View style={styles.halfField}>
            <InfoField label="ZIP Code" value={zipCode} />
          </View>
          <View style={styles.halfField}>
            <InfoField label="Country" value={country} />
          </View>
        </View>

        <Divider style={styles.divider} />

        <Text variant="titleMedium" style={styles.sectionTitle}>
          Payment Information
        </Text>
        <InfoField label="Card Number" value={cardNumber} />
        <View style={styles.row}>
          <View style={styles.halfField}>
            <InfoField label="Expiry" value={expiryDate} />
          </View>
          <View style={styles.halfField}>
            <InfoField label="CVV" value={cvv} />
          </View>
        </View>
        <InfoField label="Cardholder Name" value={cardName} />

        <Divider style={styles.divider} />

        <Text variant="titleMedium" style={styles.sectionTitle}>
          Order Summary
        </Text>
        <Card style={styles.summaryCard}>
          <Card.Content>
            <Text variant="bodySmall" style={styles.itemCount}>
              {pricing.itemCount === 1 ? '1 item' : `${pricing.itemCount} items`}
            </Text>
            <SummaryRow label="Subtotal" value={formatCurrency(pricing.subtotal)} />
            <SummaryRow label="Tax (8.5%)" value={formatCurrency(pricing.tax)} />
            <SummaryRow
              label="Shipping"
              value={pricing.shipping === 0 ? 'FREE' : formatCurrency(pricing.shipping)}
              valueColor={pricing.shipping === 0 ? colors.green500 : undefined}
            />
            <Divider style={styles.summaryDivider} />
            <SummaryRow label="Total" value={formatCurrency(pricing.total)} bold />
          </Card.Content>
        </Card>

        <Button
          mode="contained"
          onPress={handlePlaceOrder}
          disabled={isProcessing || cartItems.length === 0}
          loading={isProcessing}
          style={styles.placeOrderButton}
        >
          {isProcessing ? 'Processing Payment...' : 'Place Order'}
        </Button>
      </ScrollView>

      <Portal>
        {showPaymentError ? (
          <View style={styles.dialogOverlay}>
            <Card style={styles.dialog}>
              <Card.Content>
                <Text variant="titleLarge" style={styles.dialogTitle}>
                  Payment Declined
                </Text>
                <Text variant="bodyMedium" style={styles.dialogText}>
                  Card Declined - Error Code: 4001{'\n\n'}(This is a test error)
                </Text>
                <View style={styles.dialogActions}>
                  <Button onPress={() => setShowPaymentError(false)}>Try Again</Button>
                  <Button
                    onPress={() => {
                      setShowPaymentError(false);
                      clearError();
                    }}
                  >
                    Clear Error
                  </Button>
                </View>
              </Card.Content>
            </Card>
          </View>
        ) : null}
      </Portal>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.white,
  },
  content: {
    padding: 16,
    paddingBottom: 32,
    gap: 12,
  },
  sectionTitle: {
    fontWeight: '700',
  },
  input: {
    backgroundColor: colors.white,
  },
  infoField: {
    marginBottom: 8,
  },
  infoLabel: {
    color: colors.grey600,
    marginBottom: 4,
  },
  row: {
    flexDirection: 'row',
    gap: 12,
  },
  halfField: {
    flex: 1,
  },
  divider: {
    marginVertical: 8,
  },
  summaryCard: {
    marginTop: 4,
  },
  itemCount: {
    color: colors.grey600,
    marginBottom: 8,
  },
  summaryDivider: {
    marginVertical: 8,
  },
  placeOrderButton: {
    marginTop: 8,
  },
  dialogOverlay: {
    position: 'absolute',
    top: 0,
    right: 0,
    bottom: 0,
    left: 0,
    backgroundColor: 'rgba(0,0,0,0.4)',
    alignItems: 'center',
    justifyContent: 'center',
    padding: 24,
  },
  dialog: {
    width: '100%',
    maxWidth: 420,
  },
  dialogTitle: {
    fontWeight: '700',
    marginBottom: 8,
  },
  dialogText: {
    marginBottom: 16,
  },
  dialogActions: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    gap: 8,
  },
});
