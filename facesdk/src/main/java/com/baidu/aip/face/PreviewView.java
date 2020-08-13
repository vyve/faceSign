/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.face;

import android.graphics.Rect;
import android.graphics.RectF;
import android.view.TextureView;
import android.view.View;

import com.baidu.aip.face.camera.AutoFitTextureView;

/**
 * 该类用于实现，图片源(@see ImageSource)数据预览。
 */

public interface PreviewView {

    /**
     * 图片帧缩放类型。
     */
    enum ScaleType {

        /**
         * 宽度与父控件一致，高度自适应
         */
        FIT_WIDTH,
        /**
         * 调试与父控件一致，宽度自适应
         */
        FIT_HEIGHT,
        /**
         * 全屏显示 ，保持显示比例，多余的部分会被裁剪掉。
         */
        CROP_INSIDE,
    }

    AutoFitTextureView getTextureView();


    /**
     * 设置帧大小。
     *
     * @param width  帧宽度
     * @param height 帧调试
     */
    void setPreviewSize(int width, int height);

    /**
     * 预览View中的坐标映射到，原始图片中。应用场景举例：裁剪框
     *
     * @param rect 预览View中的坐标
     */
    void mapToOriginalRect(RectF rect);

    /**
     * 原始图片中的坐标到预览View坐标中的映射。应用场景举例：预览页面显示人脸框。
     *
     * @param rectF 原始图中的坐标
     */
    void mapFromOriginalRect(RectF rectF);


    void mapFromOriginalRectEx(RectF rectF);

    /**
     * 获取扫描类型
     * @return 0 人脸扫描
     *          1 OCR扫描
     */
    int getIdentifyType();

    int getWidth();

    int getHeight();

    /**
     * 看到的预览可能不是照片的全部。返回预览视图的全貌。
     * @return 预览视图frame;
     */
    Rect getPreviewFrame();
}
