package com.cin.facesign.viewmodel;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.blankj.utilcode.util.ToastUtils;
import com.cin.mylibrary.base.BaseModel;
import com.cin.mylibrary.base.BaseViewModel;
import com.cin.mylibrary.bean.BaseResponseBean;
import com.cin.mylibrary.http.FilterSubscriber;
import com.cin.mylibrary.http.RetrofitHelper;

/**
 * Created by 王新超 on 2020/6/12.
 */
public class LoginViewModel extends BaseViewModel {

    public ObservableField<String> username;
    public ObservableField<String> password;

    public MutableLiveData<BaseModel> loginResult;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        username = new ObservableField<>();
        password = new ObservableField<>();
        loginResult = new MutableLiveData<>();
    }

    /**
     * 登录
     */
    public void login() {
        if (TextUtils.isEmpty(username.get())) {
            ToastUtils.showShort("用户名不能为空！");
            return;
        }
        if (TextUtils.isEmpty(password.get())) {
            ToastUtils.showShort("密码不能为空！");
            return;
        }
        BaseModel model = new BaseModel<>();
        RetrofitHelper.getInstance().login(username.get(),password.get(),new FilterSubscriber<BaseResponseBean>(getApplication()){
            @Override
            public void onNext(BaseResponseBean bean) {
                super.onNext(bean);
                model.setSuccess(true);
                loginResult.postValue(model);
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                model.setSuccess(false);
                model.setErrorMsg(e.getMessage());
                loginResult.postValue(model);
            }
        });
    }
}
