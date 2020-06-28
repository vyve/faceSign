package com.cin.mylibrary.http;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by 王新超 on 2020/3/9.
 */
public class BaseRepository {
    protected Subscription goRequest(Observable<?> observable, Subscriber subscriber){
        return observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }
}
