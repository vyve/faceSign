package com.cin.mylibrary.base;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;

/**
 * Created by 王新超 on 2020/6/18.
 */
public abstract class BaseDialog extends Dialog {

    protected View.OnClickListener listener1;
    protected View.OnClickListener listener2;

    public BaseDialog(@NonNull Context context) {
        super(context);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        setContentView(layoutId());
        if (getWindow()!=null) {
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            getWindow().setAttributes(params);
            //去除dialog自带的padding
            getWindow().getDecorView().setBackgroundColor(0x00000000);
        }
    }

    public abstract int layoutId();

    public void setOnButton1ClickListener(View.OnClickListener listener){
        this.listener1 = listener;
    }

    public void setOnButton2ClickListener(View.OnClickListener listener){
        this.listener2 = listener;
    }
}
