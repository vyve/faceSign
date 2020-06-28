package com.cin.facesign.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.baidu.aip.face.FaceFilter;
import com.blankj.utilcode.util.BrightnessUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.cin.facesign.Constant;
import com.cin.facesign.utils.FileUtils;
import com.cin.mylibrary.base.BaseViewModel;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by 王新超 on 2020/6/15.
 */
public class FaceIdentifyViewModel extends BaseViewModel {

    public ObservableField<Boolean> mSavedBitmap = new ObservableField<>();

    public FaceIdentifyViewModel(@NonNull Application application) {
        super(application);
        mSavedBitmap.set(false);
    }

    /**
     * 设置屏幕亮度
     */
    public void setBrightness() {
        //设置屏幕亮度
        int brightness = BrightnessUtils.getBrightness();
        if (brightness < 200) {
            BrightnessUtils.setBrightness(200);
        }
    }

    /**
     * 保存图片
     */
    public boolean saveFaceBmp(FaceFilter.TrackedModel model) {
        String filePath = Constant.FACE_IDENTIFY_LOCAL_PATH;
        final Bitmap face = model.cropFace();
        if (face != null) {
            ImageUtils.save(face, filePath, Bitmap.CompressFormat.JPEG);
        }
        File path = new File(filePath);
        if (!path.exists()) {
            filePath = "";
        }
        final File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }
        boolean saved = false;
        try {
            byte[] buf = FileUtils.readFile(file);
            if (buf.length > 0) {
                saved = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!saved) {
            Log.d("fileSize", "file size >=-99");
        } else {
            mSavedBitmap.set(true);
        }
        return saved;
    }


}
