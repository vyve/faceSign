package com.cin.mylibrary.viewadapter;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.request.RequestOptions;

/**
 * Created by 王新超 on 2020/3/9.
 */
public class ImageViewAdapter {
    @SuppressLint("CheckResult")
    @BindingAdapter(value = {"android:url", "android:placeholder",
            "android:centerInside", "android:centerCrop", "android:noScaleType",
            "android:fitCenter","android:errorPlaceholder",
            "android:angle"}, requireAll = false)
    public static void setImageUri(ImageView imageView, String url, Drawable placeholder,
                                   boolean centerInside,boolean centerCrop,
                                   boolean noScaleType,boolean fitCenter,Drawable errorPlaceholder,int angle) {
        RequestOptions options = new RequestOptions();

        if (centerInside) {
            options.centerInside();
        } else if (centerCrop) {
            options.centerCrop();
        } else {
            if (!noScaleType) {
                options.centerCrop();
            }
        }
        if (errorPlaceholder != null) {
            options.error(errorPlaceholder);
        }
        if (placeholder != null) {
            options.placeholder(placeholder);
        }

        if (angle != 0) {
            //有圆角时 默认加载模式为centerCrop
            if (centerInside) {
                options.transform(new CenterInside(), new GlideRoundTransform(angle));
            } else if (fitCenter) {
                options.transform(new FitCenter(), new GlideRoundTransform(angle));
            } else {
                options.transform(new CenterCrop(), new GlideRoundTransform(angle));
            }
        }

        if (!TextUtils.isEmpty(url)) {
            Glide.with(imageView.getContext())
                    .load(url)
                    .apply(options)
                    .into(imageView);
        }
    }
}
