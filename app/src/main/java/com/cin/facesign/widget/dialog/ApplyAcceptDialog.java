package com.cin.facesign.widget.dialog;

import android.content.Context;

import androidx.annotation.NonNull;

import com.cin.facesign.R;
import com.cin.mylibrary.base.BaseDialog;

/**
 * 申请已受理dialog
 * Created by 王新超 on 2020/6/18.
 */
public class ApplyAcceptDialog extends BaseDialog {
    public ApplyAcceptDialog(@NonNull Context context) {
        super(context);

        findViewById(R.id.close).setOnClickListener(v -> {
            dismiss();
            listener1.onClick(v);
        });
    }

    @Override
    public int layoutId() {
        return R.layout.dialog_apply_accept;
    }
}
