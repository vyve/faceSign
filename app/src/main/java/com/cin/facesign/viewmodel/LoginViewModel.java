package com.cin.facesign.viewmodel;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.cin.facesign.Constant;
import com.cin.facesign.ui.MainActivity;
import com.cin.mylibrary.base.BaseViewModel;
import com.cin.mylibrary.bean.BaseResponseBean;
import com.cin.mylibrary.bean.UserBean;
import com.cin.mylibrary.http.FilterSubscriber;
import com.cin.mylibrary.http.RetrofitHelper;

/**
 * Created by 王新超 on 2020/6/12.
 */
public class LoginViewModel extends BaseViewModel {

    public ObservableField<String> username;
    public ObservableField<String> password;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        username = new ObservableField<>();
        password = new ObservableField<>();
    }

    /**
     * 登录
     */
    public void login(Context context) {
        if (TextUtils.isEmpty(username.get())) {
            ToastUtils.showShort("用户名不能为空！");
            return;
        }
        if (TextUtils.isEmpty(password.get())) {
            ToastUtils.showShort("密码不能为空！");
            return;
        }
        showLoadingDialog();
        RetrofitHelper.getInstance().login(username.get(),password.get(),new FilterSubscriber<BaseResponseBean<UserBean>>(context){

            @Override
            public void onNext(BaseResponseBean<UserBean> bean) {
                super.onNext(bean);
                //保存userId
                SPUtils.getInstance().put(Constant.userId,bean.getData().getId());
                SPUtils.getInstance().put(Constant.isLogin,true);
                SPUtils.getInstance().put(Constant.username,bean.getData().getName());
                MainActivity.startActivity(context);
                dismissLoadingDialog();
                showToast("登录成功");
                finish();
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                dismissLoadingDialog();
                showToast(error);
            }
        });
    }
}
