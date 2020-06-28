/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.face.camera;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import com.baidu.aip.FaceDetector;
import com.baidu.aip.ImageFrame;
import com.baidu.aip.face.ArgbPool;
import com.baidu.aip.face.ImageSource;
import com.baidu.aip.face.OnFrameAvailableListener;
import com.baidu.aip.face.PreviewView;

import android.content.Context;
import android.util.Log;

/**
 * 封装了系统相机为输入源。
 */
public class CameraImageSource extends ImageSource {

    /**
     * 相机控制类
     */
    private ICameraControl cameraControl;
    private Context context;

    public ICameraControl getCameraControl() {
        return cameraControl;
    }

    private ArgbPool argbPool = new ArgbPool();

    private int cameraFaceType = ICameraControl.CAMERA_FACING_FRONT;

    public void setCameraFacing(int type) {
        this.cameraFaceType = type;
    }

    public CameraImageSource(Context context,boolean register) {
        this.context = context;
        cameraControl = new Camera1Control(getContext(),register);

        cameraControl.setCameraFacing(cameraFaceType);
        cameraControl.setOnFrameListener(new ICameraControl.OnFrameListener<byte[]>() {
            @Override
            public void onPreviewFrame(byte[] data, int rotation, int width, int height,boolean faceIdentify) {
                int width1 = width;
                int height1 = height;
                int[] argb = argbPool.acquire(width1, height1);

                if (argb == null || argb.length != width1 * height1) {
                    argb = new int[width1 * height1];
                }
                if (faceIdentify) {
                    rotation = rotation < 0 ? 360 + rotation : rotation;
                    FaceDetector.yuvToARGB(data, width1, height1, argb, rotation, 0);
                    // 旋转了90或270度。高宽需要替换
                    if (rotation % 180 == 90) {
                        int temp = width1;
                        width1 = height1;
                        height1 = temp;
                    }
                }
                Log.i("当前线程",Thread.currentThread().getName());

               //   FaceSDK.getARGBFromYUVimg(data, argb, width, height, rotation, 0);

                ImageFrame frame = new ImageFrame();
                frame.setArgb(argb);
                frame.setWidth(width1);
                frame.setHeight(height1);
                frame.setPool(argbPool);
                ImageFrame.OCRFrame ocrFrame = new ImageFrame.OCRFrame();
                ocrFrame.setData(data);
                ocrFrame.setOptHeight(height);
                ocrFrame.setOptWidth(width);
                ocrFrame.setRotation(rotation);
                frame.setOcrFrame(ocrFrame);
                ArrayList<OnFrameAvailableListener> listeners = getListeners();
                for (OnFrameAvailableListener listener : listeners) {
                    listener.onFrameAvailable(frame);
                }
            }
        });

    }

    private int[] toIntArray(byte[] buf) {
        final ByteBuffer buffer = ByteBuffer.wrap(buf)
                .order(ByteOrder.LITTLE_ENDIAN);
        final int[] ret = new int[buf.length / 4];
        buffer.asIntBuffer().put(ret);
        return ret;
    }

    @Override
    public void start() {
        super.start();
        cameraControl.start();
    }

    @Override
    public void stop() {
        super.stop();
        cameraControl.stop();
    }

    private Context getContext() {
        return context;
    }

    @Override
    public void setPreviewView(PreviewView previewView) {
        cameraControl.setPreviewView(previewView);
    }
}
