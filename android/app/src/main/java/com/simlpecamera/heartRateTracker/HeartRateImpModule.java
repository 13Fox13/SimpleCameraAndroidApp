package com.simlpecamera.heartRateTracker;

import com.facebook.react.bridge.WritableMap;
import com.simlpecamera.heartRateTracker.modules.pulse.HeartRateService;

public class HeartRateImpModule {
    private final HeartRateService heartRateService = new HeartRateService();

    public void startTracking() {
        heartRateService.start();
    }

    public void stopTracking() {
        heartRateService.stop();
    }

    public void finishTracking() {
        heartRateService.finish();
    }

    public WritableMap handle(byte[] buffer) {
        return heartRateService.handle(buffer);
    }
}
