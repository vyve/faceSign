package com.cin.facesign.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.baidu.aip.face.camera.CameraImageSource;
import com.baidu.aip.face.DetectRegionProcessor;
import com.baidu.aip.face.FaceDetectManager;
import com.blankj.utilcode.util.LogUtils;
import com.chad.library.BR;
import com.cin.facesign.R;
import com.cin.facesign.control.FaceSignSynthesizer;
import com.cin.facesign.databinding.ActivityOnlineFaceSignBinding;
import com.cin.facesign.ui.adapter.OnlineFaceSignProgressAdapter;
import com.cin.facesign.utils.OCRUtil;
import com.cin.facesign.viewmodel.OnlineFaceSignViewModel;
import com.cin.mylibrary.base.BaseActivity;
import com.permissionx.guolindev.PermissionX;



/**
 * 在线面签
 * Created by 王新超 on 2020/6/16.
 */
public class OnlineFaceSignActivity extends BaseActivity<ActivityOnlineFaceSignBinding, OnlineFaceSignViewModel> {

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, OnlineFaceSignActivity.class));
    }

    private FaceSignSynthesizer synthesizer;
    private FaceDetectManager faceDetectManager;

    /**
     * 初始化语音合成服务是否成功
     */
    private boolean initSpeechSynthesis = false;
    /**
     * 人脸检测裁剪处理器
     */
    private DetectRegionProcessor faceCropProcessor = new DetectRegionProcessor();
    /**
     * ocr检测裁剪处理器
     */
    private DetectRegionProcessor ocrCropProcessor = new DetectRegionProcessor();
    /**
     * 开始人脸检测
     */
    private boolean mBeginDetect = false;
    private boolean mDetectStopped = false;
    /**
     * 人脸是否在合适的位置
     */
    private boolean mGoodDetect = false;
    private static final int START_FACE_IDENTIFY = 10001;

    private boolean alreadyAddOCRProcessor = false;

    int a = -1;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what) {
                /*
                 * 初始化语音合成服务是否成功
                 */
                case FaceSignSynthesizer.SPEECH_SYNTHESIS_INIT_SUCCESS:
                    initSpeechSynthesis = true;
                    synthesizer.speak(viewModel.identityAuthText.get());
                    break;
                case START_FACE_IDENTIFY:
                    mBeginDetect = true;
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public int initContentView(@Nullable Bundle savedInstanceState) {
        return R.layout.activity_online_face_sign;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void init() {
        mImmersionBar.statusBarDarkFont(false).init();

        binding.progressRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        OnlineFaceSignProgressAdapter onlineFaceSignProgressAdapter = new OnlineFaceSignProgressAdapter();
        binding.progressRecyclerView.setAdapter(onlineFaceSignProgressAdapter);
        viewModel.initProgressRecyclerView();

        synthesizer = new FaceSignSynthesizer(this, mHandler);

        faceDetectManager = new FaceDetectManager(this);
        CameraImageSource cameraImageSource = new CameraImageSource(this,false);
        cameraImageSource.setPreviewView(binding.previewView);
        faceDetectManager.setImageSource(cameraImageSource);
        //添加人脸裁剪器
        faceDetectManager.addPreProcessor(faceCropProcessor);
//        faceDetectManager.addPreProcessor(ocrCropProcessor);

        PermissionX.init(this).permissions(
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA).request((allGranted, grantedList, deniedList) -> {
            if (allGranted) {
                //初始化身份证识别
                viewModel.initOCRAccess();

                faceDetectManager.setOnFaceDetectListener((status, infos, frame) -> {
                    if (status == 0) {
                        if (infos != null && infos[0] != null) {
                            mGoodDetect = true;
                        }
                    }
                });

                faceDetectManager.setOnTrackListener(trackedModel -> {
                    if (!alreadyAddOCRProcessor) {
                        if (trackedModel.meetCriteria() && mGoodDetect && mBeginDetect) {
                            showToast("识别到人脸");
                            faceDetectManager.removePreProcessor(faceCropProcessor);
                            faceDetectManager.addPreProcessor(ocrCropProcessor);
                            faceDetectManager.setOCR(true);
                            alreadyAddOCRProcessor = true;
                        }
                    } else {
                        int status = OCRUtil.detect(trackedModel.getFrame().getOcrFrame(),
                                binding.previewView.getMaskView(),
                                binding.previewView);
                        if (a != status) {
                            LogUtils.i("aaaaaa,," + status);
                            a = status;
                        }

                        if (status ==0){
                            showToast("识别到身份证");
                        }
                    }
                    //识别到人脸

                });

                binding.previewView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        startFaceIdentify();
                        binding.previewView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
                mHandler.sendEmptyMessageDelayed(START_FACE_IDENTIFY, 1000);
            } else {
                showToast("无法获取权限");
                finish();
            }
        });
    }


    /**
     * 开始人脸识别
     */
    private void startFaceIdentify() {
        //设置人脸识别区域
        RectF faceIdentifyRectF = binding.previewView.getFaceIdentifyRectF();
        RectF ocrIdentifyRect = binding.previewView.getOCRIdentifyRect();
        faceCropProcessor.setDetectedRect(faceIdentifyRectF);
        ocrCropProcessor.setDetectedRect(ocrIdentifyRect);
        faceDetectManager.startFaceIdentify();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mDetectStopped) {
            faceDetectManager.startFaceIdentify();
            mDetectStopped = false;
        }

    }


    @Override
    protected void onStop() {
        super.onStop();
        mBeginDetect = false;
        mDetectStopped = true;
        faceDetectManager.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (synthesizer != null) {
            synthesizer.release();
        }
        mHandler.removeCallbacksAndMessages(null);
    }
}
