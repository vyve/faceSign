/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.cin.facesign.widget.face;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.baidu.aip.face.PreviewView;
import com.baidu.aip.face.camera.AutoFitTextureView;
import com.baidu.ocr.ui.camera.MaskView;

/**
 * 基于 系统TextureView实现的预览View;
 */
public class TexturePreviewView extends FrameLayout implements PreviewView {

    private AutoFitTextureView textureView;

    private int videoWidth = 0;
    private int videoHeight = 0;
    private boolean mirrored = true;
    private MaskView maskView;
    private Rect previewFrame = new Rect();
    /**
     * 识别类型
     * 0：人脸识别
     * 1：身份证OCR识别
     */
    private int identifyType = 0;
    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    public TexturePreviewView(@NonNull Context context) {
        super(context);
        init();
    }

    public TexturePreviewView(@NonNull Context context,
                              @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TexturePreviewView(@NonNull Context context, @Nullable AttributeSet attrs,
                              @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        textureView = new AutoFitTextureView(getContext());
        addView(textureView);

        maskView = new MaskView(getContext());
        addView(maskView);

    }

    /**
     * 识别类型
     * @param identifyType 0 人脸识别
     */
    public void setIdentifyType(int identifyType){
        this.identifyType = identifyType;
    }

    @Override
    public int getIdentifyType(){
        return identifyType;
    }

    @Override
    public Rect getPreviewFrame() {
        return previewFrame;
    }

    /**
     * 有些ImageSource如系统相机前置设置头为镜面效果。这样换算坐标的时候会不一样
     * @param mirrored 是否为镜面效果。
     */
    public void setMirrored(boolean mirrored) {
        this.mirrored = mirrored;
    }

    /**
     * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
     * calculated from the parameters. Note that the actual sizes of parameters don't matter, that
     * is, calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
     *
     * @param width  Relative horizontal size
     * @param height Relative vertical size
     */
    @Override
    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
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
        previewFrame.bottom = bottom-top;
        previewFrame.right = right;
        maskView.layout(left, 0, right, bottom - top);
        int selfWidth = getWidth();
        int selfHeight = getHeight();
        if (videoWidth == 0 || videoHeight == 0 || selfWidth == 0 || selfHeight == 0) {
            return;
        }
        ScaleType scaleType = resolveScaleType();
        if (scaleType == ScaleType.FIT_HEIGHT) {
            int targetWith = videoWidth * selfHeight / videoHeight;
            int delta = (targetWith - selfWidth) / 2;
            textureView.layout(left - delta, top, right + delta, bottom);
        } else {
            int targetHeight = videoHeight * selfWidth / videoWidth;
            int delta = (targetHeight - selfHeight) / 2;
            textureView.layout(left, top - delta, right, bottom + delta);
        }
    }

    @Override
    public AutoFitTextureView getTextureView() {
        return textureView;
    }

    /**
     * 获取人脸识别实际区域
     */
    public RectF getFaceIdentifyRectF(){
        return maskView.getFaceIdentifyRectF();
    }

    public RectF getOCRIdentifyRect(){
        return maskView.getIdCardIdentifyRect();
    }

    public MaskView getMaskView(){
        return maskView;
    }

    @Override
    public void setPreviewSize(int width, int height) {
        if (this.videoWidth == width && this.videoHeight == height) {
            return;
        }
        this.videoWidth = width;
        this.videoHeight = height;
        handler.post(new Runnable() {
            @Override
            public void run() {
                requestLayout();
            }
        });

    }

    @Override
    public void mapToOriginalRect(RectF rectF) {

        int selfWidth = getWidth();
        int selfHeight = getHeight();
        if (videoWidth == 0 || videoHeight == 0 || selfWidth == 0 || selfHeight == 0) {
            return;
        }

        Matrix matrix = new Matrix();
        ScaleType scaleType = resolveScaleType();
        if (scaleType == ScaleType.FIT_HEIGHT) {
            int targetWith = videoWidth * selfHeight / videoHeight;
            int delta = (targetWith - selfWidth) / 2;
            float ratio = 1.0f * videoHeight / selfHeight;
            matrix.postTranslate(delta, 0);
            matrix.postScale(ratio, ratio);
        } else {
            int targetHeight = videoHeight * selfWidth / videoWidth;
            int delta = (targetHeight - selfHeight) / 2;

            float ratio = 1.0f * videoWidth / selfWidth;
            matrix.postTranslate(0, delta);
            matrix.postScale(ratio, ratio);
        }
        matrix.mapRect(rectF);
    }

    @Override
    public void mapFromOriginalRect(RectF rectF) {
        int selfWidth = getWidth();
        int selfHeight = getHeight();
        if (videoWidth == 0 || videoHeight == 0 || selfWidth == 0 || selfHeight == 0) {
            return;
        }

        Matrix matrix = new Matrix();

        ScaleType scaleType = resolveScaleType();
        if (scaleType == ScaleType.FIT_HEIGHT) {
            int targetWith = videoWidth * selfHeight / videoHeight;
            int delta = (targetWith - selfWidth) / 2;

            float ratio = 1.0f * selfHeight / videoHeight;

            matrix.postScale(ratio, ratio);
            matrix.postTranslate(-delta, 0);
        } else {
            int targetHeight = videoHeight * selfWidth / videoWidth;
            int delta = (targetHeight - selfHeight) / 2;

            float ratio = 1.0f * selfWidth / videoWidth;

            matrix.postScale(ratio, ratio);
            matrix.postTranslate(0, -delta);
        }
        matrix.mapRect(rectF);

        if (mirrored) {
            float left = selfWidth - rectF.right;
            float right = left + rectF.width();
            rectF.left = left;
            rectF.right = right;
        }
    }

    @Override
    public void mapFromOriginalRectEx(RectF rectF) {
        int selfWidth = getWidth();
        int selfHeight = getHeight();
        if (videoWidth == 0 || videoHeight == 0 || selfWidth == 0 || selfHeight == 0) {
            return;
            // TODO
        }
        Matrix matrix = new Matrix();
        float ratio = 1.0f * selfWidth / videoWidth;
        matrix.postScale(ratio, ratio);
      //  matrix.postTranslate(0, 0);
        matrix.mapRect(rectF);

        if (mirrored) {
            float left = selfWidth - rectF.right;
            float right = left + rectF.width();
            rectF.left = left;
            rectF.right = right;
        }
    }


    private ScaleType resolveScaleType() {
        if (getHeight() <= 0 || videoHeight <= 0) {
            return ScaleType.CROP_INSIDE;
        }
        float selfRatio = 1.0f * getWidth() / getHeight();
        float targetRatio = 1.0f * videoWidth / videoHeight;

        ScaleType scaleType = this.scaleType;
        if (this.scaleType == ScaleType.CROP_INSIDE) {
            scaleType = selfRatio > targetRatio ? ScaleType.FIT_WIDTH : ScaleType.FIT_HEIGHT;
        }
        return scaleType;
    }

    private ScaleType scaleType = ScaleType.CROP_INSIDE;
    private Handler handler = new Handler(Looper.getMainLooper());

}
