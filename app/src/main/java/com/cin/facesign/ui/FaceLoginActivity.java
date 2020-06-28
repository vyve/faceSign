package com.cin.facesign.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.cin.facesign.BR;
import com.cin.facesign.R;
import com.cin.facesign.databinding.ActivityFaceLoginBinding;
import com.cin.facesign.viewmodel.LoginViewModel;
import com.cin.mylibrary.base.BaseActivity;

/**
 * 面容登录
 * Created by 王新超 on 2020/6/12.
 */
public class FaceLoginActivity extends BaseActivity<ActivityFaceLoginBinding, LoginViewModel> {
    public static void startActivity(Context context){
        context.startActivity(new Intent(context,FaceLoginActivity.class));
    }
    @Override
    public int initContentView(@Nullable Bundle savedInstanceState) {
        return R.layout.activity_face_login;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void init() {
        binding.setPresenter(new Presenter());
    }

    public class Presenter{
        /**
         * 面容识别
         */
        public void onFaceIdentifyClick(){

        }
        /**
         * 跳转注册
         */
        public void onRegisterClick(){
            RegisterActivity.startActivity(FaceLoginActivity.this);
        }

        /**
         * 跳转账号密码登录
         */
        public void onAccountLoginClick(){
            LoginActivity.startActivity(FaceLoginActivity.this);
        }
    }
}
