package com.cin.facesign.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.baidu.aip.ImageFrame;
import com.baidu.aip.asrwakeup3.core.recog.MyRecognizer;
import com.baidu.aip.asrwakeup3.core.recog.listener.ChainRecogListener;
import com.baidu.aip.asrwakeup3.core.recog.listener.MessageStatusRecogListener;
import com.baidu.aip.face.FaceFilter;
import com.baidu.idl.facesdk.FaceInfo;
import com.baidu.speech.asr.SpeechConstant;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.cin.facesign.R;
import com.cin.facesign.control.FaceSignSynthesizer;
import com.cin.facesign.databinding.ActivityFaceSignFinishBinding;
import com.cin.facesign.utils.OCRUtil;
import com.cin.facesign.viewmodel.FaceSignFinishViewModel;
import com.cin.facesign.widget.dialog.asr.AsrSpeechDialog;
import com.cin.facesign.widget.dialog.asr.DigitalDialogInput;
import com.cin.facesign.widget.face.FaceSignDetectManager;
import com.cin.facesign.widget.face.FaceSignDetectRegionProcessor;
import com.cin.mylibrary.base.BaseActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 面签完成
 * Created by 王新超 on 2020/6/18.
 */
public class FaceSignFinishActivity extends BaseActivity<ActivityFaceSignFinishBinding, FaceSignFinishViewModel> {

    private MyRecognizer myRecognizer;

    public static void startActivity(Context context){
        context.startActivity(new Intent(context,FaceSignFinishActivity.class));
    }

    @Override
    public int initContentView(@Nullable Bundle savedInstanceState) {
        return R.layout.activity_face_sign_finish;
    }

    @Override
    public int initVariableId() {
        return 0;
    }

    @Override
    public void init() {


//        FaceSignDetectManager faceSignDetectManager = new FaceSignDetectManager(this);
//        faceSignDetectManager.setFaceSignTextureView(binding.faceSignTextureView);
////        faceSignDetectManager.setOCR(true);
//        faceSignDetectManager.setOnFaceDetectListener(new FaceSignDetectManager.OnFaceDetectListener() {
//            @Override
//            public void onDetectFace(int status, FaceInfo[] infos, ImageFrame imageFrame) {
//
//            }
//        });
//        faceSignDetectManager.setOnTrackListener(new FaceFilter.OnTrackListener() {
//            @Override
//            public void onTrack(FaceFilter.TrackedModel trackedModel) {
//                if (trackedModel.meetCriteria()) {
//                    showToast("识别到人脸");
//                }
//            }
//        });
//
//        binding.faceSignTextureView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                FaceSignDetectRegionProcessor processor = new FaceSignDetectRegionProcessor();
//                processor.setDetectedRect(binding.faceSignTextureView.getFaceIdentityRect());
//                faceSignDetectManager.addPreProcessor(processor);
//
//                faceSignDetectManager.startFaceIdentify();
//
//                binding.faceSignTextureView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//            }
//        });

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               finish();
            }
        });

    }

}
