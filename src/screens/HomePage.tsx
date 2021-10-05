import React from "react";
import { useEffect } from "react";
import { NativeEventEmitter, Linking } from "react-native";
import {
  Button,
  ResultContainer,
  Spacer,
  ButtonTitle,
  Wrapper,
  Result,
  NeedAccessLabel,
} from "./HomePage.styles";
import { observer } from "mobx-react-lite";
import { HeartRateModule } from "../utils/common";
import { useMst } from "../store/RootStore";
import { BTN, needAccess } from "../utils/constants";

const HeartRateImplModuleEvents = new NativeEventEmitter(HeartRateModule);

export const HomePage = observer(() => {
  const { heartRateStore } = useMst();

  useEffect(() => {
    HeartRateImplModuleEvents.addListener("onHeartRateState", (res) =>
      heartRateStore.onChangeHeartRateState(res)
    );

    return () => {
      HeartRateImplModuleEvents.removeAllListeners("onHeartRateState");
    };
  }, [heartRateStore]);

  const startTracking = () => heartRateStore.startTracking();

  const stopTracking = () => heartRateStore.stopTracking();

  const goToSettings = () => Linking.openSettings();

  return (
    <Wrapper>
      <ResultContainer>
        <Result>{heartRateStore.rateValue}</Result>
      </ResultContainer>
      <Spacer />
      <Button onPress={startTracking}>
        <ButtonTitle>{BTN.START}</ButtonTitle>
      </Button>
      <Spacer />
      <Button onPress={stopTracking}>
        <ButtonTitle>{BTN.STOP}</ButtonTitle>
      </Button>
      <Spacer />
      <Button onPress={goToSettings}>
        <ButtonTitle>{BTN.SETTINGS}</ButtonTitle>
      </Button>
      <NeedAccessLabel>{needAccess}</NeedAccessLabel>
    </Wrapper>
  );
});
