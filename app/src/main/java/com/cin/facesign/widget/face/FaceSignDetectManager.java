package com.cin.facesign.widget.face;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapRegionDecoder;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.baidu.aip.FaceDetector;
import com.baidu.aip.FaceSDKManager;
import com.baidu.aip.ImageFrame;
import com.baidu.aip.face.ArgbPool;
import com.baidu.aip.face.FaceFilter;
import com.baidu.aip.face.OnFrameAvailableListener;
import com.baidu.aip.face.stat.Ast;
import com.baidu.idl.facesdk.FaceInfo;
import com.baidu.idl.facesdk.FaceSDK;
import com.baidu.idl.facesdk.FaceTracker;
import com.blankj.utilcode.util.LogUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by 王新超 on 2020/6/22.
 */
public class FaceSignDetectManager {
    private Activity mContext;
    private FaceSignManager faceSignManager;

    public FaceSignDetectManager(Activity context) {
        mContext = context;
        Ast.getInstance().init(context.getApplicationContext(), "3.3.0.0", "facedetect");
    }

    /**
     * 人脸检测事件监听器
     */
    private OnFaceDetectListener listener;
    private FaceFilter faceFilter = new FaceFilter();
    private HandlerThread processThread;
    private Handler processHandler;
    private Handler uiHandler;
    private ImageFrame lastFrame;
    private int mPreviewDegree = 90;
    private boolean isOCR = false;
    private FaceSignTextureView faceSignTextureView;
    private ArgbPool argbPool = new ArgbPool();
    private ArrayList<OnFrameAvailableListener> listeners = new ArrayList<>();

    public void setOCR(boolean OCR) {
        isOCR = OCR;
    }

    private ArrayList<FaceSignDetectRegionProcessor> preProcessors = new ArrayList<>();

    /**
     * 设置人脸检测监听器，检测后的结果会回调。
     *
     * @param listener 监听器
     */
    public void setOnFaceDetectListener(OnFaceDetectListener listener) {
        this.listener = listener;
    }

    /**
     * 增加处理回调，在人脸检测前会被回调。
     *
     * @param processor 图片帧处理回调
     */
    public void addPreProcessor(FaceSignDetectRegionProcessor processor) {
        preProcessors.add(processor);
    }

    public void removePreProcessor(FaceSignDetectRegionProcessor processor){
        preProcessors.remove(processor);
    }

