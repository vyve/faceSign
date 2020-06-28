package com.cin.mylibrary.http;

import com.cin.mylibrary.request_bean.RegisterFaceBean;
import com.cin.mylibrary.bean.FaceSDKRegisterBean;
import com.cin.mylibrary.bean.FaceSDKToken;

import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by 王新超 on 2020/3/9.
 */
public interface BDRetrofitService {
    @POST("oauth/2.0/token?grant_type=client_credentials")
    Observable<FaceSDKToken> getSDKToken(@Query("client_id") String clientId, @Query("client_secret") String clientSecret);

    @POST("rest/2.0/face/v3/faceset/user/add")
    Observable<FaceSDKRegisterBean> registerFace(@Query("access_token")String token, @Body RegisterFaceBean bean);
}
