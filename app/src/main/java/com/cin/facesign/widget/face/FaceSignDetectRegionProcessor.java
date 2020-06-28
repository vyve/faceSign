package com.cin.facesign.widget.face;

import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.baidu.aip.ImageFrame;
import com.baidu.aip.face.FaceCropper;
import com.baidu.aip.face.camera.CameraImageSource;

/**
 * 裁剪一定区域内的图片进行检测。
 * Created by 王新超 on 2020/6/23.
 */
public class FaceSignDetectRegionProcessor {
    private RectF detectedRect;

    private RectF originalCoordinate = new RectF();

    /**
     * 设置裁剪的区域。该区域内的图片被会裁剪进行检测，其余会被抛弃。
     * @param rect 检测区域
     */
    public void setDetectedRect(RectF rect) {
        detectedRect = rect;

        Log.i("aaaaa",rect.left+","+rect.right+","+rect.top+","+rect.bottom);
    }


    private Rect cropRect = new Rect();

    public boolean process(FaceSignDetectManager faceDetectManager, ImageFrame frame) {
        if (detectedRect != null) {
            originalCoordinate.set(detectedRect);
            FaceSignTextureView textureView = faceDetectManager.getFaceSignTextureView();
            textureView.mapToOriginalRect(originalCoordinate);
            cropRect.left = (int) originalCoordinate.left;
            cropRect.top = (int) originalCoordinate.top;
            cropRect.right = (int) originalCoordinate.right;
            cropRect.bottom = (int) originalCoordinate.bottom;
            frame.setArgb(FaceCropper.crop(frame.getArgb(), frame.getWidth(), cropRect));
            frame.setWidth(cropRect.width());
            frame.setHeight(cropRect.height());
        }
        return false;
    }
}
