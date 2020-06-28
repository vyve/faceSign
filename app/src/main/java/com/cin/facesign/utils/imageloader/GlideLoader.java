package com.cin.facesign.utils.imageloader;

import android.annotation.SuppressLint;
import android.os.Looper;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.request.RequestOptions;
import com.cin.facesign.MyApp;

/**
 * Created by 王新超 on 2018/6/13.
 */
public class GlideLoader implements ILoaderProxy {
    @SuppressLint("CheckResult")
    @Override
    public void loadImage(LoaderOptions options) {
        RequestOptions glideOptions = new RequestOptions();
        if (options.isCenterInside) {
            glideOptions.centerInside();
        } else if (options.isCenterCrop) {
            glideOptions.centerCrop();
        } else {
            if (!options.noScaleType) {
                glideOptions.centerCrop();
            }
        }
        if (options.errorResId != 0) {
            glideOptions.error(options.errorResId);
        } else {
//            glideOptions.error(UIUtil.getDrawable(R.mipmap.default_square_img));
        }
        if (options.placeholderResId != 0) {
            glideOptions.placeholder(options.placeholderResId);
        }
//        else {
//            glideOptions.placeholder(UIUtil.getDrawable(R.mipmap.default_square_img));
//        }
        if (options.getBitmapAngle() != 0) {
            //有圆角时 默认加载模式为centerCrop
            if (options.isCenterInside) {
                glideOptions.transforms(new CenterInside(), new GlideRoundTransform(options.getBitmapAngle()));
            } else if (options.isFitCenter) {
                glideOptions.transforms(new FitCenter(), new GlideRoundTransform(options.getBitmapAngle()));
            } else {
                glideOptions.transforms(new CenterCrop(), new GlideRoundTransform(options.getBitmapAngle()));
            }
        }
        if (!options.useCacheStrategy) {
            glideOptions.diskCacheStrategy(DiskCacheStrategy.NONE);
        } else {
            glideOptions.diskCacheStrategy(DiskCacheStrategy.ALL);
        }
        if (options.url != null) {
            load(options.url, options.imageView, glideOptions);
        } else if (options.file != null) {
            load(options.file, options.imageView, glideOptions);
        } else if (options.drawableResId != 0) {
            load(options.drawableResId, options.imageView, glideOptions);
        } else if (options.uri != null) {
            load(options.uri, options.imageView, glideOptions);
        } else if (options.bytes != null) {
            load(options.bytes, options.imageView, glideOptions);
        } else if (options.bitmap != null) {
            load(options.bitmap, options.imageView, glideOptions);
        }

    }

    private void load(Object object, ImageView imageView, RequestOptions options) {
        Glide.with(MyApp.getInstance())
                .load(object)
                .apply(options)
                .into(imageView);
    }

    @Override
    public void clearMemoryCache() {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) { //只能在主线程执行
                Glide.get(MyApp.getInstance()).clearMemory();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clearDiskCache() {
        Glide.get(MyApp.getInstance()).clearDiskCache();
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.get(MyApp.getInstance()).clearDiskCache();
                    }
                }).start();
            } else {
                Glide.get(MyApp.getInstance()).clearDiskCache();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
