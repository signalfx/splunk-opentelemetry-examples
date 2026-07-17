import 'react-native-gesture-handler';
import { registerRootComponent } from 'expo';
import { enableScreens } from 'react-native-screens';

import App from './App';

enableScreens();

registerRootComponent(App);
