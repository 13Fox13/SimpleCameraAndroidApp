import {
  applySnapshot,
  getEnv,
  getSnapshot,
  Instance,
  types,
} from "mobx-state-tree";
let measurementTimer: any;
let tipsTimer: any;

// @ts-ignore
type HeartRateStore = Instance<typeof HeartRateStore>;
// @ts-ignore
export const HeartRateStore = types
  .model("HeartRateStore", {
    rate: 0,
    measurementCount: 15,
    tipsIndex: 0,
    isMeasurementTimerStarted: false,
    stateIndex: 0,
  })
  .views((self: HeartRateStore) => ({
    get rateValue() {
      return self.rate || "0";
    },
    get time() {
      if (self.measurementCount === 0) {
        self.finishTracking();
      }
      return `${self.measurementCount} sec left`;
    },
  }))
  .actions((self) => {
    let initialHeartRateStore: any = {};

    return {
      afterCreate() {
        initialHeartRateStore = getSnapshot(self);
      },
      beforeDestroy() {
        this.resetStore();
      },
      changePermissions() {
        this.startTipsTimer();
      },
      async startTracking() {
        getEnv(self).heartRateService.startTracking();
        this.startTipsTimer();
      },
      stopTracking() {
        getEnv(self).heartRateService.stopTracking();
      },
      finishTracking() {
        getEnv(self).heartRateService.finishTracking();
        this.stopMeasurementTimer();
        this.stopTipsTimer();
      },
      onChangeHeartRateState({
        hr_value,
        hr_index,
      }: {
        hr_value: number;
        hr_index: number;
      }) {
        self.stateIndex = hr_index;
        if (hr_index !== 3) {
          this.stopMeasurementTimer();
          this.resetMeasurementTimer();
        }
        if (hr_index === 3) {
          self.rate = Math.floor(hr_value);
        }
        if (hr_index === 3 && !self.isMeasurementTimerStarted) {
          this.startMeasurementTimer();
        }
      },
      startMeasurementTimer() {
        measurementTimer = setInterval(() => {
          this.decrementMeasurementCount();
        }, 1000);
        self.isMeasurementTimerStarted = true;
      },
      decrementMeasurementCount() {
        if (self.stateIndex !== 3) {
          return;
        }
        if (self.measurementCount !== 0) {
          self.measurementCount -= 1;
        }
      },
      stopMeasurementTimer() {
        self.isMeasurementTimerStarted = false;
        clearInterval(measurementTimer);
      },
      resetMeasurementTimer() {
        self.measurementCount = 15;
      },
      startTipsTimer() {
        tipsTimer = setInterval(() => {
          this.setTipIndex();
        }, 2500);
      },
      setTipIndex() {
        if (self.tipsIndex + 1 < 4) {
          self.tipsIndex += 1;
        }
      },
      stopTipsTimer() {
        clearInterval(tipsTimer);
      },
      resetTipIndex() {
        self.tipsIndex = 0;
      },
      resetStore() {
        applySnapshot(self, initialHeartRateStore);
      },
    };
  });
