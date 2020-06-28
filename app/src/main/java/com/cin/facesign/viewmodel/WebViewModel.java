package com.cin.facesign.viewmodel;

import android.app.Application;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.cin.mylibrary.base.BaseViewModel;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by 王新超 on 2020/6/18.
 */
public class WebViewModel extends BaseViewModel {

    public ObservableField<String> title = new ObservableField<>();
    public ObservableField<Boolean> showError = new ObservableField<>();
    public ObservableField<Boolean> showWebView = new ObservableField<>();
    public ObservableField<Boolean> showProgressBar = new ObservableField<>();

    public WebViewModel(@NonNull Application application) {
        super(application);
        showError.set(false);
        showWebView.set(true);
        showProgressBar.set(false);
    }

    public void getCookie(final String path) {
        new Thread(() -> {
            try {
                URL url = new URL(path);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");

                conn.connect();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                    Map<String, List<String>> headFields = conn.getHeaderFields();
                    List<String> list = headFields.get("Set-Cookie");
                    if (list != null) {
                        CookieSyncManager.createInstance(getApplication().getApplicationContext());
                        CookieManager cookieManager = CookieManager.getInstance();
                        for (String cookie : list) {
                            cookieManager.setCookie(path, cookie);
                        }
                        CookieSyncManager.getInstance().sync();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }


    /**
     * 将cookie同步到WebView
     *
     * @param url    WebView要加载的url
     * @param cookie 要同步的cookie
     * @return true 同步cookie成功，false同步cookie失败
     */
    public boolean syncCookie(String url, String cookie) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(getApplication().getApplicationContext());
        }
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setCookie(url, cookie);
        //如果没有特殊需求，这里只需要将session id以"key=value"形式作为cookie即可
        String newCookie = cookieManager.getCookie(url);
        return !TextUtils.isEmpty(newCookie);
    }
}
