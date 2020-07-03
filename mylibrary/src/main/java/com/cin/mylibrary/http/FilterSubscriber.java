package com.cin.mylibrary.http;

import android.content.Context;

import com.google.gson.JsonSyntaxException;


import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

import rx.Subscriber;


/**
 * 统一 错误码处理
 */
public class FilterSubscriber<T> extends Subscriber<T> {
    protected Context context;
    public String error;

    public FilterSubscriber(Context context) {
        this.context = context;
    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {
        if (e instanceof TimeoutException || e instanceof SocketTimeoutException
                || e instanceof ConnectException) {
            error = "连接超时";
        } else if (e instanceof JsonSyntaxException) {
            error = "Json格式出错了";
        } else if (e instanceof ApiException) {
            ApiException exception = (ApiException) e;
            error = exception.message;
            if (exception.message.equals("401")) {
                error = "登录过期";
            }
        }else {
            error = e.getMessage();
        }
    }

    @Override
    public void onNext(T t) {

    }

}