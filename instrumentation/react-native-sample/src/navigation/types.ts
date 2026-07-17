export type RootTabParamList = {
  HomeTab: undefined;
  ProductsTab: undefined;
  CartTab: undefined;
  OrdersTab: undefined;
  ProfileTab: undefined;
};

export type RootStackParamList = {
  MainTabs: undefined;
  ProductDetail: { productId: string };
  Checkout: undefined;
  OrderConfirmation: {
    customerName: string;
    customerEmail: string;
    orderTotal: string;
  };
};

export type ProductsStackParamList = {
  ProductsList: undefined;
  ProductDetail: { productId: string };
};

export type CartStackParamList = {
  CartMain: undefined;
  Checkout: undefined;
  OrderConfirmation: {
    customerName: string;
    customerEmail: string;
    orderTotal: string;
  };
};
