package com.cin.facesign.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.blankj.utilcode.util.SPUtils;
import com.cin.facesign.Constant;
import com.cin.mylibrary.base.BaseViewModel;

/**
 * Created by 王新超 on 2020/6/17.
 */
public class PersonalViewModel extends BaseViewModel {

    public ObservableField<String> name = new ObservableField<>();
    public ObservableField<String> userId = new ObservableField<>();

    public PersonalViewModel(@NonNull Application application) {
        super(application);
        name.set(SPUtils.getInstance().getString(Constant.username));
        userId.set("ID "+SPUtils.getInstance().getInt(Constant.userId));
    }
}
