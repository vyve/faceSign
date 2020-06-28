package com.cin.facesign.widget;

import android.content.Context;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.cin.facesign.R;
import com.cin.mylibrary.base.BaseDialog;

/**
 * 身份证验证成功
 * Created by 王新超 on 2020/6/18.
 */
public class CertificationSuccessDialog extends BaseDialog {
    public CertificationSuccessDialog(@NonNull Context context) {
        super(context);
        if (getWindow()!=null) {
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            getWindow().setAttributes(params);
            //去除dialog自带的padding
            getWindow().getDecorView().setBackgroundColor(0x00000000);
        }
    }

    @Override
    public int layoutId() {
        return R.layout.dialog_certification_success;
    }
}
