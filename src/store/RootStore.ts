import { useContext, createContext } from "react";
import {
  types,
  Instance,
  getSnapshot,
  applySnapshot,
  flow,
} from "mobx-state-tree";
import { HeartRateStore } from "./HeartRateStore";
import { HeartRateService } from "../service/HeartRateService";

const RootModel = types
  .model({
    heartRateStore: HeartRateStore,
  })
  .actions((self) => {
    let initialState = {};
    return {
      afterCreate: flow(function* () {
        try {
          initialState = getSnapshot(self);
        } catch (error) {
          console.log(error);
        }
      }),
      resetStore() {
        applySnapshot(self, initialState);
      },
    };
  });

export const createStore = (): RootInstance => {
  const heartRateStore = HeartRateStore.create();
  const rootStore = RootModel.create(
    {
      heartRateStore,
    },
    {
      heartRateService: HeartRateService,
    }
  );

  return rootStore;
};

export const rootStore = createStore();

export type RootInstance = Instance<typeof RootModel>;
const RootStoreContext = createContext<null | RootInstance>(null);

export const { Provider } = RootStoreContext;

export function useMst() {
  const store = useContext(RootStoreContext);
  if (store === null) {
    throw new Error("Store cannot be null, please add a context provider");
  }
  return store;
}
