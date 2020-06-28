package com.cin.facesign.widget.face;

import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.TextureView;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.baidu.aip.face.PreviewView;
import com.baidu.aip.face.camera.AutoFitTextureView;
import com.baidu.ocr.ui.camera.MaskView;

/**
 * Created by 王新超 on 2020/6/22.
 */
public class FaceSignTextureView extends FrameLayout implements PreviewView{
    private Context context;
    private AutoFitTextureView mTextureView;
    private MaskView mMaskView;

    private int mVideoWidth = 0;
    private int mVideoHeight = 0;
    /**
     * 预览 frame Rect
     */
    private Rect previewFrame = new Rect();
    private FaceSignManager mFaceSignManager;
    private Handler handler = new Handler(Looper.getMainLooper());
    private int mRatioWidth = 0;
    private int mRatioHeight = 0;
    /**
     * 识别类型
     * 0：人脸识别
     * 1：身份证OCR识别
     */
    private int identifyType = 0;

    public FaceSignTextureView(@NonNull Context context) {
        this(context, null);
    }

    public FaceSignTextureView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FaceSignTextureView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        mTextureView = new AutoFitTextureView(context);
        addView(mTextureView);

        mMaskView = new MaskView(context);
        addView(mMaskView);

        mFaceSignManager = new FaceSignManager((Activity) context, this);
    }

    public AutoFitTextureView getTextureView() {
        return mTextureView;
    }

    @Override
    public void setAspectRatio(int width, int height) {

    }

    public MaskView getMaskView(){
        return mMaskView;
    }

    /**
     * 识别类型
     *
     * @param identifyType 0 人脸识别
     */
    public void setIdentifyType(int identifyType) {
        this.identifyType = identifyType;
    }

    public int getIdentifyType() {
        return identifyType;
    }

    @Override
    public Rect getPreviewFrame() {
        return previewFrame;
    }

    /**
     * 人脸识别的实际区域
     */
    public RectF getFaceIdentityRect() {
        return mMaskView.getFaceIdentifyRectF();
    }

    public void setPreviewSize(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mVideoWidth = width;
        mVideoHeight = height;

        mRatioWidth = width;
        mRatioHeight = height;
        handler.post(new Runnable() {
            @Override
            public void run() {
                requestLayout();
            }
        });
    }

    /**
     * 预览View中的坐标映射到，原始图片中。应用场景举例：裁剪框
     */
    public void mapToOriginalRect(RectF rectF) {
        int selfWidth = getWidth();
        int selfHeight = getHeight();
        if (mVideoWidth == 0 || mVideoHeight == 0 || selfWidth == 0 || selfHeight == 0) {
            return;
        }

        Matrix matrix = new Matrix();
        int targetHeight = mVideoHeight * selfWidth / mVideoWidth;
        int delta = (targetHeight - selfHeight) / 2;

        float ratio = 1.0f * mVideoWidth / selfWidth;
        matrix.postTranslate(0, delta);
        matrix.postScale(ratio, ratio);
        matrix.mapRect(rectF);
    }

    @Override
    public void mapFromOriginalRect(RectF rectF) {

    }

    @Override
    public void mapFromOriginalRectEx(RectF rectF) {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        previewFrame.left = left;
        previewFrame.top = 0;
        previewFrame.bottom = bottom - top;
        previewFrame.right = right;
        int selfWidth = getWidth();
        int selfHeight = getHeight();
        if (mVideoWidth == 0 || mVideoHeight == 0 || selfWidth == 0 || selfHeight == 0) {
            return;
        }
        int targetHeight = mVideoHeight * selfWidth / mVideoWidth;
        int delta = (targetHeight - selfHeight) / 2;
//        mTextureView.layout(left, top - delta, right, bottom + delta);
//        mMaskView.layout(left, top - delta, right, bottom + delta);
        mTextureView.layout(left, 0, right, bottom - top);
        mMaskView.layout(left, 0, right, bottom - top);
    }
}
