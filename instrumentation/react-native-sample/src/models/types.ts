export interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  imageUrl: string;
  category: string;
  brand: string;
  rating: number;
  reviewCount: number;
  inStock: boolean;
  specifications?: string;
}

export interface CartItem {
  id: string;
  productId: string;
  productName: string;
  productPrice: number;
  productImageUrl: string;
  quantity: number;
  addedAt: number;
}

export enum OrderStatus {
  PROCESSING = 'PROCESSING',
  CONFIRMED = 'CONFIRMED',
  SHIPPED = 'SHIPPED',
  DELIVERED = 'DELIVERED',
  CANCELLED = 'CANCELLED',
}

export interface Order {
  orderId: string;
  customerName: string;
  customerEmail: string;
  shippingAddress: string;
  paymentMethod: string;
  orderDate: number;
  status: OrderStatus;
  subtotal: number;
  tax: number;
  shipping: number;
  total: number;
  itemCount: number;
  estimatedDelivery: number;
}

export interface OrderItem {
  id: string;
  orderId: string;
  productId: string;
  productName: string;
  productPrice: number;
  productImageUrl: string;
  quantity: number;
  itemTotal: number;
}

export interface DummyProduct {
  id: number;
  title: string;
  description: string;
  price: number;
  rating: number;
  stock: number;
  brand: string | null;
  category: string;
  thumbnail: string;
}

export interface DummyProductsResponse {
  products: DummyProduct[];
  total: number;
}
