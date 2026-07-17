import { ASTRONOMY_PRODUCTS } from '../constants/astronomyProducts';
import { PRODUCT_IMAGES } from '../constants/productImages';
import { SAMPLE_PRODUCTS } from '../constants/sampleProducts';
import { DummyProduct, DummyProductsResponse, Product } from '../models/types';

const BASE_URL = 'https://dummyjson.com';

function mapDummyProduct(dummy: DummyProduct): Product {
  const template = ASTRONOMY_PRODUCTS[(dummy.id - 1) % ASTRONOMY_PRODUCTS.length];
  return {
    id: dummy.id.toString(),
    name: template.name,
    description: template.description,
    price: dummy.price,
    imageUrl: PRODUCT_IMAGES[dummy.id % PRODUCT_IMAGES.length],
    category: template.category,
    brand: template.brand,
    rating: dummy.rating,
    reviewCount: 0,
    inStock: dummy.stock > 0,
    specifications: template.specifications,
  };
}

async function fetchProducts(path: string): Promise<Product[]> {
  const response = await fetch(`${BASE_URL}${path}`);
  if (!response.ok) {
    throw new Error('Failed to fetch products');
  }
  const data: DummyProductsResponse = await response.json();
  return data.products.map(mapDummyProduct);
}

export async function getProducts(limit = 15): Promise<Product[]> {
  try {
    return await fetchProducts(`/products?limit=${limit}`);
  } catch {
    return SAMPLE_PRODUCTS;
  }
}

export async function searchProducts(query: string): Promise<Product[]> {
  try {
    return await fetchProducts(`/products/search?q=${encodeURIComponent(query)}`);
  } catch {
    const lowerQuery = query.toLowerCase();
    return SAMPLE_PRODUCTS.filter(
      (product) =>
        product.name.toLowerCase().includes(lowerQuery) ||
        product.description.toLowerCase().includes(lowerQuery) ||
        product.brand.toLowerCase().includes(lowerQuery),
    );
  }
}
