import React from 'react';
import { HomePage } from './screens/HomePage';
import { Provider, rootStore } from './store/RootStore';

export const App: React.FunctionComponent<{}> = () => {
  return (
    <Provider value={rootStore}>
      <HomePage />      
    </Provider>
  );
};