    public void setFaceSignTextureView(FaceSignTextureView faceSignTextureView){
        this.faceSignTextureView = faceSignTextureView;
        faceSignManager = new FaceSignManager(mContext, faceSignTextureView);
        faceSignManager.setOnFrameListener(new FaceSignManager.OnFrameListener() {
            @Override
            public void onFrame(byte[] data, int rotation, int width, int height, boolean faceIdentify) {
                try {
                    int width1 = width;
                    int height1 = height;
                    int[] argb = argbPool.acquire(width1, height1);

                    if (argb == null || argb.length != width1 * height1) {
                        argb = new int[width1 * height1];
                    }
                    if (faceIdentify) {

                        rotation = rotation < 0 ? 360 + rotation : rotation;
                        if (data!=null) {
                            FaceDetector.yuvToARGB(data, width1, height1, argb, rotation, 0);
                        }
//                    argb = decodeYUV420SP(data, width1, height1);
                        // 旋转了90或270度。高宽需要替换
                        if (rotation % 180 == 90) {
                            int temp = width1;
                            width1 = height1;
                            height1 = temp;
                        }
                    }

                    Log.i("当前线程",Thread.currentThread().getName());
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
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });
    }

    /** 注册监听器，当有图片帧时会回调。*/
    public void addOnFrameAvailableListener(OnFrameAvailableListener listener) {
        this.listeners.add(listener);
    }

    /** 删除监听器*/
    public void removeOnFrameAvailableListener(OnFrameAvailableListener listener) {
        if (listener != null) {
            this.listeners.remove(listener);
        }
    }

    /** 获取监听器列表 */
    protected ArrayList<OnFrameAvailableListener> getListeners() {
        return listeners;
    }

    public FaceSignTextureView getFaceSignTextureView() {
        return faceSignTextureView;
    }

    /**
     * 设置人检跟踪回调。
     *
     * @param onTrackListener 人脸回调
     */
    public void setOnTrackListener(FaceFilter.OnTrackListener onTrackListener) {
        faceFilter.setOnTrackListener(onTrackListener);
    }

    /**
     * 开始人脸识别
     */
    public void startFaceIdentify() {
        addOnFrameAvailableListener(onFrameAvailableListener);
        faceSignManager.start();
        processThread = new HandlerThread("process");
        processThread.setPriority(10);
        processThread.start();
        processHandler = new Handler(processThread.getLooper());
        uiHandler = new Handler();


    }

    private Runnable processRunnable = new Runnable() {
        @Override
        public void run() {
            if (lastFrame == null) {
                return;
            }
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
            int[] argb;
            int width;
            int height;
            ArgbPool pool;
            byte[] data;
            int optWidth;
            int optHeight;
            int rotation;
            synchronized (lastFrame) {
                argb = lastFrame.getArgb();
                width = lastFrame.getWidth();
                height = lastFrame.getHeight();
                pool = lastFrame.getPool();
                data = lastFrame.getOcrFrame().getData();
                optWidth=lastFrame.getOcrFrame().getOptWidth();
                optHeight = lastFrame.getOcrFrame().getOptHeight();
                rotation = lastFrame.getOcrFrame().getRotation();
                lastFrame = null;
            }
            process(argb, width, height, pool,data,optWidth,optHeight,rotation);
        }
    };

    public void stop() {
        removeOnFrameAvailableListener(onFrameAvailableListener);
        if (processThread != null) {
            processThread.quit();
            processThread = null;
        }
        Ast.getInstance().immediatelyUpload();
    }


    private void process(int[] argb, int width, int height, ArgbPool pool,byte[] data,int optWidth,int optHeight,int rotation) {
        int value;

        ImageFrame frame = new ImageFrame();
        frame.setArgb(argb);
        frame.setWidth(width);
        frame.setHeight(height);
        frame.setPool(pool);
        ImageFrame.OCRFrame ocrFrame = new ImageFrame.OCRFrame();
        ocrFrame.setData(data);
        ocrFrame.setRotation(rotation);
        ocrFrame.setOptWidth(optWidth);
        ocrFrame.setOptHeight(optHeight);
        frame.setOcrFrame(ocrFrame);

        for (FaceSignDetectRegionProcessor processor : preProcessors) {
            if (processor.process(this, frame)) {
                break;
            }
        }

        value = FaceSDKManager.getInstance().getFaceTracker(mContext)
                .prepare_max_face_data_for_verify(frame.getArgb(), frame.getHeight(), frame.getWidth(),
                        FaceSDK.ImgType.ARGB.ordinal(), FaceTracker.ActionType.RECOGNIZE.ordinal());
        FaceInfo[] faces = FaceSDKManager.getInstance().getFaceTracker(mContext).get_TrackedFaceInfo();

        LogUtils.i("aaaaa",value+"");

        if (value == 0||isOCR) {
            faceFilter.filter(faces, frame,isOCR);
        }
        if (listener != null) {
            listener.onDetectFace(value, faces, frame);
        }
        Ast.getInstance().faceHit("facelogin",  60 * 1000, faces);

        frame.release();

    }

    private OnFrameAvailableListener onFrameAvailableListener = new OnFrameAvailableListener() {
        @Override
        public void onFrameAvailable(ImageFrame imageFrame) {
            lastFrame = imageFrame;
            processHandler.removeCallbacks(processRunnable);
            processHandler.post(processRunnable);
//            process(imageFrame.getArgb(),imageFrame.getWidth(),imageFrame.getHeight(),imageFrame.getPool(),imageFrame.getOcrFrame().getData(),
//                    imageFrame.getOcrFrame().getOptWidth(),imageFrame.getOcrFrame().getOptHeight(),imageFrame.getOcrFrame().getRotation());
        }
    };

    /**
     * 该回调用于回调，人脸检测结果。当没有人脸时，infos 为null,status为 FaceDetector.DETECT_CODE_NO_FACE_DETECTED
     */
    public interface OnFaceDetectListener {
        void onDetectFace(int status, FaceInfo[] infos, ImageFrame imageFrame);
    }

}
