package com.cin.facesign.widget.dialog.asr;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import com.cin.facesign.R;
import com.cin.facesign.ui.FaceSignFinishActivity;
import com.cin.facesign.ui.OnlineFaceSignActivity;
import com.cin.facesign.widget.voiceline.VoiceLineView;

/**
 * Created by 王新超 on 2020/6/29.
 */
public class AsrSpeechDialog extends BaiduASRDialog {

    private VoiceLineView voiceLineView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_asr_speech, null);
        voiceLineView = view.findViewById(R.id.voiceLineView);
        view.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancleRecognition();
                finish();
            }
        });
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(new View(this));
        ViewGroup.LayoutParams param = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addContentView(view, param);
        getWindow().setBackgroundDrawable(new ColorDrawable(0));

        startRecognition();
    }

    @Override
    protected void onRecognitionStart() {

    }

    @Override
    protected void onPrepared() {

    }

    @Override
    protected void onBeginningOfSpeech() {

    }

    @Override
    protected void onVolumeChanged(float volume) {
        voiceLineView.setVolume((int) volume);
    }

    @Override
    protected void onEndOfSpeech() {

    }

    @Override
    protected void onFinish(int errorType, int errorCode) {
        if (errorType==0) {
            //如果没有识别出“确认”和“否认” 就重新去识别
            startRecognition();
        }
    }

    @Override
    protected void onPartialResults(String[] results) {
        if (results != null && results.length > 0) {
            if (results[0].startsWith("确认")||results[0].startsWith("否认")) {
                cancleRecognition();
                Intent intent = new Intent(this, FaceSignFinishActivity.class);
                intent.putExtra("result",results[0]);
                setResult(RESULT_OK,intent);
                finish();
            }
        }
    }
}
