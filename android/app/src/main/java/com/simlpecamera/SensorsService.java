package com.simlpecamera;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


public class SensorsService {

    private SensorManager sensorManager;
    private float accX = 0.0f;
    private float accY = 0.0f;
    private float accZ = 0.0f;
    private Boolean motion = false;

    public void init(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(accListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void destroy() {
        if (sensorManager != null)
        sensorManager.unregisterListener(accListener);
    }

    private SensorEventListener accListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int acc) {
        }

        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            checkMotion(x, y, z);
        }
    };

    private void checkMotion(final float x,
                             final float y,
                             final float z) {
        float delta = 0.3f;
        Boolean motionX = Math.abs(accX - x) > delta;
        Boolean motionY = Math.abs(accY - y) > delta;
        Boolean motionZ = Math.abs(accZ - z) > delta;

        motion = motionX || motionY || motionZ;
        accX = x;
        accY = y;
        accZ = z;
    }

    public Boolean getMotion() {
        return motion;
    }
}
