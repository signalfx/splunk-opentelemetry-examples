import { CartItem } from '../models/types';

export interface OrderPricing {
  subtotal: number;
  tax: number;
  shipping: number;
  total: number;
  itemCount: number;
  savings: number | null;
}

export function calculateOrderPricing(cartItems: CartItem[]): OrderPricing {
  const itemCount = cartItems.reduce((sum, item) => sum + item.quantity, 0);
  const subtotal = cartItems.reduce(
    (sum, item) => sum + item.productPrice * item.quantity,
    0,
  );
  const tax = subtotal * 0.085;
  const shipping = subtotal >= 50 ? 0 : 9.99;
  const total = subtotal + tax + shipping;
  const savings = subtotal > 200 ? subtotal * 0.05 : null;

  return { subtotal, tax, shipping, total, itemCount, savings };
}
