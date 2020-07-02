package com.cin.facesign.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.baidu.aip.asrwakeup3.core.recog.MyRecognizer;
import com.baidu.aip.asrwakeup3.core.recog.listener.ChainRecogListener;
import com.baidu.aip.asrwakeup3.core.recog.listener.MessageStatusRecogListener;
import com.baidu.aip.face.camera.CameraImageSource;
import com.baidu.aip.face.DetectRegionProcessor;
import com.baidu.aip.face.FaceDetectManager;
import com.baidu.speech.asr.SpeechConstant;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.BR;
import com.cin.facesign.R;
import com.cin.facesign.bean.eventbus.SignatureFinishEvent;
import com.cin.facesign.control.FaceSignSynthesizer;
import com.cin.facesign.databinding.ActivityOnlineFaceSignBinding;
import com.cin.facesign.ui.adapter.OnlineFaceSignProgressAdapter;
import com.cin.facesign.utils.OCRUtil;
import com.cin.facesign.viewmodel.OnlineFaceSignViewModel;
import com.cin.facesign.widget.dialog.ApplyAcceptDialog;
import com.cin.facesign.widget.dialog.LineUpDialog;
import com.cin.facesign.widget.dialog.TurnHumanServiceDialog;
import com.cin.facesign.widget.dialog.asr.AsrSpeechDialog;
import com.cin.facesign.widget.dialog.asr.DigitalDialogInput;
import com.cin.mylibrary.base.BaseActivity;
import com.permissionx.guolindev.PermissionX;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 在线面签
 * Created by 王新超 on 2020/6/16.
 */
public class OnlineFaceSignActivity extends BaseActivity<ActivityOnlineFaceSignBinding, OnlineFaceSignViewModel> {


    private LineUpDialog lineUpDialog;

    public static void startActivity(FragmentActivity activity) {
        PermissionX.init(activity).permissions(
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA).request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        activity.startActivity(new Intent(activity, OnlineFaceSignActivity.class));
                    } else {
                        ToastUtils.showShort("权限被拒绝，无法使用该功能！");
                    }
                }
        );

    }

    private FaceSignSynthesizer synthesizer;
    private FaceDetectManager faceDetectManager;
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
    /**
     * 开始人脸识别
     */
    private static final int START_FACE_IDENTIFY = 10001;
    /**
     * 延迟播放语音合成文字
     */
    private static final int START_SPEAK_TEXT = 10002;
    /**
     * 展示语音录入框
     */
    private static final int SHOW_ASR_SPEECH_DIALOG = 10003;
    /**
     * 面签最终完成
     */
    private static final int FACE_SIGN_FINISH = 10004;
    /**
     * 语音识别弹窗
     */
    private static final int ASR_REQUEST = 101;

    private boolean alreadyAddOCRProcessor = false;
    /**
     * 当前的进度
     */
    private int currentProgress = 0;
    /**
     * 当前进度适配器
     */
    private OnlineFaceSignProgressAdapter mOnlineFaceSignProgressAdapter;
    /**
     * 语音识别
     */
    private MyRecognizer myRecognizer;

    private boolean firstSpeechInitSuccess = true;
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
                    if (firstSpeechInitSuccess) {
                        synthesizer.speak(getSpeakText());
                    }
                    firstSpeechInitSuccess = false;
                    break;
                case START_FACE_IDENTIFY:
                    mBeginDetect = true;
                    break;
                case START_SPEAK_TEXT:
                    LogUtils.i("currentProgress=" + currentProgress);
                    mOnlineFaceSignProgressAdapter.updateProgress(currentProgress);
                    //需要跳转展示电子文档
                    if (currentProgress == 3) {
//                        WebActivity.startActivity(OnlineFaceSignActivity.this, "https://github.com/");
//                        SignatureActivity.startActivity(OnlineFaceSignActivity.this);
                        ElectronicDocumentActivity.startActivity(OnlineFaceSignActivity.this);
                    } else {
                        binding.voiceTextContent.setText(getSpeakText());
                        synthesizer.speak(getSpeakText());
                    }
                    break;
                case SHOW_ASR_SPEECH_DIALOG:
                    //提示是否是本人签名完成 展示弹窗

                    Intent intent = new Intent(OnlineFaceSignActivity.this, AsrSpeechDialog.class);
                    startActivityForResult(intent, ASR_REQUEST);
                    break;
                case FACE_SIGN_FINISH:
                    FaceSignFinishActivity.startActivity(OnlineFaceSignActivity.this);
                    finish();
                    break;
                case 100:
                    lineUpDialog.dismiss();
                    ApplyAcceptDialog applyAcceptDialog = new ApplyAcceptDialog(OnlineFaceSignActivity.this);
                    applyAcceptDialog.setOnButton1ClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });
                    applyAcceptDialog.show();
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
        EventBus.getDefault().register(this);
        mImmersionBar.statusBarDarkFont(false).init();

        binding.progressRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mOnlineFaceSignProgressAdapter = new OnlineFaceSignProgressAdapter();
        binding.progressRecyclerView.setAdapter(mOnlineFaceSignProgressAdapter);
        viewModel.initProgressRecyclerView();
        binding.voiceTextContent.setText(getSpeakText());

        synthesizer = new FaceSignSynthesizer(this, mHandler);
        initASRSpeech();

        faceDetectManager = new FaceDetectManager(this);
        CameraImageSource cameraImageSource = new CameraImageSource(this, false);
        cameraImageSource.setPreviewView(binding.previewView);
        faceDetectManager.setImageSource(cameraImageSource);
        //添加人脸裁剪器
        faceDetectManager.addPreProcessor(faceCropProcessor);
