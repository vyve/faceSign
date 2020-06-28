package com.cin.mylibrary.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.cin.mylibrary.R;


/**
 * Created by 王新超 on 2020/6/16.
 */
public class LoadingDialog extends Dialog {
    public LoadingDialog(@NonNull Context context) {
        super(context);
        setCancelable(true);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.layout_loading_dialog);//loading的xml文件
        if (getWindow()!=null) {
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            getWindow().setAttributes(params);
        }
    }
}
