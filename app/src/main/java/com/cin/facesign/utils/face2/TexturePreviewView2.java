package com.cin.facesign.utils.face2;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.baidu.aip.face.camera.AutoFitTextureView;


/**
 * 人脸识别预览框
 * Created by 王新超 on 2020/8/17.
 */
public class TexturePreviewView2 extends FrameLayout {

    private AutoFitTextureView textureView;
    private VideoRecord videoRecord;
    private IVideoRecordFrameListener listener;
    private Context context;

    public TexturePreviewView2(@NonNull Context context) {
        this(context,null);
    }

    public TexturePreviewView2(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public TexturePreviewView2(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        textureView = new AutoFitTextureView(context);
        addView(textureView);
        MaskView2 maskView = new MaskView2(getContext());
        addView(maskView);
    }

    public void start(){
        videoRecord.onResume();
    }
    /**
     * 面签系统视频录制类型
     * @param type 1 第一段视频录制
     *             2 第二段视频录制
     */
    public void startVideoRecord(int type){
        videoRecord.startRecordingVideo(type);
    }

    public void stop(){
        videoRecord.onPause();
    }
    public void stopVideoRecord(){
        videoRecord.stopRecordingVideo();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        textureView.layout(left , top, right , bottom);
    }
    public void setOnFrameListener(IVideoRecordFrameListener listener){
        this.listener = listener;
        videoRecord = new VideoRecord((FragmentActivity) context,textureView,listener);
    }
}
