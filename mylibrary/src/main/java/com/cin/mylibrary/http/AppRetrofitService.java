package com.cin.mylibrary.http;

import com.cin.mylibrary.bean.CheckFaceInfoResultBean;
import com.cin.mylibrary.bean.InsuranceBean;
import com.cin.mylibrary.bean.OssConfigBean;
import com.cin.mylibrary.bean.UserBean;
import com.cin.mylibrary.request_bean.BaseRequestBean;
import com.cin.mylibrary.bean.BaseResponseBean;
import com.cin.mylibrary.request_bean.CheckFaceInfoRequestBean;
import com.cin.mylibrary.request_bean.CompleteOnlineIdentifyRequestBean;
import com.cin.mylibrary.request_bean.LoginRequestBean;
import com.cin.mylibrary.request_bean.RegisterRequestBean;

import java.util.List;

import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by 王新超 on 2020/6/28.
 */
public interface AppRetrofitService {

    @POST("user/login")
    Observable<BaseResponseBean<UserBean>> login(@Body LoginRequestBean bean);

    @POST("user/signup")
    Observable<BaseResponseBean<UserBean>> register(@Body RegisterRequestBean bean);

    @POST("insurance/all")
    Observable<BaseResponseBean> getAllInsurance(@Body BaseRequestBean requestBean);

    @POST("insurance/hot")
    Observable<BaseResponseBean<List<InsuranceBean>>> getHotInsurance(@Body BaseRequestBean requestBean);

    @POST("insurance/queryById")
    Observable<BaseResponseBean> getInsuranceDetail(@Body BaseRequestBean requestBean, @Query("id")Integer id);

    @POST("user/insurances")
    Observable<BaseResponseBean> getUserInsurances(@Body BaseRequestBean bean,@Query("userId")Integer userId);

    @POST("user/newUserInsurance")
    Observable<BaseResponseBean> completeOnlineIdentify(@Body CompleteOnlineIdentifyRequestBean bean,
                                                        @Query("userId")Integer userId,@Query("insuranceId") Integer insuranceId);

    @POST("face/check")
    Observable<BaseResponseBean<CheckFaceInfoResultBean>> checkFaceInfo(@Body CheckFaceInfoRequestBean bean);

    @POST("oss/token")
    Observable<BaseResponseBean<OssConfigBean>> getOSSConfig();
}