//        faceDetectManager.addPreProcessor(ocrCropProcessor);

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
                    a = status;
                }

                if (status == 0) {
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
        startSynthesizer();

        binding.titleBar.setOnTitleClickListener(() -> {
            TurnHumanServiceDialog dialog = new TurnHumanServiceDialog(OnlineFaceSignActivity.this);
            dialog.show();
            dialog.setOnButton1ClickListener(v -> {
                showToast("人工辅助");

                lineUpDialog = new LineUpDialog(OnlineFaceSignActivity.this);
                lineUpDialog.show();
                mHandler.sendEmptyMessageDelayed(100, 2000);
            });
            dialog.setOnButton2ClickListener(v -> showToast("线下面签"));
        });
    }

    /**
     * 初始化语音识别
     */
    public void initASRSpeech() {

        ChainRecogListener chainRecogListener = new ChainRecogListener();
        // DigitalDialogInput 输入 ，MessageStatusRecogListener可替换为用户自己业务逻辑的listener
        chainRecogListener.addListener(new MessageStatusRecogListener(mHandler));
        myRecognizer = new MyRecognizer(this, chainRecogListener);
        final Map<String, Object> params = new HashMap<>();
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        DigitalDialogInput asrInput = new DigitalDialogInput(myRecognizer, chainRecogListener, params);
        AsrSpeechDialog.setInput(asrInput);
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

    /**
     * 开始语音合成
     */
    private void startSynthesizer() {
        mHandler.sendEmptyMessageDelayed(START_FACE_IDENTIFY, 1000);
        synthesizer.setListener(() -> {
            if (currentProgress == 4) {
                mHandler.sendEmptyMessageDelayed(SHOW_ASR_SPEECH_DIALOG, 500);
            } else {
                currentProgress++;
                mHandler.sendEmptyMessageDelayed(START_SPEAK_TEXT, 2000);
            }
        });
    }

    @Subscribe
    public void onEvent(Object object) {
        if (object instanceof SignatureFinishEvent) {
            //最后一步签名完成
            currentProgress++;
            mHandler.sendEmptyMessageDelayed(START_SPEAK_TEXT, 2000);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ASR_REQUEST && resultCode == RESULT_OK && data != null) {
            String result = data.getStringExtra("result");
            LogUtils.i(result);
            if (result == null) {
                return;
            }
            if (result.startsWith("确认")) {
                ToastUtils.setGravity(Gravity.CENTER, 0, 0);
                ToastUtils.showCustomLong(R.layout.dialog_asr_speech_success);
                ToastUtils.setGravity(-1, -1, -1);
                mHandler.sendEmptyMessageDelayed(FACE_SIGN_FINISH, 2000);
            } else if (result.startsWith("否认")) {
                ToastUtils.showShort("否认");
            }
        }
    }

    /**
     * 获取当前需要语音合成播放的文字
     */
    private String getSpeakText() {
        List<String> list = viewModel.voiceText.get();
        if (list != null) {
            return list.get(currentProgress);
        }
        return "";
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
        if (synthesizer != null) {
            synthesizer.stop();
        }
    }

    @Override
    protected void onDestroy() {
        myRecognizer.release();
        if (synthesizer != null) {
            synthesizer.release();
        }
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        mHandler.removeCallbacksAndMessages(null);
    }
}
