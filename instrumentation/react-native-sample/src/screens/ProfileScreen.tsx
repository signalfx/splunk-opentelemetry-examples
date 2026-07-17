import React from 'react';
import { StyleSheet, View } from 'react-native';
import { Button, Text } from 'react-native-paper';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { ScreenHeader } from '../components/ScreenHeader';
import { colors } from '../theme/colors';

interface ProfileScreenProps {
  onNavigateToOrders: () => void;
}

export function ProfileScreen({ onNavigateToOrders }: ProfileScreenProps) {
  return (
    <View style={styles.container}>
      <ScreenHeader title="Profile" />
      <View style={styles.content}>
        <MaterialCommunityIcons name="account-circle" size={100} color={colors.primary} />

        <Text variant="headlineSmall" style={styles.name}>
          Astronomy Enthusiast
        </Text>
        <Text variant="bodyMedium" style={styles.subtitle}>
          Welcome to AstronomyShop
        </Text>

        <Button mode="contained" icon="receipt" onPress={onNavigateToOrders} style={styles.button}>
          Order History
        </Button>
        <Button mode="outlined" icon="cog-outline" onPress={() => {}} style={styles.button}>
          Settings
        </Button>
        <Button mode="outlined" icon="help-circle-outline" onPress={() => {}} style={styles.button}>
          Help & Support
        </Button>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.white,
  },
  content: {
    flex: 1,
    alignItems: 'center',
    padding: 24,
    paddingTop: 32,
  },
  name: {
    marginTop: 16,
    fontWeight: '700',
  },
  subtitle: {
    marginTop: 4,
    color: colors.grey600,
  },
  button: {
    marginTop: 12,
    alignSelf: 'stretch',
  },
});
