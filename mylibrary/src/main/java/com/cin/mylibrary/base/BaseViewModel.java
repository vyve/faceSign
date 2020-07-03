package com.cin.mylibrary.base;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.blankj.utilcode.util.ToastUtils;
import com.cin.mylibrary.widget.bus.event.SingleLiveEvent;


/**
 * Created by 王新超 on 2020/3/9.
 */
public class BaseViewModel extends AndroidViewModel implements IBaseViewModel {

    public SingleLiveEvent<Void> showDialogEvent = new SingleLiveEvent<>();
    public SingleLiveEvent<Void> dismissDialogEvent = new SingleLiveEvent<>();
    public SingleLiveEvent<Void> finishEvent = new SingleLiveEvent<>();

    public BaseViewModel(@NonNull Application application) {
        super(application);
    }

    protected void showToast(String msg){
        ToastUtils.showShort(msg);
    }

    protected void showLoadingDialog(){
        showDialogEvent.call();
    }

    protected void dismissLoadingDialog(){
        dismissDialogEvent.call();
    }

    public void finish() {
        finishEvent.call();
    }

    @Override
    public void onAny(LifecycleOwner owner, Lifecycle.Event event) {

    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void registerRxBus() {

    }

    @Override
    public void removeRxBus() {

    }
}
