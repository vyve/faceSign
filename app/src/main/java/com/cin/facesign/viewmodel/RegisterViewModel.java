package com.cin.facesign.viewmodel;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.cin.facesign.bean.PhoneCodeBean;
import com.cin.mylibrary.AppConstant;
import com.cin.mylibrary.request_bean.RegisterFaceBean;
import com.cin.mylibrary.base.BaseModel;
import com.cin.mylibrary.base.BaseViewModel;
import com.cin.mylibrary.bean.FaceSDKRegisterBean;
import com.cin.mylibrary.http.RetrofitHelper;


import rx.Subscriber;

/**
 * Created by 王新超 on 2020/6/12.
 */
public class RegisterViewModel extends BaseViewModel {
    public ObservableField<String> username = new ObservableField<>();
    /**
     * 0 男 default
     * 1 女
     */
    public ObservableField<Integer> sex = new ObservableField<>();
    public ObservableField<String> idCard = new ObservableField<>();
    public ObservableField<String> phone = new ObservableField<>();
    public MutableLiveData<BaseModel<PhoneCodeBean>> phoneCodeBean = new MutableLiveData<>();
    public ObservableField<String> password = new ObservableField<>();
    public ObservableField<String> passwordAgain = new ObservableField<>();
    public MutableLiveData<BaseModel<Object>> registerResult = new MutableLiveData<>();
    public ObservableField<String> bitmapPath = new ObservableField<>();

    public RegisterViewModel(@NonNull Application application) {
        super(application);
        sex.set(0);

    }

    /**
     * 获取手机验证码
     */
    public void getPhoneCode() {
        if (TextUtils.isEmpty(phone.get())) {
            showToast("手机号码不能为空！");
            return;
        }

    }

    /**
     * 注册
     */
    public void register(String base64Img) {

        RegisterFaceBean bean = new RegisterFaceBean();
        bean.setGroup_id(AppConstant.faceSDKGroupID);
        bean.setImage(base64Img);
        bean.setImage_type("BASE64");
        bean.setLiveness_control("NORMAL");
        bean.setQuality_control("NONE");
        bean.setUser_id("123");
        bean.setUser_info("王新超");

        final BaseModel<Object> baseModel = new BaseModel<>();

        RetrofitHelper.getInstance().registerFace(bean, new Subscriber<FaceSDKRegisterBean>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                baseModel.setSuccess(false);
                baseModel.setErrorMsg(e.getMessage());
                registerResult.postValue(baseModel);
            }

            @Override
            public void onNext(FaceSDKRegisterBean registerBean) {
                if (registerBean.getError_code()==0) {
                    baseModel.setSuccess(true);
                }else {
                    baseModel.setSuccess(false);
                    baseModel.setErrorMsg(registerBean.getError_msg());
                }
                registerResult.postValue(baseModel);
            }
        });

    }
}
