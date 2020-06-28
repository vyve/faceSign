package com.cin.facesign.utils.imageloader;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;

import java.io.File;

public class LoaderOptions {
    public int placeholderResId;
    public int errorResId;
    public boolean isCenterCrop;
    public boolean isCenterInside;
    public boolean isFitCenter;
    public boolean noScaleType;
    /**
     * 是否使用缓存
     */
    public boolean useCacheStrategy = true;
    public Bitmap.Config config = Bitmap.Config.RGB_565;
    public int targetWidth;
    public int targetHeight;
    private int bitmapAngle; //圆角角度
    public Drawable placeholder;
    public ImageView imageView;//targetView展示图片
    public String url;
    public File file;
    public int drawableResId;
    public Uri uri;
    public byte[] bytes;
    public Bitmap bitmap;

    public LoaderOptions(String url) {
        this.url = url;
    }

    public LoaderOptions(File file) {
        this.file = file;
    }

    public LoaderOptions(int drawableResId) {
        this.drawableResId = drawableResId;
    }
    public LoaderOptions(Bitmap bitmap){
        this.bitmap = bitmap;
    }

    public LoaderOptions(Uri uri) {
        this.uri = uri;
    }

    public LoaderOptions(byte[] bytes){
        this.bytes = bytes;
    }

    public static void reset(){
    }

    public void into(ImageView imageView) {
        this.imageView = imageView;
        ImageLoader.getInstance().loadOptions(this);
    }


    public LoaderOptions placeholder(@DrawableRes int placeholderResId) {
        this.placeholderResId = placeholderResId;
        return this;
    }

    public LoaderOptions placeholder(Drawable placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    public LoaderOptions error(@DrawableRes int errorResId) {
        this.errorResId = errorResId;
        return this;
    }

    public LoaderOptions centerCrop() {
        isCenterCrop = true;
        return this;
    }

    public LoaderOptions centerInside() {
        isCenterInside = true;
        return this;
    }
    public LoaderOptions fitCenter(){
        isFitCenter = true;
        return this;
    }

    public LoaderOptions noScaleType(){
        noScaleType = true;
        return this;
    }

    public LoaderOptions config(Bitmap.Config config) {
        this.config = config;
        return this;
    }

    public LoaderOptions resize(int targetWidth, int targetHeight) {
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
        return this;
    }

    /**
     * 圆角
     * @param bitmapAngle   度数
     */
    public LoaderOptions angle(int bitmapAngle) {
        this.bitmapAngle = bitmapAngle;
        return this;
    }

    public LoaderOptions useCacheStrategy(boolean useCacheStrategy) {
        this.useCacheStrategy = useCacheStrategy;
        return this;
    }

    public int getBitmapAngle() {
        return bitmapAngle;
    }
}
