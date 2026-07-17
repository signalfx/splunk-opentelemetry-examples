import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';
import { getProducts, searchProducts } from '../api/productsApi';
import {
  CartItem,
  Order,
  OrderItem,
  OrderStatus,
  Product,
} from '../models/types';
import { generateOrderId } from '../utils/formatters';
import { calculateOrderPricing } from '../utils/pricing';

interface ShopContextValue {
  products: Product[];
  cartItems: CartItem[];
  cartItemCount: number;
  orders: Order[];
  orderItems: Record<string, OrderItem[]>;
  loading: boolean;
  error: string | null;
  loadProducts: () => Promise<void>;
  searchProducts: (query: string) => Promise<void>;
  addToCart: (product: Product) => void;
  createOrder: (
    customerName: string,
    customerEmail: string,
    shippingAddress: string,
    paymentMethod: string,
  ) => Order | null;
  loadOrders: () => void;
  checkPaymentError: () => string | null;
  clearError: () => void;
}

const ShopContext = createContext<ShopContextValue | null>(null);

function createSampleOrder(): { order: Order; items: OrderItem[] } {
  const orderId = 'AS00123456';
  const order: Order = {
    orderId,
    customerName: 'Alex Johnson',
    customerEmail: 'alex.johnson@email.com',
    shippingAddress: '123 Observatory Drive\nSydney, NSW 2000\nAustralia',
    paymentMethod: '**** **** **** 9012',
    orderDate: Date.now() - 2 * 24 * 60 * 60 * 1000,
    status: OrderStatus.SHIPPED,
    subtotal: 649.99,
    tax: 55.25,
    shipping: 0,
    total: 705.24,
    itemCount: 1,
    estimatedDelivery: Date.now() + 24 * 60 * 60 * 1000,
  };

  const items: OrderItem[] = [
    {
      id: 'sample-item-1',
      orderId,
      productId: '1',
      productName: 'Celestron NexStar 127SLT Telescope',
      productPrice: 649.99,
      productImageUrl:
        'https://images.unsplash.com/photo-1446941611757-91d2c3bd3d45?w=500&h=500&fit=crop',
      quantity: 1,
      itemTotal: 649.99,
    },
  ];

  return { order, items };
}

export function ShopProvider({ children }: { children: React.ReactNode }) {
  const [products, setProducts] = useState<Product[]>([]);
  const [cartItems, setCartItems] = useState<CartItem[]>([]);
  const [orders, setOrders] = useState<Order[]>([]);
  const [orderItems, setOrderItems] = useState<Record<string, OrderItem[]>>({});
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const simulatePaymentError = useRef(false);
  const ordersInitialized = useRef(false);

  const cartItemCount = useMemo(
    () => cartItems.reduce((sum, item) => sum + item.quantity, 0),
    [cartItems],
  );

  const loadProducts = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await getProducts(15);
      setProducts(result);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load products');
      setProducts([]);
    } finally {
      setLoading(false);
    }
  }, []);

  const handleSearchProducts = useCallback(async (query: string) => {
    setLoading(true);
    setError(null);
    try {
      const result = await searchProducts(query);
      setProducts(result);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Search failed');
    } finally {
      setLoading(false);
    }
  }, []);

  const addToCart = useCallback((product: Product) => {
    setCartItems((prev) => {
      const existingIndex = prev.findIndex((item) => item.productId === product.id);
      if (existingIndex !== -1) {
        const updated = [...prev];
        updated[existingIndex] = {
          ...updated[existingIndex],
          quantity: updated[existingIndex].quantity + 1,
        };
        return updated;
      }

      return [
        ...prev,
        {
          id: `${product.id}_${Date.now()}`,
          productId: product.id,
          productName: product.name,
          productPrice: product.price,
          productImageUrl: product.imageUrl,
          quantity: 1,
          addedAt: Date.now(),
        },
      ];
    });
  }, []);

  const createOrder = useCallback(
    (
      customerName: string,
      customerEmail: string,
      shippingAddress: string,
      paymentMethod: string,
    ): Order | null => {
      if (cartItems.length === 0) {
        return null;
      }

      const pricing = calculateOrderPricing(cartItems);
      const orderId = generateOrderId();

      const order: Order = {
        orderId,
        customerName,
        customerEmail,
        shippingAddress,
        paymentMethod,
        orderDate: Date.now(),
        status: OrderStatus.PROCESSING,
        subtotal: pricing.subtotal,
        tax: pricing.tax,
        shipping: pricing.shipping,
        total: pricing.total,
        itemCount: pricing.itemCount,
        estimatedDelivery: Date.now() + 3 * 24 * 60 * 60 * 1000,
      };

      const items: OrderItem[] = cartItems.map((cartItem) => ({
        id: `${orderId}_${cartItem.productId}`,
        orderId,
        productId: cartItem.productId,
        productName: cartItem.productName,
        productPrice: cartItem.productPrice,
        productImageUrl: cartItem.productImageUrl,
        quantity: cartItem.quantity,
        itemTotal: cartItem.productPrice * cartItem.quantity,
      }));

      setOrders((prev) => [order, ...prev]);
      setOrderItems((prev) => ({ ...prev, [orderId]: items }));
      setCartItems([]);

      return order;
    },
    [cartItems],
  );

  const loadOrders = useCallback(() => {
    setLoading(true);
    if (!ordersInitialized.current) {
      const { order, items } = createSampleOrder();
      setOrders([order]);
      setOrderItems({ [order.orderId]: items });
      ordersInitialized.current = true;
    }
    setLoading(false);
  }, []);

  const checkPaymentError = useCallback((): string | null => {
    if (simulatePaymentError.current) {
      return 'Payment Failed: Credit card declined (Error Code: 4001)';
    }
    return null;
  }, []);

  const clearError = useCallback(() => {
    setError(null);
    simulatePaymentError.current = false;
  }, []);

  useEffect(() => {
    loadProducts();
  }, [loadProducts]);

  const value = useMemo(
    () => ({
      products,
      cartItems,
      cartItemCount,
      orders,
      orderItems,
      loading,
      error,
      loadProducts,
      searchProducts: handleSearchProducts,
      addToCart,
      createOrder,
      loadOrders,
      checkPaymentError,
      clearError,
    }),
    [
      products,
      cartItems,
      cartItemCount,
      orders,
      orderItems,
      loading,
      error,
      loadProducts,
      handleSearchProducts,
      addToCart,
      createOrder,
      loadOrders,
      checkPaymentError,
      clearError,
    ],
  );

  return <ShopContext.Provider value={value}>{children}</ShopContext.Provider>;
}

export function useShop(): ShopContextValue {
  const context = useContext(ShopContext);
  if (!context) {
    throw new Error('useShop must be used within ShopProvider');
  }
  return context;
}
