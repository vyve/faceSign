package com.cin.facesign;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.LogUtils;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by 王新超 on 2020/8/21.
 */
public class TestActivity extends AppCompatActivity {
    private static final String TAG = "LogTestActivity";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
                emitter.onComplete();
            }
        }).subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
                LogUtils.i(TAG,"subscribe");
            }

            @Override
            public void onNext(Integer integer) {
                LogUtils.i(TAG,integer+"");
            }

            @Override
            public void onError(Throwable e) {
                LogUtils.i(TAG,"error");
            }

            @Override
            public void onComplete() {
                LogUtils.i(TAG,"complete");
            }
        });
    }

}

