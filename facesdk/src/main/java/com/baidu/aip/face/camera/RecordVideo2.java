package com.baidu.aip.face.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;

import com.baidu.aip.face.PreviewView;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Created by 王新超 on 2020/6/22.
 */
public class RecordVideo2 {

    private static final String TAG = "LogRecordVideo";
    private MediaRecorder mMediaRecorder;
    private String mNextVideoAbsolutePath;
    private Context context;
    private Camera camera;
    private Camera.Size optSize;
    private AutoFitTextureView textureView;
    private List<Camera.Size> supportedVideoSizes;
    private Camera.PreviewCallback previewCallback;

    public RecordVideo2(Context context, AutoFitTextureView textureView) {
        this.context = context;
        this.textureView = textureView;

        textureView.setAspectRatio(textureView.getWidth(), textureView.getHeight());
    }

    public void setCamera(Camera camera, PreviewView previewView,Camera.Size optSize,Camera.PreviewCallback previewCallback) {
        this.camera = camera;
        this.optSize = optSize;
        this.previewCallback = previewCallback;

        supportedVideoSizes = camera.getParameters().getSupportedVideoSizes();
//        previewView.setAspectRatio(optSize.height,optSize.width);
    }


    public void startRecord() {
        camera.startPreview();
//        new MediaPrepareTask().execute(null, null, null);
    }


    public void pause() {
        releaseMediaRecorder();
        releaseCamera();

    }

    @SuppressLint("MissingPermission")
    private boolean initMediaRecorder() {

//        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
//        profile.videoFrameWidth = optSize.width;
//        profile.videoFrameHeight = optSize.height;

        mMediaRecorder = new MediaRecorder();
        camera.unlock();
        mMediaRecorder.setCamera(camera);

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setOrientationHint(270);// 前置摄像头输出旋转270度，保持竖屏录制
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);// 视频输出格式
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);// 音频格式
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);// 视频录制格式

        int index = bestVideoSize(supportedVideoSizes,optSize.width);

//        mMediaRecorder.setVideoSize(supportedVideoSizes.get(index).width,supportedVideoSizes.get(index).height);
//        mMediaRecorder.setProfile(profile);
        if (mNextVideoAbsolutePath == null || mNextVideoAbsolutePath.isEmpty()) {
            mNextVideoAbsolutePath = getVideoFilePath(context);
        }

        mMediaRecorder.setOutputFile(mNextVideoAbsolutePath);

        try {
            mMediaRecorder.prepare();

        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }

        return true;
    }

    //查找出最接近的视频录制分辨率
    public int bestVideoSize(List<Camera.Size> videoSizeList,int _w){
        //降序排列
        Collections.sort(videoSizeList, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                if (lhs.width > rhs.width) {
                    return -1;
                } else if (lhs.width == rhs.width) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        for(int i=0;i<videoSizeList.size();i++){
            if(videoSizeList.get(i).width<_w){
                return i;
            }
        }
        return 0;
    }


    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            // Lock camera for later use i.e taking it back from MediaRecorder.
            // MediaRecorder doesn't need it anymore and we will release it if the activity pauses.
//            camera.lock();
        }
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    /**
     * Asynchronous task for preparing the {@link android.media.MediaRecorder} since it's a long blocking
     * operation.
     */
    @SuppressLint("StaticFieldLeak")
    class MediaPrepareTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            if (initMediaRecorder()) {
                mMediaRecorder.start();

//                isRecording = true;

            } else {
                releaseMediaRecorder();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {

        }
    }


    private String getVideoFilePath(Context context) {
        String filePath = context.getFilesDir().getAbsolutePath() + "/recordVideo.mp4";

        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        return filePath;
    }
}
