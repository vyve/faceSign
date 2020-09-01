/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.face.camera;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.baidu.aip.face.PreviewView;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * 5.0以下相机API的封装。
 */
@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class Camera1Control implements ICameraControl {

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int MAX_PREVIEW_SIZE = 2048;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }


    private int displayOrientation = 0;
    private int cameraId = 0;
    private int flashMode;
    private AtomicBoolean takingPicture = new AtomicBoolean(false);

    private Context context;
    private Camera camera;

    private Camera.Parameters parameters;
    private PermissionCallback permissionCallback;
    private Rect previewFrame = new Rect();

    private int preferredWidth = 1280;
    private int preferredHeight = 720;

    @ICameraControl.CameraFacing
    private int cameraFacing = CAMERA_FACING_FRONT;

    private boolean usbCamera = false;
    private Camera.Size optSize;
    private RecordVideo2 mRecordVideo;
    /**
     * true  注册 （仅仅是面部识别）
     * false 面签过程
     */
    private boolean register;

    @Override
    public void setDisplayOrientation(@CameraView.Orientation int displayOrientation) {
        this.displayOrientation = displayOrientation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshPermission() {
        startPreview(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFlashMode(@FlashMode int flashMode) {
        if (this.flashMode == flashMode) {
            return;
        }
        this.flashMode = flashMode;
        updateFlashMode(flashMode);
    }

    @Override
    public int getFlashMode() {
        return flashMode;
    }

    @Override
    public void setCameraFacing(@CameraFacing int cameraFacing) {
        this.cameraFacing = cameraFacing;
    }

    @Override
    public void setUsbCamera(boolean usbCamera) {
        this.usbCamera = usbCamera;
    }

    @Override
    public void start() {
        startCamera();
    }

    private SurfaceTexture surfaceTexture;

    private void startCamera() {
//        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
//                != PackageManager.PERMISSION_GRANTED) {
//            if (permissionCallback != null) {
//                permissionCallback.onRequestPermission();
//            }
//            return;
//        }

        if (camera == null) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == cameraFacing) {
                    cameraId = i;
                }
            }
            camera = Camera.open(cameraId);
        }
        if (parameters == null) {
            parameters = camera.getParameters();
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        surfaceTexture = new SurfaceTexture(11);
        parameters.setRotation(90); // TODO
        int rotation = ORIENTATIONS.get(displayOrientation);


        camera.setDisplayOrientation(rotation);

        try {
            camera.setPreviewCallback(previewCallback);
            camera.setPreviewTexture(surfaceTexture);
            if (textureView != null) {
                surfaceTexture.detachFromGLContext();
                textureView.setSurfaceTexture(surfaceTexture);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        opPreviewSize(previewView.getWidth(), previewView.getHeight());
    }

    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(final byte[] data, Camera camera) {
            final Camera.Size size = camera.getParameters().getPreviewSize();

            int rotation = getSurfaceOrientation();
            if (cameraFacing == ICameraControl.CAMERA_FACING_FRONT) {
                // android自带的摄像头和接usb摄像头，图片有180旋转，用usb摄像头注释下面的代码
                if (!usbCamera) {
                    if (rotation == 90 || rotation == 270) {
                        rotation = (rotation + 180) % 360;
                    }
                }
            }

//            if (rotation % 180 == 90) {
//                previewView.setPreviewSize(size.height, size.width);
//            } else {
//                previewView.setPreviewSize(size.width, size.height);
//            }

            // 在某些机型和某项项目中，某些帧的data的数据不符合nv21的格式，需要过滤，否则后续处理会导致crash
//            if (data.length != parameters.getPreviewSize().width * parameters.getPreviewSize().height * 1.5) {
//                return;
//            }

            final int finalRotation = rotation;
            CameraThreadPool.execute(new Runnable() {
                @Override
                public void run() {
//                    onFrameListener.onPreviewFrame(data, finalRotation, size.width, size.height, optSize.width, optSize.height, previewView.getIdentifyType() == 0);
                    onFrameListener.onPreviewFrame(data, finalRotation, size.width, size.height, size.width, size.height, previewView.getIdentifyType() == 0);

                }
            });
            //人脸识别
//            if (previewView.getIdentifyType() == 1) {
//                CameraThreadPool.execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        onRequestDetect(data);
//                    }
//                });
//            }

        }
    };

    private OnIdentityOcrListener ocrIdentityListener;

    public interface OnIdentityOcrListener {
        void onDetect(byte[] jpeg, int orientation);
    }

    /**
     * ocr识别成功监听
     */
    public void setOnOcrPreviewFrameListener(OnIdentityOcrListener ocrIdentityListener) {
        this.ocrIdentityListener = ocrIdentityListener;
    }

    private void onRequestDetect(byte[] data) {
        // 相机已经关闭
        if (camera == null || data == null || optSize == null) {
            return;
        }

        YuvImage img = new YuvImage(data, ImageFormat.NV21, optSize.width, optSize.height, null);
        ByteArrayOutputStream os = null;
        try {
            os = new ByteArrayOutputStream(data.length);
            img.compressToJpeg(new Rect(0, 0, optSize.width, optSize.height), 80, os);
            byte[] jpeg = os.toByteArray();
            if (ocrIdentityListener!=null) {
                ocrIdentityListener.onDetect(jpeg, getSurfaceOrientation());
            }
        } catch (OutOfMemoryError e) {
            // 内存溢出则取消当次操作
        } finally {
            try {
                os.close();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

    }


    private AutoFitTextureView textureView;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void setTextureView(AutoFitTextureView textureView) {
        this.textureView = textureView;
        if (surfaceTexture != null) {
            surfaceTexture.detachFromGLContext();
            textureView.setSurfaceTexture(surfaceTexture);
        }
    }

    @Override
    public void stop() {
        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
        if (mRecordVideo != null) {
            mRecordVideo.pause();
        }
    }

    @Override
    public void pause() {
        if (camera != null) {
            camera.stopPreview();
        }
        if (mRecordVideo != null) {
            mRecordVideo.pause();
        }
        setFlashMode(FLASH_MODE_OFF);
    }

    @Override
    public void resume() {
        takingPicture.set(false);
        if (camera == null) {
            startCamera();
        }
    }

    private OnFrameListener onFrameListener;

    @Override
    public void setOnFrameListener(OnFrameListener listener) {
        this.onFrameListener = listener;
    }

    @Override
    public void setPreferredPreviewSize(int width, int height) {
        this.preferredWidth = Math.max(width, height);
        this.preferredHeight = Math.min(width, height);
    }

    @Override
    public View getDisplayView() {
        return null;
    }


    private PreviewView previewView;

    @Override
    public void setPreviewView(PreviewView previewView) {
        this.previewView = previewView;
        setTextureView(previewView.getTextureView());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mRecordVideo = new RecordVideo2(context, textureView);
        }
    }

    @Override
    public PreviewView getPreviewView() {
        return previewView;
    }

    @Override
    public void takePicture(final OnTakePictureCallback onTakePictureCallback) {
        if (takingPicture.get()) {
            return;
        }

        switch (displayOrientation) {
            case CameraView.ORIENTATION_PORTRAIT:
                parameters.setRotation(90);
                break;
            case CameraView.ORIENTATION_HORIZONTAL:
                parameters.setRotation(0);
                break;
            case CameraView.ORIENTATION_INVERT:
                parameters.setRotation(180);
                break;
            default:
                parameters.setRotation(90);
                break;
        }
        Camera.Size picSize =
                getOptimalSize(camera.getParameters().getSupportedPictureSizes());
        parameters.setPictureSize(picSize.width, picSize.height);
        camera.setParameters(parameters);
        takingPicture.set(true);
        camera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                camera.cancelAutoFocus();
                try {
                    camera.takePicture(null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {
                            if (register) {
                                camera.startPreview();
                            } else {
                                mRecordVideo.startRecord();
                            }
                            takingPicture.set(false);
                            if (onTakePictureCallback != null) {
                                onTakePictureCallback.onPictureTaken(data);
                            }
                        }
                    });
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    if (register) {
                        camera.startPreview();
                    } else {
                        mRecordVideo.startRecord();
                    }
                    takingPicture.set(false);
                }
            }
        });
    }

    @Override
    public void setPermissionCallback(PermissionCallback callback) {
        this.permissionCallback = callback;
    }

    public Camera1Control(Context context, boolean register) {
        this.context = context;
        this.register = register;
    }

    // 开启预览
    private void startPreview(boolean checkPermission) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (checkPermission && permissionCallback != null) {
                permissionCallback.onRequestPermission();
            }
            return;
        }
        if (register) {
            camera.startPreview();
        } else {
            mRecordVideo.startRecord();
        }
    }

    private void opPreviewSize(int width, @SuppressWarnings("unused") int height) {
        if (parameters != null && camera != null && width > 0) {
            optSize = getOptimalSize(camera.getParameters().getSupportedPreviewSizes());

            parameters.setPreviewSize(optSize.width, optSize.height);

//            camera.setDisplayOrientation(getSurfaceOrientation());
            //            camera.stopPreview();
            try {
                camera.setParameters(parameters);
            } catch (RuntimeException e) {
                e.printStackTrace();

            }
            if (register) {
                camera.startPreview();
            } else {
                mRecordVideo.setCamera(camera, previewView, optSize, previewCallback);
                mRecordVideo.startRecord();
            }

        }
    }

    private Camera.Size getOptimalSize(List<Camera.Size> sizes) {
        int width = textureView.getWidth();
        int height = textureView.getHeight();
//        int width = previewView.getWidth();
//        int height = previewView.getHeight();

        Camera.Size pictureSize = sizes.get(0);

        List<Camera.Size> candidates = new ArrayList<>();

        for (Camera.Size size : sizes) {
            if (size.width >= width && size.height >= height && size.width * height == size.height * width) {
                // 比例相同
                candidates.add(size);
            } else if (size.height >= width && size.width >= height && size.width * width == size.height * height) {
                // 反比例
                candidates.add(size);
            }
        }
        if (!candidates.isEmpty()) {
            return Collections.min(candidates, sizeComparator);
        }

        for (Camera.Size size : sizes) {
            if (size.width > width && size.height > height) {
                return size;
            }
        }

        return pictureSize;
    }

