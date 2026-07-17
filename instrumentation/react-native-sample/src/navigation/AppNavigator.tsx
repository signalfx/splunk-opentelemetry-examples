import React from 'react';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Badge } from 'react-native-paper';
import { View } from 'react-native';
import type { BottomTabScreenProps } from '@react-navigation/bottom-tabs';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { CartScreen } from '../screens/CartScreen';
import { CheckoutScreen } from '../screens/CheckoutScreen';
import { HomeScreen } from '../screens/HomeScreen';
import { OrderConfirmationScreen } from '../screens/OrderConfirmationScreen';
import { OrderHistoryScreen } from '../screens/OrderHistoryScreen';
import { ProductDetailScreen } from '../screens/ProductDetailScreen';
import { ProductsScreen } from '../screens/ProductsScreen';
import { ProfileScreen } from '../screens/ProfileScreen';
import { useShop } from '../store/ShopContext';
import { colors } from '../theme/colors';
import {
  CartStackParamList,
  ProductsStackParamList,
  RootTabParamList,
} from './types';

const Tab = createBottomTabNavigator<RootTabParamList>();
const ProductsStack = createNativeStackNavigator<ProductsStackParamList>();
const CartStack = createNativeStackNavigator<CartStackParamList>();

function TabIcon({
  name,
  color,
  size,
  badgeCount,
}: {
  name: keyof typeof MaterialCommunityIcons.glyphMap;
  color: string;
  size: number;
  badgeCount?: number;
}) {
  return (
    <View>
      <MaterialCommunityIcons name={name} size={size} color={color} />
      {badgeCount && badgeCount > 0 ? (
        <Badge style={{ position: 'absolute', top: -4, right: -10 }} size={16}>
          {badgeCount}
        </Badge>
      ) : null}
    </View>
  );
}

function HomeTabScreen({ navigation }: BottomTabScreenProps<RootTabParamList, 'HomeTab'>) {
  return (
    <HomeScreen onBrowseProducts={() => navigation.navigate('ProductsTab')} />
  );
}

function OrdersTabScreen({ navigation }: BottomTabScreenProps<RootTabParamList, 'OrdersTab'>) {
  return (
    <OrderHistoryScreen onNavigateToProducts={() => navigation.navigate('ProductsTab')} />
  );
}

function ProfileTabScreen({ navigation }: BottomTabScreenProps<RootTabParamList, 'ProfileTab'>) {
  return (
    <ProfileScreen onNavigateToOrders={() => navigation.navigate('OrdersTab')} />
  );
}

function ProductsListScreen({
  navigation,
}: NativeStackScreenProps<ProductsStackParamList, 'ProductsList'>) {
  return (
    <ProductsScreen
      onProductPress={(productId) => navigation.navigate('ProductDetail', { productId })}
    />
  );
}

function ProductDetailRoute({
  route,
  navigation,
}: NativeStackScreenProps<ProductsStackParamList, 'ProductDetail'>) {
  return (
    <ProductDetailScreen
      productId={route.params.productId}
      onNavigateBack={() => navigation.goBack()}
      onNavigateToCart={() => navigation.getParent()?.navigate('CartTab')}
    />
  );
}

function ProductsNavigator() {
  return (
    <ProductsStack.Navigator screenOptions={{ headerShown: false }}>
      <ProductsStack.Screen name="ProductsList" component={ProductsListScreen} />
      <ProductsStack.Screen name="ProductDetail" component={ProductDetailRoute} />
    </ProductsStack.Navigator>
  );
}

function CartMainScreen({
  navigation,
}: NativeStackScreenProps<CartStackParamList, 'CartMain'>) {
  return (
    <CartScreen
      onNavigateToProducts={() => navigation.getParent()?.navigate('ProductsTab')}
      onNavigateToCheckout={() => navigation.navigate('Checkout')}
    />
  );
}

function CheckoutRoute({
  navigation,
}: NativeStackScreenProps<CartStackParamList, 'Checkout'>) {
  return (
    <CheckoutScreen
      onNavigateBack={() => navigation.goBack()}
      onOrderPlaced={(customerName, customerEmail, orderTotal) =>
        navigation.navigate('OrderConfirmation', {
          customerName,
          customerEmail,
          orderTotal,
        })
      }
    />
  );
}

function OrderConfirmationRoute({
  route,
  navigation,
}: NativeStackScreenProps<CartStackParamList, 'OrderConfirmation'>) {
  return (
    <OrderConfirmationScreen
      customerName={route.params.customerName}
      customerEmail={route.params.customerEmail}
      orderTotal={route.params.orderTotal}
      onContinueShopping={() => {
        navigation.popToTop();
        navigation.getParent()?.navigate('ProductsTab');
      }}
    />
  );
}

function CartNavigator() {
  return (
    <CartStack.Navigator screenOptions={{ headerShown: false }}>
      <CartStack.Screen name="CartMain" component={CartMainScreen} />
      <CartStack.Screen name="Checkout" component={CheckoutRoute} />
      <CartStack.Screen name="OrderConfirmation" component={OrderConfirmationRoute} />
    </CartStack.Navigator>
  );
}

function MainTabs() {
  const { cartItemCount } = useShop();

  return (
    <Tab.Navigator
      screenOptions={{
        headerShown: false,
        tabBarActiveTintColor: colors.primary,
        tabBarInactiveTintColor: colors.grey600,
      }}
    >
      <Tab.Screen
        name="HomeTab"
        component={HomeTabScreen}
        options={{
          tabBarLabel: 'Home',
          tabBarIcon: ({ color, size }) => (
            <MaterialCommunityIcons name="home" size={size} color={color} />
          ),
        }}
      />

      <Tab.Screen
        name="ProductsTab"
        component={ProductsNavigator}
        options={{
          tabBarLabel: 'Products',
          tabBarIcon: ({ color, size }) => (
            <MaterialCommunityIcons name="magnify" size={size} color={color} />
          ),
        }}
      />

      <Tab.Screen
        name="CartTab"
        component={CartNavigator}
        options={{
          tabBarLabel: 'Cart',
          tabBarIcon: ({ color, size }) => (
            <TabIcon name="cart-outline" color={color} size={size} badgeCount={cartItemCount} />
          ),
        }}
      />

      <Tab.Screen
        name="OrdersTab"
        component={OrdersTabScreen}
        options={{
          tabBarLabel: 'Orders',
          tabBarIcon: ({ color, size }) => (
            <MaterialCommunityIcons name="receipt" size={size} color={color} />
          ),
        }}
      />

      <Tab.Screen
        name="ProfileTab"
        component={ProfileTabScreen}
        options={{
          tabBarLabel: 'Profile',
          tabBarIcon: ({ color, size }) => (
            <MaterialCommunityIcons name="account-outline" size={size} color={color} />
          ),
        }}
      />
    </Tab.Navigator>
  );
}

export function AppNavigator() {
  return <MainTabs />;
}
