package com.cin.facesign.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;

import com.baidu.aip.ImageFrame;
import com.baidu.aip.face.PreviewView;
import com.baidu.idcardquality.IDcardQualityProcess;
import com.baidu.ocr.ui.camera.MaskView;
import com.baidu.ocr.ui.util.ImageUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by 王新超 on 2020/6/18.
 */
public class OCRUtil {
    public static int detect(ImageFrame.OCRFrame ocrFrame, MaskView maskView, PreviewView previewView) {
        byte[] data = ocrFrame.getData();
        int optWidth = ocrFrame.getOptWidth();
        int optHeight = ocrFrame.getOptHeight();
        int rotation = ocrFrame.getRotation();
        if (data == null) {
            return -1;
        }

        YuvImage img = new YuvImage(data, ImageFormat.NV21, optWidth, optHeight, null);
        ByteArrayOutputStream os = null;
        BitmapRegionDecoder decoder = null;
        try {
            os = new ByteArrayOutputStream(data.length);
            img.compressToJpeg(new Rect(0, 0, optWidth, optHeight), 80, os);
            byte[] jpeg = os.toByteArray();

            decoder = BitmapRegionDecoder.newInstance(jpeg, 0, jpeg.length, true);
        } catch (OutOfMemoryError | IOException e) {
            // 内存溢出则取消当次操作
            e.printStackTrace();
        } finally {
            try {
                os.close();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        int width = rotation % 180 == 0 ? decoder.getWidth() : decoder.getHeight();
        int height = rotation % 180 == 0 ? decoder.getHeight() : decoder.getWidth();

        Rect frameRect = maskView.getFrameRectExtend();

        int ocrMaskWidth = (int) (maskView.getIdCardIdentifyRect().right - maskView.getIdCardIdentifyRect().left);
        int ocrMaskHeight = (int) (maskView.getIdCardIdentifyRect().bottom-maskView.getIdCardIdentifyRect().top);

//        int left = width * frameRect.left / ocrMaskWidth;
//        int top = height * frameRect.top / ocrMaskHeight;
//        int right = width * frameRect.right / ocrMaskWidth;
//        int bottom = height * frameRect.bottom / ocrMaskHeight;

        int left = frameRect.left;
        int top = frameRect.top;
        int right = frameRect.right;
        int bottom =frameRect.bottom;

        Rect previewFrame = previewView.getPreviewFrame();
        // 高度大于图片
        if (previewFrame.top < 0) {
            // 宽度对齐。
            int adjustedPreviewHeight = previewFrame.height() * previewView.getWidth() / previewFrame.width();
            int topInFrame = ((adjustedPreviewHeight - frameRect.height()) / 2)
                    * previewView.getWidth() / previewFrame.width();
            int bottomInFrame = ((adjustedPreviewHeight + frameRect.height()) / 2) * previewView.getWidth()
                    / previewFrame.width();

            // 等比例投射到照片当中。
            top = topInFrame * height / previewFrame.height();
            bottom = bottomInFrame * height / previewFrame.height();
        } else {
            // 宽度大于图片
            if (previewFrame.left < 0) {
                // 高度对齐
                int adjustedPreviewWidth = previewFrame.width() * previewView.getHeight() / previewFrame.height();
                int leftInFrame = ((adjustedPreviewWidth - maskView.getFrameRect().width()) / 2) * previewView.getHeight()
                        / previewFrame.height();
                int rightInFrame = ((adjustedPreviewWidth + maskView.getFrameRect().width()) / 2) * previewView.getHeight()
                        / previewFrame.height();

                // 等比例投射到照片当中。
                left = leftInFrame * width / previewFrame.width();
                right = rightInFrame * width / previewFrame.width();
            }
        }

        Rect region = new Rect();
        region.left = left;
        region.top = top;
        region.right = right;
        region.bottom = bottom;

        // 90度或者270度旋转
        if (rotation % 180 == 90) {
            int x = decoder.getWidth() / 2;
            int y = decoder.getHeight() / 2;

            int rotatedWidth = region.height();
            int rotated = region.width();

            // 计算，裁剪框旋转后的坐标
            region.left = x - rotatedWidth / 2;
            region.top = y - rotated / 2;
            region.right = x + rotatedWidth / 2;
            region.bottom = y + rotated / 2;
            region.sort();
        }

        BitmapFactory.Options options = new BitmapFactory.Options();

        // 最大图片大小。
        int maxPreviewImageSize = 2560;
        int size = Math.min(decoder.getWidth(), decoder.getHeight());
        size = Math.min(size, maxPreviewImageSize);

        options.inSampleSize = ImageUtil.calculateInSampleSize(options, size, size);
        options.inScaled = true;
        options.inDensity = Math.max(options.outWidth, options.outHeight);
        options.inTargetDensity = size;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = decoder.decodeRegion(region, options);

        if (rotation != 0) {
            // 只能是裁剪完之后再旋转了。有没有别的更好的方案呢？
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            Bitmap rotatedBitmap = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
            if (bitmap != rotatedBitmap) {
                // 有时候 createBitmap会复用对象
                bitmap.recycle();
            }
            bitmap = rotatedBitmap;
        }

        int status = IDcardQualityProcess.getInstance().idcardQualityDetectionImg(bitmap, true);

        return status;
    }

}