//    private Camera.Size getOptimalSize(List<Camera.Size> sizes) {
//        int w = textureView.getWidth();
//        int h = textureView.getHeight();
//        final double ASPECT_TOLERANCE = 0.1;
//        double targetRatio = (double) w / h;
//        if (sizes == null) return null;
//
//        Camera.Size optimalSize = null;
//        double minDiff = Double.MAX_VALUE;
//
//        int targetHeight = h;
//
//        // Try to find an size match aspect ratio and size
//        for (Camera.Size size : sizes) {
//            double ratio = (double) size.width / size.height;
//            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
//            if (Math.abs(size.height - targetHeight) < minDiff) {
//                optimalSize = size;
//                minDiff = Math.abs(size.height - targetHeight);
//            }
//        }
//
//        // Cannot find the one match the aspect ratio, ignore the requirement
//        if (optimalSize == null) {
//            minDiff = Double.MAX_VALUE;
//            for (Camera.Size size : sizes) {
//                if (Math.abs(size.height - targetHeight) < minDiff) {
//                    optimalSize = size;
//                    minDiff = Math.abs(size.height - targetHeight);
//                }
//            }
//        }
//        return optimalSize;
//    }


    private Comparator<Camera.Size> sizeComparator = new Comparator<Camera.Size>() {
        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            return Long.signum((long) lhs.width * lhs.height - (long) rhs.width * rhs.height);
        }
    };

    private void updateFlashMode(int flashMode) {
        switch (flashMode) {
            case FLASH_MODE_TORCH:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                break;
            case FLASH_MODE_OFF:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                break;
            case ICameraControl.FLASH_MODE_AUTO:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                break;
            default:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                break;
        }
        camera.setParameters(parameters);
    }

    protected int getSurfaceOrientation() {
        @CameraView.Orientation
        int orientation = displayOrientation;
        switch (orientation) {
            case CameraView.ORIENTATION_PORTRAIT:
                return 90;
            case CameraView.ORIENTATION_HORIZONTAL:
                return 0;
            case CameraView.ORIENTATION_INVERT:
                return 180;
            default:
                return 90;
        }
    }

    @Override
    public Rect getPreviewFrame() {
        return previewFrame;
    }

}
