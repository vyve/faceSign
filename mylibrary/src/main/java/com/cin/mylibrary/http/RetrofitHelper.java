package com.cin.mylibrary.http;

import com.blankj.utilcode.util.SPUtils;
import com.cin.mylibrary.AppConstant;
import com.cin.mylibrary.request_bean.BaseRequestBean;
import com.cin.mylibrary.request_bean.LoginRequestBean;
import com.cin.mylibrary.request_bean.RegisterFaceBean;
import com.cin.mylibrary.bean.BaseResponseBean;
import com.cin.mylibrary.bean.FaceSDKRegisterBean;
import com.cin.mylibrary.bean.FaceSDKToken;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by 王新超 on 2020/6/15.
 */
public class RetrofitHelper {
    private static RetrofitHelper instance;

    public static RetrofitHelper getInstance() {
        if (instance == null) {
            synchronized (RetrofitHelper.class) {
                if (instance == null) {
                    instance = new RetrofitHelper();
                }
            }
        }
        return instance;
    }

    /**
     * 获取FaceSDK Token
     */
    public void getSDKToken(Subscriber<FaceSDKToken> subscriber) {
        RetrofitUtil.getInstance().getFaceSDKService().getSDKToken(AppConstant.faceSDKApiKey,AppConstant.faceSDKSecretKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 注册FaceSDK
     */
    public void registerFace(RegisterFaceBean bean, Subscriber<FaceSDKRegisterBean> subscriber){
        RetrofitUtil.getInstance().getFaceSDKService().registerFace(SPUtils.getInstance().getString(AppConstant.SP_FACE_TOKEN),bean)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 获取所有保险
     */
    public void getAllInsurance(BaseRequestBean requestBean,Subscriber<BaseResponseBean> subscriber){
        RetrofitUtil.getInstance().getAppService().getAllInsurance(requestBean)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();

    }

    /**
     * 获取热门保险
     */
    public void getHotInsurance(BaseRequestBean requestBean,Subscriber<BaseResponseBean> subscriber){
        RetrofitUtil.getInstance().getAppService().getHotInsurance(requestBean)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    /**
     * 获取保险详情
     */
    public void getInsuranceDetail(int id,BaseRequestBean requestBean,Subscriber<BaseResponseBean> subscriber){
        RetrofitUtil.getInstance().getAppService().getInsuranceDetail(requestBean,id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public void login(String username,String password,Subscriber<BaseResponseBean> subscriber){
        LoginRequestBean bean = new LoginRequestBean();
        bean.setUserName(username);
        bean.setPassword(password);
        RetrofitUtil.getInstance().getAppService().login(bean)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }
}
