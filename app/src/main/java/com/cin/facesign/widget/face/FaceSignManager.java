package com.cin.facesign.widget.face;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;

import com.baidu.ocr.sdk.utils.LogUtil;
import com.blankj.utilcode.util.LogUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by 王新超 on 2020/6/22.
 */
public class FaceSignManager {
    private static final String TAG = "LogFaceSignManager";
    private Activity activity;
    /**
     * 相机是否已经打开
     */
    private boolean isOpened;
    private TextureView mTextureView;
    private FaceSignTextureView mFaceSignTextureView;
    private MediaRecorder mMediaRecorder;
    private ImageReader imageReader;
    private OnFrameListener listener;
    private Handler mHandler;
    private Size mPreviewSize;
    /**
     * 识别类型
     * 0：人脸识别
     * 1：身份证OCR识别
     */
    private int identifyType = 0;

    public FaceSignManager(Activity activity, FaceSignTextureView faceSignTextureView) {
        this.activity = activity;
        this.mTextureView = faceSignTextureView.getTextureView();
        mFaceSignTextureView = faceSignTextureView;
    }

    private void initLooper() {
        HandlerThread handlerThread = new HandlerThread("faceSign");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
    }

    /**
     * 每一帧图片的回调
     */
    public interface OnFrameListener {
        /**
         * @param data         图片数据
         * @param rotation     旋转角度
         * @param width        图片宽度
         * @param height       图片高度
         * @param faceIdentify true 人脸识别 false OCR识别
         */
        void onFrame(byte[] data, int rotation, int width, int height, boolean faceIdentify);
    }

    public void setOnFrameListener(OnFrameListener listener) {
        this.listener = listener;
    }

