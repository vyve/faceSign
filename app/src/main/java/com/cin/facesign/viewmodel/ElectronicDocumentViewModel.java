package com.cin.facesign.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.cin.mylibrary.base.BaseViewModel;

/**
 * Created by 王新超 on 2020/7/1.
 */
public class ElectronicDocumentViewModel extends BaseViewModel {
    public ObservableField<String> remainTime = new ObservableField<>();
    public ObservableField<Boolean> signButtonEnable = new ObservableField<>();
    private Handler mHandler;
    private int currentTime = 10;
    @SuppressLint("HandlerLeak")
    public ElectronicDocumentViewModel(@NonNull Application application) {
        super(application);
        mHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                currentTime--;
                remainTime.set("剩余阅读时间【"+currentTime+"秒】");
                if (currentTime==0){
                    signButtonEnable.set(true);
                }else {
                    mHandler.sendEmptyMessageDelayed(1,1000);
                }
            }
        };
        start();
        remainTime.set("剩余阅读时间【"+currentTime+"秒】");
        signButtonEnable.set(false);
    }

    private void start(){
        mHandler.sendEmptyMessageDelayed(1,1000);
    }

}
