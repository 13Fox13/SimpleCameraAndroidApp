package com.simlpecamera.heartRateTracker.modules.pulse;

import android.graphics.Color;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.simlpecamera.heartRateTracker.modules.pulse.heartRateAlgorithm.pulseDetector.HeartRateHueFilter;
import com.simlpecamera.heartRateTracker.modules.pulse.heartRateAlgorithm.pulseDetector.HeartRatePulseDetector;

public final class HeartRateService {

    private final HeartRateHueFilter hueFilter = new HeartRateHueFilter();
    private final HeartRatePulseDetector pulseMainDetector = new HeartRatePulseDetector();

    private static int validFrameCounter = 0;
    private static int validFrameBufferSize = 70;
    private static double pulse = 0.0;
    private static double hrValue = 0.0;

    private int status = 0;

    public void setStatus(int newStatus) {
        this.status = newStatus;
    }

    public int getStatus() {
        return this.status;
    }

    public void setPulse(int newPulse) {
        this.pulse = this.pulse + newPulse;
    }

    public Double getPulse() {
        return this.pulse;
    }

    public void setHrValue(Double newHrValue) {
        this.hrValue = newHrValue;
    }

    public Double getHrValue() {
        return this.hrValue;
    }

    public void start() {
        setStatus(3);
    }

    public void stop() {
        setStatus(0);
    }

    public void finish() {
        setStatus(4);
    }

    public WritableMap handle(byte[] buffer) {
        WritableMap map = Arguments.createMap();
        map.putInt("hr_index", getStatus());

        if (getStatus() == 0) {
            validFrameCounter = 0;
            setHrValue(0.0);
            setPulse(0);
        }

        double time = System.currentTimeMillis();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int color = Color.argb(buffer[0] & 0xFF, buffer[1] & 0xFF, buffer[2] & 0xFF, buffer[3] & 0xFF);
                int red = Color.red(color) * 255;
                int green = Color.green(color) * 255;
                int blue = Color.blue(color) * 255;
            double hue = hueFilter.rgb2hsv(red, green, blue);

            Double filteredRough = hueFilter.butterworthBandpassRoughFilter(hue);
            Log.d("CAMERA_MODULE", " result hue: " + hue + " red: " + red + " green: " + green + " blue: " + blue);
            if (getStatus() == 3) {
                if (validFrameCounter < validFrameBufferSize/2) {
                    validFrameCounter += 1;
                    setPulse(pulseMainDetector.addNewValue(filteredRough, time));
                }
                setHrValue(getPulse());
            }
            if (getStatus() == 4) {
                validFrameCounter = 0;
                setHrValue(0.0);
                setPulse(0);
            }
            map.putDouble("hr_value", getHrValue());
        }
        return map;
    }
}
