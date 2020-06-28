package com.cin.mylibrary.viewadapter;

import android.os.SystemClock;
import android.view.View;

import androidx.databinding.BindingAdapter;

/**
 * Created by 王新超 on 2020/3/23.
 */
public class ViewViewAdapter {
    /**
     * 防止快速点击
     * @param closeFastClick 关闭防止快速点击 默认开启防止快速点击
     */
    @BindingAdapter(value = {"android:onClick","android:closeFastClick"}, requireAll = false)
    public static void setOnClick(View view, final View.OnClickListener clickListener,final boolean closeFastClick) {
        final long[] mHits = new long[2];
        view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!closeFastClick) {
                        System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                        mHits[mHits.length - 1] = SystemClock.uptimeMillis();
                        if (mHits[0] < (SystemClock.uptimeMillis() - 800)) {
                            clickListener.onClick(v);
                        }
                    }else {
                        clickListener.onClick(v);
                    }
                }
        });
    }

}
