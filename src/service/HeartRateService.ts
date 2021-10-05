import { NativeEventEmitter } from "react-native";
import { HeartRateModule } from "../utils/common";

export const HeartRateEventEmitter = new NativeEventEmitter(HeartRateModule);

class HeartRate {
  private heartRateModule: any;

  constructor(heartRateModule: any) {
    this.heartRateModule = heartRateModule;
  }
  public startTracking() {
    this.heartRateModule.startTracking();
  }
  public stopTracking() {
    this.heartRateModule.stopTracking();
  }
  public finishTracking() {
    this.heartRateModule.finishTracking();
  }
}

export const HeartRateService = new HeartRate(HeartRateModule);
