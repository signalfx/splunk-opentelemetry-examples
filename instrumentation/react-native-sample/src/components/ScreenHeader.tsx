import React from 'react';
import { StyleSheet } from 'react-native';
import { Appbar } from 'react-native-paper';
import { colors } from '../theme/colors';

interface ScreenHeaderProps {
  title: string;
  onBack?: () => void;
}

export function ScreenHeader({ title, onBack }: ScreenHeaderProps) {
  return (
    <Appbar.Header style={styles.header} elevated>
      {onBack ? <Appbar.BackAction onPress={onBack} color={colors.white} /> : null}
      <Appbar.Content title={title} titleStyle={styles.title} />
    </Appbar.Header>
  );
}

const styles = StyleSheet.create({
  header: {
    backgroundColor: colors.primary,
  },
  title: {
    color: colors.white,
    fontWeight: '600',
  },
});
