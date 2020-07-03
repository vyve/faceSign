package com.cin.mylibrary.http;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;

/**
 * Created by 王新超 on 2018/11/6.
 */
public class RetrofitUtil {

    private static Retrofit bdRetrofit;
    private static Retrofit sRetrofit;
    private static RetrofitUtil instance;


    private static Retrofit getRetrofit() {
        if (sRetrofit == null) {
            synchronized (RetrofitUtil.class) {
                if (sRetrofit == null) {
                    OkHttpClient.Builder clientBuilder = new OkHttpClient().newBuilder();

                    clientBuilder
                            .connectTimeout(600, TimeUnit.SECONDS)
                            .writeTimeout(600, TimeUnit.SECONDS)
                            .readTimeout(600, TimeUnit.SECONDS)
                            .addInterceptor(httpLoggingInterceptor);
                    sRetrofit = new Retrofit.Builder()
                            .client(clientBuilder.build())
                            .baseUrl("https://www.matrixsci.cn/record/")
                            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                            .addConverterFactory(GsonFactory.create())
                            .build();
                }
            }
        }
        return sRetrofit;
    }

    private static Retrofit getFaceSDKRetrofit() {
        if (bdRetrofit == null) {
            synchronized (RetrofitUtil.class) {
                if (bdRetrofit == null) {
                    OkHttpClient.Builder clientBuilder = new OkHttpClient().newBuilder();
                    clientBuilder.connectTimeout(600, TimeUnit.SECONDS)
                            .addInterceptor(httpLoggingInterceptor)
                            .writeTimeout(600, TimeUnit.SECONDS)
                            .readTimeout(600, TimeUnit.SECONDS);
                    bdRetrofit = new Retrofit.Builder()
                            .client(clientBuilder.build())
                            .baseUrl("https://aip.baidubce.com")
                            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                            .addConverterFactory(BDGsonFactory.create())
                            .build();
                }
            }
        }
        return bdRetrofit;
    }


    public static RetrofitUtil getInstance() {
        if (instance == null) {
            synchronized (RetrofitUtil.class) {
                if (instance == null) {
                    instance = new RetrofitUtil();
                }
            }
        }
        return instance;
    }

    public AppRetrofitService getAppService() {
        return getRetrofit().create(AppRetrofitService.class);
    }

    public BDRetrofitService getFaceSDKService() {
        return getFaceSDKRetrofit().create(BDRetrofitService.class);
    }

    private static HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY);
}
