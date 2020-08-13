package com.cin.mylibrary.http;

import com.blankj.utilcode.util.SPUtils;
import com.cin.mylibrary.AppConstant;
import com.cin.mylibrary.bean.CheckFaceInfoResultBean;
import com.cin.mylibrary.bean.InsuranceBean;
import com.cin.mylibrary.bean.OssConfigBean;
import com.cin.mylibrary.bean.UserBean;
import com.cin.mylibrary.request_bean.BaseRequestBean;
import com.cin.mylibrary.request_bean.CheckFaceInfoRequestBean;
import com.cin.mylibrary.request_bean.CompleteOnlineIdentifyRequestBean;
import com.cin.mylibrary.request_bean.LoginRequestBean;
import com.cin.mylibrary.request_bean.RegisterFaceBean;
import com.cin.mylibrary.bean.BaseResponseBean;
import com.cin.mylibrary.bean.FaceSDKRegisterBean;
import com.cin.mylibrary.bean.FaceSDKToken;
import com.cin.mylibrary.request_bean.RegisterRequestBean;

import java.util.List;

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
                .subscribe(subscriber);

    }

    /**
     * 获取热门保险
     */
    public void getHotInsurance(BaseRequestBean requestBean,Subscriber<BaseResponseBean<List<InsuranceBean>>> subscriber){
        RetrofitUtil.getInstance().getAppService().getHotInsurance(requestBean)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 获取保险详情
     */
    public void getInsuranceDetail(int id,BaseRequestBean requestBean,Subscriber<BaseResponseBean> subscriber){
        RetrofitUtil.getInstance().getAppService().getInsuranceDetail(requestBean,id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 登录
     */
    public void login(String username,String password,Subscriber<BaseResponseBean<UserBean>> subscriber){
        LoginRequestBean bean = new LoginRequestBean();
        bean.setUserName(username);
        bean.setPassword(password);
        RetrofitUtil.getInstance().getAppService().login(bean)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 注册
     */
    public void register(RegisterRequestBean bean,Subscriber<BaseResponseBean<UserBean>> subscriber){
        RetrofitUtil.getInstance().getAppService().register(bean)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 获取用户所有保险
     */
    public void getUserInsurances(Integer userId,Subscriber<BaseResponseBean> subscriber){
        RetrofitUtil.getInstance().getAppService().getUserInsurances(new BaseRequestBean(),userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 提交在线认证数据
     */
    public void completeOnlineIdentify(Integer userId,Integer insuranceId,CompleteOnlineIdentifyRequestBean bean,Subscriber<BaseResponseBean> subscriber){
        RetrofitUtil.getInstance().getAppService().completeOnlineIdentify(bean,userId,insuranceId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }
    /**
     * 人脸信息对比
     */
    public void checkFaceInfo(String faceImgUrl,String idCardImgUrl, Subscriber<BaseResponseBean<CheckFaceInfoResultBean>> subscriber){
        CheckFaceInfoRequestBean bean = new CheckFaceInfoRequestBean();
        bean.setIdCardImage(idCardImgUrl);
        bean.setImage(faceImgUrl);
        RetrofitUtil.getInstance().getAppService().checkFaceInfo(bean)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }
    /**
     * OSS Config 信息
     */
    public void getOSSConfig(Subscriber<BaseResponseBean<OssConfigBean>> subscriber){
        RetrofitUtil.getInstance().getAppService().getOSSConfig()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

}
