package com.cin.facesign.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.baidu.aip.asrwakeup3.core.recog.MyRecognizer;
import com.baidu.aip.asrwakeup3.core.recog.listener.ChainRecogListener;
import com.baidu.aip.asrwakeup3.core.recog.listener.MessageStatusRecogListener;
import com.baidu.speech.asr.SpeechConstant;
import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.BR;
import com.cin.facesign.Constant;
import com.cin.facesign.R;
import com.cin.facesign.bean.eventbus.CameraOpenSuccessEvent;
import com.cin.facesign.bean.eventbus.ScreenRecordPassEvent;
import com.cin.facesign.bean.eventbus.ScreenRecordRefusedEvent;
import com.cin.facesign.bean.eventbus.SignatureFinishEvent;
import com.cin.facesign.bean.eventbus.VideoRecordEvent;
import com.cin.facesign.control.FaceSignSynthesizer;
import com.cin.facesign.databinding.ActivityOnlineFaceSign2Binding;
import com.cin.facesign.ui.adapter.OnlineFaceSignProgressAdapter;
import com.cin.facesign.viewmodel.OnlineFaceSignViewModel;
import com.cin.facesign.widget.ScreenRecordHelper;
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
 * Created by 王新超 on 2020/8/17.
 */
public class OnlineFaceSign2Activity extends BaseActivity<ActivityOnlineFaceSign2Binding, OnlineFaceSignViewModel> {
    private static final String TAG = "视频面签";