    public void start() {
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                openCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    public int getIdentifyType() {
        return identifyType;
    }

    @SuppressLint("MissingPermission")
    private void openCamera() {
        if (isOpened) {
            return;
        }
        isOpened = true;
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (manager == null) {
                return;
            }
            //这个可能会有很多个，但是通常都是两个，第一个是后置，第二个是前置；
            String cameraId = manager.getCameraIdList()[1];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            Size[] mSupportPixels = map.getOutputSizes(SurfaceTexture.class);
            mPreviewSize = getOptimalSize(mSupportPixels);
            mFaceSignTextureView.setPreviewSize(mPreviewSize.getHeight(), mPreviewSize.getWidth());
            manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    LogUtils.i(TAG, "相机已经打开");
                    createCameraPreview(camera);
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    LogUtils.i(TAG, "相机失去连接");
                    camera.close();
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    LogUtils.i(TAG, "相机错误" + error);
                    camera.close();
                }
            }, mHandler);//这个指定其后台运行，如果直接UI线程也可以，直接填null；
            LogUtils.i(TAG, "open Camera " + cameraId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createCameraPreview(final CameraDevice cameraDevice) {
        try {
            if (null == cameraDevice) {
                LogUtils.i(TAG, "updatePreview error, return");
                return;
            }
            setUpImageReader();
            setUpMediaRecorder();
            final CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface textureSurface = new Surface(texture);

            Surface recorderSurface = mMediaRecorder.getSurface();
            Surface imageSurface = imageReader.getSurface();
            captureRequestBuilder.addTarget(textureSurface);
            captureRequestBuilder.addTarget(recorderSurface);
            captureRequestBuilder.addTarget(imageSurface);
            List<Surface> surfaceList = Arrays.asList(textureSurface, recorderSurface, imageSurface);
            cameraDevice.createCaptureSession(surfaceList, new CameraCaptureSession.StateCallback() {//配置要接受图像的surface
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                    try {
                        //成功配置后，便开始进行相机图像的监听
                        initLooper();
                        cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMediaRecorder.start();
                        }
                    });

                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    LogUtils.i(TAG, "Configuration change");
                }
            }, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化视频录制MediaRecorder
     */
    private void setUpMediaRecorder() {

        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        profile.videoFrameWidth = mPreviewSize.getWidth();
        profile.videoFrameHeight = mPreviewSize.getHeight();

        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);// 视频输出格式
        mMediaRecorder.setVideoEncodingBitRate(10000000);
//        mMediaRecorder.setVideoFrameRate(1);

        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setOrientationHint(270);// 前置摄像头输出旋转270度，保持竖屏录制
        mMediaRecorder.setVideoSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        mMediaRecorder.setOutputFile(getVideoFilePath());
        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        mMediaRecorder.stop();
        mMediaRecorder.reset();
        mMediaRecorder.release();
        imageReader.close();
        mMediaRecorder = null;
    }

    /**
     * 读取到每一帧的 byte[]
     */
    private void setUpImageReader() {
//        imageReader = ImageReader.newInstance(mFaceSignTextureView.getWidth(), mFaceSignTextureView.getHeight(), ImageFormat.YUV_420_888, 2);
        imageReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(), ImageFormat.YUV_420_888, 2);
        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image image = reader.acquireLatestImage();
                if (image != null) {
                    int width = image.getWidth();
                    int height = image.getHeight();
                    Image.Plane[] planes = image.getPlanes();
                    ByteBuffer buffer = planes[0].getBuffer();
                    byte[] data = new byte[buffer.remaining()];
                    buffer.get(data);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.onFrame(data, 270, image.getWidth(), image.getHeight(), getIdentifyType() == 0);
                            }
                        }
                    });

                    image.close();
                }
            }
        }, mHandler);
    }

    public static byte[] yuvImageToByteArray(Image image) {

        assert (image.getFormat() == ImageFormat.YUV_420_888);

        int width = image.getWidth();
        int height = image.getHeight();

        Image.Plane[] planes = image.getPlanes();
        byte[] result = new byte[width * height * 3 / 2];

        int stride = planes[0].getRowStride();
        assert (1 == planes[0].getPixelStride());
        if (stride == width) {
            planes[0].getBuffer().get(result, 0, width * height);
        } else {
            for (int row = 0; row < height; row++) {
                planes[0].getBuffer().position(row * stride);
                planes[0].getBuffer().get(result, row * width, width);
            }
        }

        stride = planes[1].getRowStride();
        assert (stride == planes[2].getRowStride());
        int pixelStride = planes[1].getPixelStride();
        assert (pixelStride == planes[2].getPixelStride());
        byte[] rowBytesCb = new byte[stride];
        byte[] rowBytesCr = new byte[stride];

        for (int row = 0; row < height / 2; row++) {
            int rowOffset = width * height + width / 2 * row;
            planes[1].getBuffer().position(row * stride);
            planes[1].getBuffer().get(rowBytesCb);
            planes[2].getBuffer().position(row * stride);
            planes[2].getBuffer().get(rowBytesCr);

            for (int col = 0; col < width / 2; col++) {
                result[rowOffset + col * 2] = rowBytesCr[col * pixelStride];
                result[rowOffset + col * 2 + 1] = rowBytesCb[col * pixelStride];
            }
        }
        return result;
    }

    public static int[] decodeYUV420SP(byte[] yuv420sp, int width, int height) {
        final int frameSize = width * height;
        int rgb[] = new int[frameSize];
        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }
                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);
                if (r < 0)
                    r = 0;
                else if (r > 262143)
                    r = 262143;
                if (g < 0)
                    g = 0;
                else if (g > 262143)
                    g = 262143;
                if (b < 0)
                    b = 0;
                else if (b > 262143)
                    b = 262143;
                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000)
                        | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
        return rgb;
    }
    /**
     * 获取合适的分辨率
     */
    private Size getOptimalSize(Size[] sizes) {
        int width = mFaceSignTextureView.getWidth();
        int height = mFaceSignTextureView.getHeight();

        Size pictureSize = sizes[0];

        List<Size> candidates = new ArrayList<>();

        for (Size size : sizes) {
            if (size.getWidth() >= width && size.getHeight() >= height && size.getWidth() * height == size.getHeight() * width) {
                // 比例相同
                candidates.add(size);
            } else if (size.getHeight() >= width && size.getWidth() >= height && size.getWidth() * width == size.getHeight() * height) {
                // 反比例
                candidates.add(size);
            }
        }
        if (!candidates.isEmpty()) {
            return Collections.min(candidates, sizeComparator);
        }

        for (Size size : sizes) {
            if (size.getWidth() > width && size.getHeight() > height) {
                return size;
            }
        }

        return pictureSize;
    }

    private Comparator<Size> sizeComparator = (lhs, rhs) ->
            Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());

    /**
     * 视频文件存储路径
     */
    private String getVideoFilePath() {
        String filePath = activity.getFilesDir().getAbsolutePath() + "/recordVideo.mp4";

        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        return filePath;
    }
}
