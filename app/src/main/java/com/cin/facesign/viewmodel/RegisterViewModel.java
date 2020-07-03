package com.cin.facesign.viewmodel;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.blankj.utilcode.util.SPUtils;
import com.cin.facesign.Constant;
import com.cin.facesign.bean.eventbus.RegisterFinishEvent;
import com.cin.facesign.ui.MainActivity;
import com.cin.facesign.utils.FileUtils;
import com.cin.mylibrary.AppConstant;
import com.cin.mylibrary.bean.BaseResponseBean;
import com.cin.mylibrary.bean.UserBean;
import com.cin.mylibrary.http.FilterSubscriber;
import com.cin.mylibrary.request_bean.RegisterFaceBean;
import com.cin.mylibrary.base.BaseModel;
import com.cin.mylibrary.base.BaseViewModel;
import com.cin.mylibrary.bean.FaceSDKRegisterBean;
import com.cin.mylibrary.http.RetrofitHelper;
import com.cin.mylibrary.request_bean.RegisterRequestBean;


import org.greenrobot.eventbus.EventBus;

import java.io.File;

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
    public ObservableField<String> sex = new ObservableField<>();
    public ObservableField<String> idCard = new ObservableField<>();
    public ObservableField<String> phone = new ObservableField<>();
    public ObservableField<String> phoneCode = new ObservableField<>();
    public ObservableField<String> password = new ObservableField<>();
    public ObservableField<String> passwordAgain = new ObservableField<>();
    public ObservableField<String> bitmapPath = new ObservableField<>();

    private Context context;

    public RegisterViewModel(@NonNull Application application) {
        super(application);
        sex.set("男");
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
    public void register(Context context) {

        this.context = context;

        if (TextUtils.isEmpty(username.get())){
            showToast("姓名不能为空");
            return;
        }

        if (TextUtils.isEmpty(idCard.get())){
            showToast("身份证不能为空");
            return;
        }

        if (!idCard.get().matches("^[1-9]\\d{5}[1-9]\\d{3}((0[1-9])|(1[0-2]))(0[1-9]|([1|2][0-9])|3[0-1])((\\d{4})|\\d{3}X)$")){
            showToast("身份证格式不正确");
            return;
        }

        if (TextUtils.isEmpty(phone.get())) {
            showToast("手机号码不能为空！");
            return;
        }

        if (TextUtils.isEmpty(phoneCode.get())){
            showToast("验证码不能为空");
            return;
        }

        if (TextUtils.isEmpty(password.get())||TextUtils.isEmpty(passwordAgain.get())){
            showToast("密码不能为空");
            return;
        }
        if (!password.get().equals(passwordAgain.get())){
            showToast("两次密码输入不一致");
            return;
        }

        if (!password.get().matches("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,20}$")){
            showToast("密码格式不正确");
            return;
        }

        if (TextUtils.isEmpty(bitmapPath.get())) {
            showToast("请先进行人脸检测");
            return;
        }


        final File file = new File(Constant.FACE_IDENTIFY_LOCAL_PATH);
        if (!file.exists()) {
            showToast("文件不存在");
            return;
        }
        String base64Img = "";
        try {
            byte[] buf = FileUtils.readFile(file);

            base64Img = new String(Base64.encode(buf, Base64.NO_WRAP));

        } catch (Exception e) {
            e.printStackTrace();
        }

        showLoadingDialog();

        RegisterFaceBean bean = new RegisterFaceBean();
        bean.setGroup_id(AppConstant.faceSDKGroupID);
        bean.setImage(base64Img);
        bean.setImage_type("BASE64");
        bean.setLiveness_control("NORMAL");
        bean.setQuality_control("NONE");
        bean.setUser_id("123");
        bean.setUser_info("王新超");

        RetrofitHelper.getInstance().registerFace(bean, new Subscriber<FaceSDKRegisterBean>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                dismissLoadingDialog();
                showToast(e.getMessage());
            }

            @Override
            public void onNext(FaceSDKRegisterBean registerBean) {
                if (registerBean.getError_code()!=0) {
                    dismissLoadingDialog();
                    showToast(registerBean.getError_msg());
                }else {
                    registerApp(registerBean.getResult().getFace_token());
                }

            }
        });
    }

    private void registerApp(String imageToken){
        RegisterRequestBean bean = new RegisterRequestBean();
        bean.setName(username.get());
        bean.setUserName(username.get());
        bean.setSex(sex.get());
        bean.setIdentityCard(idCard.get());
        bean.setPhone(phone.get());
        bean.setPassword(password.get());
        bean.setImage(imageToken);
        RetrofitHelper.getInstance().register(bean, new FilterSubscriber<BaseResponseBean<UserBean>>(context){
            @Override
            public void onNext(BaseResponseBean<UserBean> baseResponseBean) {
                super.onNext(baseResponseBean);
                //保存userId
                SPUtils.getInstance().put(Constant.userId,baseResponseBean.getData().getId());
                SPUtils.getInstance().put(Constant.isLogin,true);
                SPUtils.getInstance().put(Constant.username,baseResponseBean.getData().getName());
                dismissLoadingDialog();
                showToast("注册成功");
                MainActivity.startActivity(context);
                //关闭登录页面
                EventBus.getDefault().post(new RegisterFinishEvent());
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