    public static void startActivity(FragmentActivity activity, int insuranceId) {
        PermissionX.init(activity).permissions(
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA).request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (!Settings.System.canWrite(activity)) {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                activity.startActivity(intent);
                            } else {

                                Intent intent = new Intent(activity, OnlineFaceSign2Activity.class);
                                intent.putExtra("insuranceId", insuranceId);
                                activity.startActivity(intent);
                            }
                        } else {
                            Intent intent = new Intent(activity, OnlineFaceSign2Activity.class);
                            intent.putExtra("insuranceId", insuranceId);
                            activity.startActivity(intent);
                        }
                    } else {
                        ToastUtils.showShort("权限被拒绝，无法使用该功能！");
                    }
                }
        );

    }

    /**
     * 保险id
     */
    private int insuranceId;
    /**
     * 当前进度适配器
     */
    private OnlineFaceSignProgressAdapter mOnlineFaceSignProgressAdapter;
    /**
     * 当前的进度
     * 0 身份验证
     * 1 产品及条款介绍
     * 2 信息咨询
     * 3 电子文档展示
     * 4 客户签名
     */
    private int currentProgress = 0;
    /**
     * 语音识别
     */
    private MyRecognizer myRecognizer;
    /**
     * 语音合成
     */
    private FaceSignSynthesizer synthesizer;
    private boolean firstSpeechInitSuccess = true;
    private boolean isAlive;
    /**
     * 重新获取视频帧
     */
    private static final int GET_VIDEO_FRAME = 10001;
    /**
     * 延迟播放语音合成文字
     */
    private static final int START_SPEAK_TEXT = 10002;
    /**
     * 面签最终完成
     */
    private static final int FACE_SIGN_FINISH = 10004;
    /**
     * 初始化语音合成
     */
    private static final int INIT_SYNTHESIZER = 10005;
    /**
     * 语音识别弹窗
     */
    private static final int ASR_REQUEST = 101;

    /**
     * 是否需要保存屏幕录制视频
     */
    private boolean needSaveScreenRecord = false;
    /**
     * 排队弹窗
     */
    private LineUpDialog lineUpDialog;
    /**
     * 屏幕录制帮助类
     */
    private ScreenRecordHelper screenRecordHelper;
    /**
     * 视频录制阶段
     * 1：第一阶段
     * 2：第二阶段
     */
    private int videoRecordProgress = 1;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what) {
                case GET_VIDEO_FRAME:
                    EventBus.getDefault().post(new VideoRecordEvent(true));
                    break;
                case INIT_SYNTHESIZER:
                    synthesizer = new FaceSignSynthesizer(OnlineFaceSign2Activity.this, mHandler);

                    synthesizerListener();
                    initASRSpeech();
                    break;
                /*
                 * 初始化语音合成服务是否成功
                 */
                case FaceSignSynthesizer.SPEECH_SYNTHESIS_INIT_SUCCESS:
                    if (firstSpeechInitSuccess) {
//                        synthesizer.speak(viewModel.getSpeakText(0));
                        //开始语音播放第一步
                        currentProgress = 0;
                        synthesizer.speak(getSpeakText());
                        //语音初始化完成 开始取视频帧交给服务端识别
                        EventBus.getDefault().post(new VideoRecordEvent(true));
                    }
                    firstSpeechInitSuccess = false;
                    break;
                case START_SPEAK_TEXT:
                    mOnlineFaceSignProgressAdapter.updateProgress(currentProgress);
                    binding.voiceTitle.setText(mOnlineFaceSignProgressAdapter.getProgressTitle(currentProgress));
                    //条款介绍
                    if (currentProgress == 1) {
                        binding.voiceTextContent.setText(getSpeakText());
                        synthesizer.speak(getSpeakText());
                    }
                    //信息咨询 播放完成后展示语音识别弹窗
                    if (currentProgress == 2) {
                        binding.voiceTextContent.setText(getSpeakText());
                        synthesizer.speak(getSpeakText());
                        if (isAlive) {
                            AsrSpeechDialog.startActivityForResult(OnlineFaceSign2Activity.this, ASR_REQUEST, "是","是或否");
                        }
                    }
                    if (currentProgress==3){
                        binding.voiceTextContent.setText(getSpeakText());
                        synthesizer.speak(getSpeakText());
                    }
                    if (currentProgress == 4) {
                        binding.voiceTextContent.setText(getSpeakText());
                        synthesizer.speak(getSpeakText());
                        if (isAlive) {
                            AsrSpeechDialog.startActivityForResult(OnlineFaceSign2Activity.this, ASR_REQUEST, "是","是或否");
                        }
                    }
                    break;
                case FACE_SIGN_FINISH:
                    needSaveScreenRecord = true;
                    //停止第三段视频录制
                    Log.i(TAG, "停止第三段视频录制");
                    binding.previewView.stopVideoRecord();
                    FaceSignFinishActivity.startActivity(OnlineFaceSign2Activity.this, insuranceId);
                    finish();
                    break;
                case 100:
                    lineUpDialog.dismiss();
                    ApplyAcceptDialog applyAcceptDialog = new ApplyAcceptDialog(OnlineFaceSign2Activity.this);
                    applyAcceptDialog.setOnButton1ClickListener(v -> finish());
                    applyAcceptDialog.show();
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    public int initContentView(@Nullable Bundle savedInstanceState) {
        return R.layout.activity_online_face_sign2;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void init() {
        isAlive = true;
        EventBus.getDefault().register(this);
        mImmersionBar.statusBarDarkFont(false).init();

        insuranceId = getIntent().getIntExtra("insuranceId", 0);
        viewModel.insuranceId.set(insuranceId);

        binding.progressRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mOnlineFaceSignProgressAdapter = new OnlineFaceSignProgressAdapter();
        binding.progressRecyclerView.setAdapter(mOnlineFaceSignProgressAdapter);
        viewModel.initProgressRecyclerView();
        binding.voiceTextContent.setText(getSpeakText());
        binding.voiceTextContent.setMovementMethod(ScrollingMovementMethod.getInstance());

        //初始化语音合成服务
        mHandler.sendEmptyMessage(INIT_SYNTHESIZER);

        initListener();

        screenRecordHelper = new ScreenRecordHelper(OnlineFaceSign2Activity.this,
                null, Constant.FILE_SAVE_PATH, "screenRecord");

        //开始视频预览
        binding.previewView.start();
    }

    private void initListener() {
        binding.titleBar.setOnTitleClickListener(() -> {
            TurnHumanServiceDialog dialog = new TurnHumanServiceDialog(OnlineFaceSign2Activity.this);
            dialog.show();
            dialog.setOnButton1ClickListener(v -> {
                showToast("人工辅助");

                lineUpDialog = new LineUpDialog(OnlineFaceSign2Activity.this);
                lineUpDialog.show();
                mHandler.sendEmptyMessageDelayed(100, 2000);
            });
            dialog.setOnButton2ClickListener(v -> showToast("线下面签"));
        });
        binding.previewView.setOnFrameListener(framePath -> {


//            viewModel.uploadVideoRecordFrame(OnlineFaceSign2Activity.this, framePath));
        });
        //人脸身份证验证结果
        viewModel.faceIdentifyAccess.observe(this, aBoolean -> {
            if (aBoolean) {
                //识别成功弹窗
                ToastUtils.setGravity(Gravity.CENTER, 0, 0);
                ToastUtils.showCustomLong(R.layout.dialog_certification_success);
                ToastUtils.setGravity(-1, -1, -1);
                //只有人脸识别成功以后才能开始进行第二步产品及条款介绍
                currentProgress = 1;
                mHandler.sendEmptyMessageDelayed(START_SPEAK_TEXT, 2000);
            } else {
                //如果失败，延迟一秒重新获取视频帧并上传验证
                mHandler.sendEmptyMessageDelayed(GET_VIDEO_FRAME, 1000);
            }
        });

    }

    /**
     * 开始语音合成
     */
    private void synthesizerListener() {
        //语音播放结束监听
        synthesizer.setListener(() -> {
            if (currentProgress != 0) {
                if (currentProgress == 1) {
                    //条款介绍播放完毕 记着进行第三步 信息咨询
                    currentProgress = 2;
                    mHandler.sendEmptyMessageDelayed(START_SPEAK_TEXT, 2000);
                }
                if (currentProgress==3){
                    screenRecordHelper.startRecord();
                }
            }else {
                /*
                 * 测试-跳过人脸身份证识别
                 */
                /***************************************************************************************************/
                //识别成功弹窗
                ToastUtils.setGravity(Gravity.CENTER, 0, 0);
                ToastUtils.showCustomLong(R.layout.dialog_certification_success);
                ToastUtils.setGravity(-1, -1, -1);
                //只有人脸识别成功以后才能开始进行第二步产品及条款介绍
                currentProgress = 1;
                mHandler.sendEmptyMessageDelayed(START_SPEAK_TEXT, 2000);
                /***************************************************************************************************/
            }
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

    @Subscribe
    public void onEvent(Object object) {
        if (object instanceof SignatureFinishEvent) {
            //最后一步签名完成
            currentProgress = 4;
            mHandler.sendEmptyMessageDelayed(START_SPEAK_TEXT, 2000);
            //停止屏幕录制
            screenRecordHelper.stopRecord(0, 0, null);
            videoRecordProgress = 2;

            //开始视频预览
            binding.previewView.start();

        } else if (object instanceof ScreenRecordRefusedEvent) {
            //请求屏幕录制权限被拒绝
            finish();
        } else if (object instanceof ScreenRecordPassEvent) {

            Log.i(TAG, "屏幕录制权限请求完成，停止第一段视频录制，开始屏幕录制");
            //停止视频录制，开始屏幕录制
            binding.previewView.stopVideoRecord();

            //请求录屏权限成功
            ElectronicDocumentActivity.startActivity(OnlineFaceSign2Activity.this);

        } else if (object instanceof CameraOpenSuccessEvent) {
            //相机预览已经打开，开始视频录制
            Log.i(TAG, "开始视频录制，当前录制的是第" + videoRecordProgress + "段视频");
            binding.previewView.startVideoRecord(videoRecordProgress);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            screenRecordHelper.onActivityResult(requestCode, resultCode, data);
        }
        if (requestCode == ASR_REQUEST && resultCode == RESULT_OK && data != null) {
            //信息咨询回调
            if (currentProgress == 2) {
                //介绍接下来进行文档阅读和签名
                currentProgress = 3;
                mHandler.sendEmptyMessageDelayed(START_SPEAK_TEXT, 2000);
            }
            //询问是否是本人签名
            if (currentProgress == 4) {
                ToastUtils.setGravity(Gravity.CENTER, 0, 0);
                ToastUtils.showCustomLong(R.layout.dialog_asr_speech_success);
                ToastUtils.setGravity(-1, -1, -1);
                mHandler.sendEmptyMessageDelayed(FACE_SIGN_FINISH, 2000);
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
        isAlive = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        isAlive = false;
    }

    @Override
    protected void onDestroy() {
        if (myRecognizer != null) {
            myRecognizer.release();
        }
        if (synthesizer != null) {
            synthesizer.release();
        }
        super.onDestroy();
        if (needSaveScreenRecord && screenRecordHelper != null) {
            screenRecordHelper.stopRecord(0, 0, null);
        }

        binding.previewView.stop();
        EventBus.getDefault().unregister(this);
        mHandler.removeCallbacksAndMessages(null);
    }
}
