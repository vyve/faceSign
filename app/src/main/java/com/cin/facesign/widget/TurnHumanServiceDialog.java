package com.cin.facesign.widget;

import android.content.Context;

import androidx.annotation.NonNull;

import com.cin.facesign.R;
import com.cin.mylibrary.base.BaseDialog;

/**
 * 转人工面签
 * Created by 王新超 on 2020/6/18.
 */
public class TurnHumanServiceDialog extends BaseDialog {
    public TurnHumanServiceDialog(@NonNull Context context) {
        super(context);
        findViewById(R.id.close).setOnClickListener(v -> dismiss());

        findViewById(R.id.human).setOnClickListener(v -> {
            if (listener1 != null) {
                listener1.onClick(v);
            }
            dismiss();
        });
        findViewById(R.id.online).setOnClickListener(v -> {
            if (listener2 != null) {
                listener2.onClick(v);
            }
            dismiss();
        });
    }

    @Override
    public int layoutId() {
        return R.layout.dialog_turn_human_service;
    }
}
