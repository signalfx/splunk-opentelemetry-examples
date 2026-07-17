import React from 'react';
import { StyleSheet, View } from 'react-native';
import { Button, Text } from 'react-native-paper';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { ScreenHeader } from '../components/ScreenHeader';
import { colors } from '../theme/colors';

interface HomeScreenProps {
  onBrowseProducts: () => void;
}

export function HomeScreen({ onBrowseProducts }: HomeScreenProps) {
  return (
    <View style={styles.container}>
      <ScreenHeader title="Astronomy Shop" />
      <View style={styles.content}>
        <MaterialCommunityIcons name="star" size={96} color={colors.primary} />
        <Text variant="headlineLarge" style={styles.title}>
          Welcome to{'\n'}Astronomy Shop
        </Text>
        <Text variant="bodyLarge" style={styles.subtitle}>
          Your destination for telescopes, eyepieces, accessories, and everything you need to
          explore the night sky.
        </Text>
        <Button mode="contained" onPress={onBrowseProducts} style={styles.button}>
          Browse Products
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
    justifyContent: 'center',
    padding: 32,
  },
  title: {
    marginTop: 24,
    fontWeight: '700',
    textAlign: 'center',
  },
  subtitle: {
    marginTop: 12,
    textAlign: 'center',
    color: colors.grey600,
  },
  button: {
    marginTop: 32,
    alignSelf: 'stretch',
  },
});
