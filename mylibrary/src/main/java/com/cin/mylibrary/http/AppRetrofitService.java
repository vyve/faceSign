package com.cin.mylibrary.http;

import com.cin.mylibrary.request_bean.BaseRequestBean;
import com.cin.mylibrary.bean.BaseResponseBean;

import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by 王新超 on 2020/6/28.
 */
public interface AppRetrofitService {

    @POST("user/login")
    Observable<BaseRequestBean> login(@Body BaseRequestBean requestBean);

    @POST("insurance/all")
    Observable<BaseResponseBean> getAllInsurance(@Body BaseRequestBean requestBean);

    @POST("insurance/hot")
    Observable<BaseResponseBean> getHotInsurance(@Body BaseRequestBean requestBean);

    @POST("insurance/queryById")
    Observable<BaseResponseBean> getInsuranceDetail(@Body BaseRequestBean requestBean, @Query("id")Integer id);
}
