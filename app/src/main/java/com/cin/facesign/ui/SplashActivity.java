package com.cin.facesign.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.SPUtils;
import com.cin.facesign.Constant;
import com.cin.facesign.R;
import com.cin.facesign.databinding.ActivitySplashBinding;
import com.cin.facesign.viewmodel.SplashViewModel;
import com.cin.mylibrary.base.BaseActivity;

/**
 * Created by 王新超 on 2020/7/1.
 */
public class SplashActivity extends BaseActivity<ActivitySplashBinding, SplashViewModel> {
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            boolean isLogin = SPUtils.getInstance().getBoolean(Constant.isLogin, false);
            if (isLogin) {
                MainActivity.startActivity(SplashActivity.this);
            } else {
                LoginActivity.startActivity(SplashActivity.this);
            }
            finish();
        }
    };

    @Override
    public int initContentView(@Nullable Bundle savedInstanceState) {
        return R.layout.activity_splash;
    }

    @Override
    public int initVariableId() {
        return 0;
    }

    @Override
    public void init() {
        mHandler.sendEmptyMessageDelayed(1, 1000);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
