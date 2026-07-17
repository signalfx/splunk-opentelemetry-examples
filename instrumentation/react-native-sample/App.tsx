import { NavigationContainer } from '@react-navigation/native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { StatusBar } from 'expo-status-bar';
import React, { Component, type ErrorInfo, type ReactNode } from 'react';
import { ScrollView, StyleSheet, Text, View } from 'react-native';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import { PaperProvider } from 'react-native-paper';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { AppNavigator } from './src/navigation/AppNavigator';
import { ShopProvider } from './src/store/ShopContext';
import { colors } from './src/theme/colors';
import { theme } from './src/theme/theme';

interface ErrorBoundaryProps {
  children: ReactNode;
}

interface ErrorBoundaryState {
  error: Error | null;
}

class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  state: ErrorBoundaryState = { error: null };

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return { error };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error('App crash:', error, info.componentStack);
  }

  render() {
    if (this.state.error) {
      return (
        <View style={styles.errorContainer}>
          <Text style={styles.errorTitle}>Something went wrong</Text>
          <ScrollView style={styles.errorScroll}>
            <Text style={styles.errorMessage}>{this.state.error.message}</Text>
            {this.state.error.stack ? (
              <Text style={styles.errorStack}>{this.state.error.stack}</Text>
            ) : null}
          </ScrollView>
        </View>
      );
    }

    return this.props.children;
  }
}

export default function App() {
  return (
    <GestureHandlerRootView style={styles.root}>
      <SafeAreaProvider>
        <ErrorBoundary>
          <PaperProvider
            theme={theme}
            settings={{
              icon: (props) => <MaterialCommunityIcons {...props} />,
            }}
          >
            <ShopProvider>
              <NavigationContainer>
                <AppNavigator />
                <StatusBar style="light" />
              </NavigationContainer>
            </ShopProvider>
          </PaperProvider>
        </ErrorBoundary>
      </SafeAreaProvider>
    </GestureHandlerRootView>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
  },
  errorContainer: {
    flex: 1,
    padding: 24,
    paddingTop: 64,
    backgroundColor: colors.white,
  },
  errorTitle: {
    fontSize: 20,
    fontWeight: '700',
    marginBottom: 12,
    color: colors.red500,
  },
  errorScroll: {
    flex: 1,
  },
  errorMessage: {
    fontSize: 16,
    marginBottom: 12,
    color: colors.onSurface,
  },
  errorStack: {
    fontSize: 12,
    color: colors.grey600,
    fontFamily: 'monospace',
  },
});
