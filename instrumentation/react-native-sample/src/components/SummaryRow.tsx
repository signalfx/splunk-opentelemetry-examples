import React from 'react';
import { StyleSheet, View, ViewStyle } from 'react-native';
import { Text } from 'react-native-paper';

interface SummaryRowProps {
  label: string;
  value: string;
  valueColor?: string;
  bold?: boolean;
  style?: ViewStyle;
}

export function SummaryRow({
  label,
  value,
  valueColor,
  bold = false,
  style,
}: SummaryRowProps) {
  return (
    <View style={[styles.row, style]}>
      <Text variant={bold ? 'titleMedium' : 'bodyMedium'}>{label}</Text>
      <Text
        variant={bold ? 'titleMedium' : 'bodyMedium'}
        style={[
          bold && styles.bold,
          valueColor ? { color: valueColor } : undefined,
        ]}
      >
        {value}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 2,
  },
  bold: {
    fontWeight: '700',
  },
});
