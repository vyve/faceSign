package com.cin.facesign.utils.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import java.io.File;

/**
 * 图片管理类，提供对外接口。
 * 静态代理模式，开发者只需要关心ImageLoader + LoaderOptions
 */
public class ImageLoader{
    private static ILoaderProxy sLoader;
    private static volatile ImageLoader sInstance;
    private Context context;
    private ImageLoader() {
    }

    public static ImageLoader getInstance() {
        if (sInstance == null) {
            synchronized (ImageLoader.class) {
                if (sInstance == null) {
                    sInstance = new ImageLoader();
                }
            }
        }
        return sInstance;
    }

    /**
     * 在Application中设置 图片加载框架
     */
    public void setImageLoader(ILoaderProxy loader) {
        if (loader != null) {
            sLoader = loader;
        }
    }

    public LoaderOptions load(String path) {
        return new LoaderOptions(path);
    }

    public LoaderOptions load(int drawable) {
        return new LoaderOptions(drawable);
    }

    public LoaderOptions load(File file) {
        return new LoaderOptions(file);
    }

    public LoaderOptions load(Uri uri) {
        return new LoaderOptions(uri);
    }

    public LoaderOptions load(byte[] bytes){
        return new LoaderOptions(bytes);
    }
    public LoaderOptions load(Bitmap bitmap){
        return new LoaderOptions(bitmap);
    }

    public void loadOptions(LoaderOptions options) {
        sLoader.loadImage(options);
    }

    public void clearMemoryCache() {
        sLoader.clearMemoryCache();
    }

    public void clearDiskCache() {
        sLoader.clearDiskCache();
    }
}
