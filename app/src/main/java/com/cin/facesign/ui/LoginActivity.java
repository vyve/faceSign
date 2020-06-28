package com.cin.facesign.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.blankj.utilcode.util.ToastUtils;
import com.cin.facesign.BR;
import com.cin.facesign.R;
import com.cin.facesign.bean.LoginBean;
import com.cin.facesign.databinding.ActivityLoginBinding;
import com.cin.facesign.viewmodel.LoginViewModel;
import com.cin.mylibrary.base.BaseActivity;
import com.cin.mylibrary.base.BaseModel;

/**
 * 登录
 * Created by 王新超 on 2020/6/12.
 */
public class LoginActivity extends BaseActivity<ActivityLoginBinding, LoginViewModel> {

    public static void startActivity(Context context){
        context.startActivity(new Intent(context,LoginActivity.class));
    }

    @Override
    public int initContentView(@Nullable Bundle savedInstanceState) {
        return R.layout.activity_login;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void init() {
        binding.setPresenter(new Presenter());
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        viewModel.loginResult.observe(this, new Observer<BaseModel>() {
            @Override
            public void onChanged(BaseModel baseModel) {
                if (baseModel.isSuccess()){

                }else {
                    showToast(baseModel.getErrorMsg());
                }
            }
        });
    }

    public class Presenter{
        /**
         * 登录
         */
        public void onLoginClick(){
            viewModel.login();
        }

        /**
         * 跳转注册
         */
        public void onRegisterClick(){
            RegisterActivity.startActivity(LoginActivity.this);
        }

        /**
         * 跳转面容登录
         */
        public void onFaceLoginClick(){
            FaceLoginActivity.startActivity(LoginActivity.this);
        }
    }

}
