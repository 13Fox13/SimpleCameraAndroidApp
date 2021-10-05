package com.simlpecamera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.modules.core.PermissionAwareActivity;
import com.simlpecamera.heartRateTracker.HeartRateImpModule;

import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;
import java.util.Collections;

public class CameraModule extends ReactContextBaseJavaModule {
    private MediaCodec mediaCodec;
    private Surface encoderSurface;
    private CameraManager cameraManager;
    private CaptureRequest.Builder captureBuilder;
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private SensorManager sensorManager;
    private float accX = 0.0f;
    private float accY = 0.0f;
    private float accZ = 0.0f;
    private Boolean motion = false;

    private HeartRateImpModule heartRateImpModule = new HeartRateImpModule();


    public CameraModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @NonNull
    @Override
    public String getName() {
        return "CameraModule";
    }

    @ReactMethod
    public boolean permissions() {
        PermissionAwareActivity activity = (PermissionAwareActivity) getCurrentActivity();

        if (ContextCompat.checkSelfPermission(getReactApplicationContext(), Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_DENIED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(new String[]{Manifest.permission.CAMERA}, 100,
                        (requestCode, permissions, grantResults) -> {
                            if (requestCode == 100) {
                                if (grantResults.length > 0
                                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                            return false;
                        });
            } else {
                return false;
            }
        } else {
            return true;
        }
        return false;
    }

    @ReactMethod()
    public void startTracking(Promise promise) {
        WritableMap map = Arguments.createMap();
        map.putBoolean("value", permissions());
        sendEvent("onPermission", map);
        sensorManager = (SensorManager) getReactApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(accListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        heartRateImpModule.startTracking();
        startBackgroundThread();
        setUpMediaCodec();
        loadCamera();
    }

    @ReactMethod
    public void stopTracking() {
        heartRateImpModule.stopTracking();
        sensorManager.unregisterListener(accListener);
        stopStreamingVideo();
        closeCamera();
        stopBackgroundThread();
    }

    @ReactMethod
    public void finishTracking() {
        heartRateImpModule.finishTracking();
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

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            try {
                backgroundThread.join();
                backgroundThread = null;
                backgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadCamera() {
        cameraManager = (CameraManager) getReactApplicationContext().getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] availableCameras = cameraManager.getCameraIdList();
            if (availableCameras[0] != null && cameraDevice == null) {
                if (ContextCompat.checkSelfPermission(getReactApplicationContext(),
                        Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    cameraManager.openCamera(availableCameras[0], new CameraDevice.StateCallback() {

                        @Override
                        public void onOpened(CameraDevice camera) {
                            cameraDevice = camera;
                            startCameraSession();
                        }

                        @Override
                        public void onDisconnected(CameraDevice camera) {
                            cameraDevice.close();
                            cameraDevice = null;
                        }

                        @Override
                        public void onError(CameraDevice camera, int error) {
                        }
                    }, backgroundHandler);
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    private void startCameraSession() {
        try {
            captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            captureBuilder.addTarget(encoderSurface);

            cameraDevice.createCaptureSession(Collections.singletonList(encoderSurface),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            captureSession = session;

                            try {
                                captureSession.setRepeatingRequest(captureBuilder.build(),
                                        null, backgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {
                        }
                    }, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setUpMediaCodec() {
        try {
            mediaCodec = MediaCodec.createEncoderByType("video/avc"); // H264 codec
        } catch (Exception e) {
            e.printStackTrace();
        }
        int width = 320;
        int height = 240;
        int colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
        int videoBitrate = 500000;
        int videoFramePerSecond = 20; // FPS
        int iframeInterval = 2; // I-Frame interval in sec

        MediaFormat format = MediaFormat.createVideoFormat("video/avc", width, height);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
        format.setInteger(MediaFormat.KEY_BIT_RATE, videoBitrate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, videoFramePerSecond);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iframeInterval);

        mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        encoderSurface = mediaCodec.createInputSurface();

        mediaCodec.setCallback(new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
            }

            @Override
            public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index,
                                                @NonNull MediaCodec.BufferInfo info) {
                ByteBuffer outPutByteBuffer = mediaCodec.getOutputBuffer(index);
                byte[] outDate = new byte[info.size];
                outPutByteBuffer.get(outDate);

                WritableMap map = Arguments.createMap();
                map.putString("data", Hex.encodeHexString(outDate));
                map.putBoolean("motion", motion);
                sendEvent("onHeartRateState", heartRateImpModule.handle(outDate));
                mediaCodec.releaseOutputBuffer(index, false);
            }

            @Override
            public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {
            }

            @Override
            public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
            }
        });
        mediaCodec.start();
    }

    private void sendEvent(String eventName,
                                 @Nullable WritableMap params) {
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    private void stopStreamingVideo() {
        if (cameraDevice != null & mediaCodec != null) {
            try {
                captureSession.stopRepeating();
                captureSession.abortCaptures();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            mediaCodec.stop();
            mediaCodec.release();
            encoderSurface.release();
            closeCamera();
        }
    }
}
