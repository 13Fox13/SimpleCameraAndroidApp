package com.simlpecamera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Environment;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;


public class CameraModule extends ReactContextBaseJavaModule {
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private Surface surface;
    private ImageReader imageReader;
    private int count;

    private final HeartRateImpModule heartRateImpModule = new HeartRateImpModule();
    private final SensorsService sensorsService = new SensorsService();
    private final CameraService cameraService = new CameraService();

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
                                return grantResults.length > 0
                                        && grantResults[0] == PackageManager.PERMISSION_GRANTED;
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
        startBackgroundThread();
        prepareImageReader();
        sensorsService.init(getReactApplicationContext());
        cameraService.init(getReactApplicationContext(), surface, backgroundHandler);
        heartRateImpModule.startTracking();
    }

    @ReactMethod
    public void stopTracking() {
        heartRateImpModule.stopTracking();
        sensorsService.destroy();
        cameraService.destroy();
        stopBackgroundThread();
    }

    @ReactMethod
    public void finishTracking() {
        heartRateImpModule.finishTracking();
    }

    private void prepareImageReader() {
        imageReader = ImageReader.newInstance(640, 480, ImageFormat.JPEG,
                10);
        imageReader.setOnImageAvailableListener(onImageAvailableListener, backgroundHandler);
        surface = imageReader.getSurface();
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.setPriority(Thread.NORM_PRIORITY);
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

    protected ImageReader.OnImageAvailableListener onImageAvailableListener = reader -> {
        Image img = reader.acquireLatestImage();
        if (img != null) {
            ByteBuffer byteBuffer = img.getPlanes()[0].getBuffer();
            byte[] outData = new byte[byteBuffer.remaining()];
            byteBuffer.get(outData);
            img.close();
            //Uncomment it if you want to save images to local storage
            // !!! it will be a huge
            //saveImage(outData);
            WritableMap map = Arguments.createMap();
            map.putString("data", Hex.encodeHexString(outData));
            map.putBoolean("motion", sensorsService.getMotion());
            sendEvent("onHeartRateState", heartRateImpModule.handle(outData));
        }
    };

    private void saveImage(byte[] data) {
        File file = new File(Environment.getExternalStorageDirectory(), "killme" + count + ".jpg");
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(file);
            output.write(data);
            count++;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != output) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendEvent(String eventName,
                           @Nullable WritableMap params) {
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }
}
