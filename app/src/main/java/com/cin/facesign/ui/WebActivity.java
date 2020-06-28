package com.cin.facesign.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.DecelerateInterpolator;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.NetworkUtils;
import com.chad.library.BR;
import com.cin.facesign.R;
import com.cin.facesign.databinding.ActivityWebBinding;
import com.cin.facesign.viewmodel.WebViewModel;
import com.cin.mylibrary.base.BaseActivity;

/**
 * Created by 王新超 on 2020/6/18.
 */
public class WebActivity extends BaseActivity<ActivityWebBinding, WebViewModel> {

    /**
     * 网络是否异常
     */
    private boolean isNetWorkError;

    /**
     * webView加载动画是否开始
     */
    private boolean isAnimStart = false;

    public static void startActivity(Context context, String url) {
        Intent intent = new Intent(context, WebActivity.class);
        intent.putExtra("url", url);
        context.startActivity(intent);
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (!isNetWorkError) {
                        viewModel.showWebView.set(true);
                        viewModel.showError.set(false);
                    } else {
                        viewModel.showWebView.set(false);
                        viewModel.showError.set(true);
                    }
                    break;
                case 1:
                    //webView加载完成不能立即显示，否则会有闪烁现象，延时500以后在显示
                    mHandler.sendEmptyMessageDelayed(0, 500);
                    break;
            }
        }
    };

    @Override
    public int initContentView(@Nullable Bundle savedInstanceState) {
        return R.layout.activity_web;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void init() {

        mImmersionBar.statusBarDarkFont(false).init();

        binding.setPresenter(new Presenter());

        String url = getIntent().getStringExtra("url");
        initWebView(binding.webWebView, url, binding.webLoadingProgress);
        binding.webWebView.loadUrl(url);
    }

    public class Presenter {
        public void onReload() {
            binding.webWebView.reload();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView(WebView webView, String url, ProgressBar progressBar) {

        CookieManager cookieManager = CookieManager.getInstance();
        String cookie = cookieManager.getCookie(url);
        viewModel.syncCookie(url, cookie);

        webView.setHorizontalScrollBarEnabled(false);//水平不显示
        webView.setVerticalScrollBarEnabled(false); //垂直不显示
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setSupportZoom(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.supportMultipleWindows();  //多窗口
        webSettings.setAppCacheEnabled(true);
        webSettings.setSaveFormData(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webView, true);
        }
        cookieManager.setAcceptCookie(true);
        //设置缓存
        if (!NetworkUtils.isConnected()) {
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        } else {
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        }
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                int mCurrentProgress = progressBar.getProgress();
                if (newProgress >= 100 && !isAnimStart) {
                    isAnimStart = true;
                    progressBar.setProgress(newProgress);
                    startDismissAnimation(progressBar.getProgress());
                } else {
                    // 开启属性动画让进度条平滑递增
                    startProgressAnimation(mCurrentProgress, newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }

            @Override
            public void onReceivedTitle(WebView webView, String title) {
                super.onReceivedTitle(webView, title);
                viewModel.title.set(title);
                // android 6.0 以下通过title获取
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    if (title.contains("404") || title.contains("500") || title.contains("Error")) {
                        isNetWorkError = true;
                        viewModel.showError.set(true);
                        viewModel.showWebView.set(false);
                    }
                }
            }


        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("http:") || url.startsWith("https:")) {
                    return false;
                }
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                isNetWorkError = false;
                viewModel.showProgressBar.set(true);
                progressBar.setAlpha(1.0f);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (!webView.getSettings().getLoadsImagesAutomatically()) {
                    webView.getSettings().setLoadsImagesAutomatically(true);
                }

                viewModel.getCookie(url);

                super.onPageFinished(view, url);

                mHandler.sendEmptyMessage(1);
            }

            @Override
            public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
                sslErrorHandler.proceed();
                super.onReceivedSslError(webView, sslErrorHandler, sslError);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                // 断网或者网络连接超时
                if (description.equals("net::ERR_INTERNET_DISCONNECTED")) {
                    isNetWorkError = true;
                    viewModel.showError.set(true);
                    viewModel.showWebView.set(false);
                }
            }

        });
    }

    /**
     * 显示进度条动画
     *
     * @param current     当前进度
     * @param newProgress 新的进度
     */
    private void startProgressAnimation(int current, int newProgress) {
        ObjectAnimator anim = ObjectAnimator.ofInt(binding.webLoadingProgress, "progress", current, newProgress);
        anim.setDuration(300);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.start();

    }

    /**
     * 进度条消失动画
     */
    private void startDismissAnimation(final int progress) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(binding.webLoadingProgress, "alpha", 1.0f, 0.0f);
        animator.setDuration(1500);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();
            int offset = 100 - progress;
            binding.webLoadingProgress.setProgress((int) (progress + offset * fraction));
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                binding.webLoadingProgress.setProgress(0);
                viewModel.showProgressBar.set(false);
                isAnimStart = false;
                super.onAnimationEnd(animation);
            }
        });
        animator.start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && binding.webWebView.canGoBack()) {
            binding.webWebView.goBack();// 返回前一个页面
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {

        // 如果先调用destroy()方法，则会命中if (isDestroyed()) return;这一行代码，需要先onDetachedFromWindow()，再
        ViewParent parent = binding.webWebView.getParent();
        if (parent != null) {
            ((ViewGroup) parent).removeView(binding.webWebView);
        }

        binding.webWebView.stopLoading();
        // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
        binding.webWebView.getSettings().setJavaScriptEnabled(false);
        binding.webWebView.clearHistory();
        binding.webWebView.removeAllViews();
        binding.webWebView.destroy();
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

}
