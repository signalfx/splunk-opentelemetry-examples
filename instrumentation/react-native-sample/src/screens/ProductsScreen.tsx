import React, { useState } from 'react';
import { ActivityIndicator, FlatList, StyleSheet, View } from 'react-native';
import { Searchbar, Text } from 'react-native-paper';
import { ProductCard } from '../components/ProductCard';
import { ScreenHeader } from '../components/ScreenHeader';
import { useShop } from '../store/ShopContext';
import { colors } from '../theme/colors';

interface ProductsScreenProps {
  onProductPress: (productId: string) => void;
}

export function ProductsScreen({ onProductPress }: ProductsScreenProps) {
  const { products, loading, loadProducts, searchProducts, addToCart } = useShop();
  const [searchQuery, setSearchQuery] = useState('');

  const handleSearchChange = (query: string) => {
    setSearchQuery(query);
    if (query.length >= 2) {
      searchProducts(query);
    } else if (query.length === 0) {
      loadProducts();
    }
  };

  return (
    <View style={styles.container}>
      <ScreenHeader title="Products" />
      <Searchbar
        placeholder="Search products..."
        value={searchQuery}
        onChangeText={handleSearchChange}
        style={styles.searchbar}
      />

      {loading && products.length === 0 ? (
        <View style={styles.centered}>
          <ActivityIndicator size="large" color={colors.primary} />
        </View>
      ) : products.length === 0 ? (
        <View style={styles.centered}>
          <Text variant="bodyLarge" style={styles.emptyText}>
            No products found
          </Text>
        </View>
      ) : (
        <FlatList
          data={products}
          keyExtractor={(item) => item.id}
          numColumns={2}
          columnWrapperStyle={styles.row}
          contentContainerStyle={styles.listContent}
          renderItem={({ item }) => (
            <ProductCard
              product={item}
              onPress={() => onProductPress(item.id)}
              onAddToCart={() => addToCart(item)}
            />
          )}
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.white,
  },
  searchbar: {
    marginHorizontal: 16,
    marginVertical: 8,
  },
  listContent: {
    paddingHorizontal: 16,
    paddingBottom: 16,
  },
  row: {
    justifyContent: 'space-between',
    gap: 12,
  },
  centered: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  emptyText: {
    color: colors.grey600,
  },
});
