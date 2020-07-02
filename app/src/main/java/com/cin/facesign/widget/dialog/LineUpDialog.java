package com.cin.facesign.widget.dialog;

import android.content.Context;

import androidx.annotation.NonNull;

import com.cin.facesign.R;
import com.cin.mylibrary.base.BaseDialog;

/**
 * 正在排队
 * Created by 王新超 on 2020/6/30.
 */
public class LineUpDialog extends BaseDialog {
    public LineUpDialog(@NonNull Context context) {
        super(context);
        findViewById(R.id.cancel).setOnClickListener(v -> dismiss());
    }

    @Override
    public int layoutId() {
        return R.layout.dialog_line_up;
    }
}
