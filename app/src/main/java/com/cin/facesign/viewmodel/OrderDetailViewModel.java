package com.cin.facesign.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.cin.facesign.bean.InsurancePolicyBean;
import com.cin.mylibrary.base.BaseViewModel;

/**
 * Created by 王新超 on 2020/6/17.
 */
public class OrderDetailViewModel extends BaseViewModel {

    public ObservableField<InsurancePolicyBean> detail = new ObservableField<>();

    public OrderDetailViewModel(@NonNull Application application) {
        super(application);
    }
}
