/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.cin.facesign.utils.face2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.core.content.res.ResourcesCompat;

import com.baidu.ocr.ui.R;


@SuppressWarnings("unused")
public class MaskView2 extends View {

    private Drawable faceOutlineDrawable;

    public void setLineColor(int lineColor) {
        this.lineColor = lineColor;
    }

    public void setMaskColor(int maskColor) {
        this.maskColor = maskColor;
    }

    private int lineColor = Color.WHITE;


    private int maskColor = Color.argb(100, 0, 0, 0);

    private Paint eraser = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint pen = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Rect frame = new Rect();
    private Rect faceOutlineFrame = new Rect();

    private Drawable locatorDrawable;


    public MaskView2(Context context) {
        super(context);
        init();
    }

    public MaskView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MaskView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    public Rect getFrameRect() {
        return new Rect(frame);

    }

    public Rect getFrameRectExtend() {
        Rect rc = new Rect(frame);
        int widthExtend = (int) ((frame.right - frame.left) * 0.2f);
        int heightExtend = (int) ((frame.bottom - frame.top) * 0.2f);
        rc.left -= widthExtend;
        rc.right += widthExtend;
        rc.top -= heightExtend;
        rc.bottom += heightExtend;
        return rc;
    }

    private void init() {
        locatorDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.bd_ocr_id_card_locator_front, null);
        faceOutlineDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.bd_ocr_id_card_locator_front, null);
    }

    /**
     * 获取人脸识别区域
     */
    public RectF getFaceIdentifyRectF() {
        RectF rectF = new RectF();
        rectF.left = faceOutlineFrame.left;
        rectF.top = faceOutlineFrame.top;
        rectF.right = faceOutlineFrame.right;
        rectF.bottom = faceOutlineFrame.top + getHeight();
        return rectF;
    }

    /**
     * 获取身份证识别区域
     */
    public RectF getIdCardIdentifyRect() {

        RectF rectF = new RectF();
        rectF.left = frame.left;
        rectF.top = frame.top;
        rectF.right = frame.right;
        rectF.bottom = frame.bottom;
        return rectF;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
//                float ratio = h > w ? 0.9f : 0.72f;
            float ratio = 0.5f;

            int width = (int) (w * ratio);
            int height = width * 400 / 620;

            int left = (w - width) / 2;
            int top = h / 2 + (h / 2 - height) / 2 + 100;
//                int top = (h - height) / 2;
            int right = width + left;
            int bottom = height + top;

            frame.left = left;
            frame.top = top;
            frame.right = right;
            frame.bottom = bottom;

            faceOutlineFrame.left = 0;
            faceOutlineFrame.top = h - w * 366 / 362;
            faceOutlineFrame.right = w;
            faceOutlineFrame.bottom = h;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Rect frame = this.frame;

        int width = frame.width();
        int height = frame.height();

        int left = frame.left;
        int top = frame.top;
        int right = frame.right;
        int bottom = frame.bottom;

        canvas.drawColor(maskColor);
        fillRectRound(left, top, right, bottom, 30, 30, false);
        canvas.drawPath(path, pen);
        canvas.drawPath(path, eraser);

        locatorDrawable.setBounds(
                (int) (left + 601f / 1006 * width),
                (int) (top + (110f / 632) * height),
                (int) (left + (963f / 1006) * width),
                (int) (top + (476f / 632) * height));
        faceOutlineDrawable.setBounds(
                (int) (faceOutlineFrame.left),
                (int) (faceOutlineFrame.top-150),
                (int) (faceOutlineFrame.right),
                (int) (faceOutlineFrame.bottom-150));

        if (faceOutlineDrawable != null) {
            faceOutlineDrawable.draw(canvas);
        }
        if (locatorDrawable != null) {
            locatorDrawable.draw(canvas);
        }
    }

    private Path path = new Path();

    private Path fillRectRound(float left, float top, float right, float bottom, float rx, float ry, boolean conformToOriginalPost) {

        path.reset();
        if (rx < 0) {
            rx = 0;
        }
        if (ry < 0) {
            ry = 0;
        }
        float width = right - left;
        float height = bottom - top;
        if (rx > width / 2) {
            rx = width / 2;
        }
        if (ry > height / 2) {
            ry = height / 2;
        }
        float widthMinusCorners = (width - (2 * rx));
        float heightMinusCorners = (height - (2 * ry));

        path.moveTo(right, top + ry);
        path.rQuadTo(0, -ry, -rx, -ry);
        path.rLineTo(-widthMinusCorners, 0);
        path.rQuadTo(-rx, 0, -rx, ry);
        path.rLineTo(0, heightMinusCorners);

        if (conformToOriginalPost) {
            path.rLineTo(0, ry);
            path.rLineTo(width, 0);
            path.rLineTo(0, -ry);
        } else {
            path.rQuadTo(0, ry, rx, ry);
            path.rLineTo(widthMinusCorners, 0);
            path.rQuadTo(rx, 0, rx, -ry);
        }

        path.rLineTo(0, -heightMinusCorners);
        path.close();
        return path;
    }

    {
        // 硬件加速不支持，图层混合。
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        pen.setColor(Color.WHITE);
        pen.setStyle(Paint.Style.STROKE);
        pen.setStrokeWidth(6);

        eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }
}
