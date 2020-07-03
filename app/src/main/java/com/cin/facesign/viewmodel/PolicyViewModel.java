package com.cin.facesign.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.blankj.utilcode.util.SPUtils;
import com.cin.facesign.Constant;
import com.cin.facesign.bean.InsurancePolicyBean;
import com.cin.mylibrary.base.BaseViewModel;
import com.cin.mylibrary.bean.BaseResponseBean;
import com.cin.mylibrary.http.FilterSubscriber;
import com.cin.mylibrary.http.RetrofitHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 王新超 on 2020/6/17.
 */
public class PolicyViewModel extends BaseViewModel {
    public ObservableField<List<InsurancePolicyBean>> data = new ObservableField<>();
    public PolicyViewModel(@NonNull Application application) {
        super(application);
    }

    public void getTodoData() {
        List<InsurancePolicyBean> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            InsurancePolicyBean bean = new InsurancePolicyBean();
            list.add(bean);
        }
        data.set(list);
    }

    /**
     * 获取列表
     */
    public void getData(){
        int userId = SPUtils.getInstance().getInt(Constant.userId);
        RetrofitHelper.getInstance().getUserInsurances(userId,new FilterSubscriber<BaseResponseBean>(getApplication()){
            @Override
            public void onNext(BaseResponseBean baseResponseBean) {
                super.onNext(baseResponseBean);

            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
            }
        });
    }
}
