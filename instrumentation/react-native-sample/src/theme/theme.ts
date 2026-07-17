import { MD3LightTheme } from 'react-native-paper';
import { colors } from './colors';

export const theme = {
  ...MD3LightTheme,
  colors: {
    ...MD3LightTheme.colors,
    primary: colors.primary,
    onPrimary: colors.white,
    primaryContainer: '#C5CAE9',
    onPrimaryContainer: '#1A237E',
    secondary: colors.accent,
    onSecondary: '#000000',
    secondaryContainer: '#FFD8E4',
    onSecondaryContainer: '#31111D',
    background: colors.white,
    onBackground: colors.onSurface,
    surface: colors.white,
    onSurface: colors.onSurface,
    surfaceVariant: colors.grey100,
    onSurfaceVariant: colors.onSurfaceVariant,
    outline: colors.grey300,
    error: colors.red500,
    onError: colors.white,
  },
};
