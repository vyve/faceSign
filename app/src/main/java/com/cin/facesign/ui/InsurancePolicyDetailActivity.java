package com.cin.facesign.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.chad.library.BR;
import com.cin.facesign.R;
import com.cin.facesign.databinding.ActivityInsurancePolicyDetailBindingImpl;
import com.cin.facesign.viewmodel.OrderDetailViewModel;
import com.cin.mylibrary.base.BaseActivity;

/**
 * 保单详情
 * Created by 王新超 on 2020/6/17.
 */
public class InsurancePolicyDetailActivity extends BaseActivity<ActivityInsurancePolicyDetailBindingImpl, OrderDetailViewModel> {

    public static void startActivity(Context context){
        context.startActivity(new Intent(context, InsurancePolicyDetailActivity.class));
    }

    @Override
    public int initContentView(@Nullable Bundle savedInstanceState) {
        return R.layout.activity_insurance_policy_detail;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void init() {

    }
}
