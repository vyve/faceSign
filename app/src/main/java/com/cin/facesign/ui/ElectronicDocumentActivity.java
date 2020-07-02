package com.cin.facesign.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.chad.library.BR;
import com.cin.facesign.R;
import com.cin.facesign.databinding.ActivityElectronicDocumentBinding;
import com.cin.facesign.viewmodel.ElectronicDocumentViewModel;
import com.cin.mylibrary.base.BaseActivity;

/**
 * 电子文档
 * Created by 王新超 on 2020/7/1.
 */
public class ElectronicDocumentActivity extends BaseActivity<ActivityElectronicDocumentBinding, ElectronicDocumentViewModel> {

    public static void startActivity(Context context){
        Intent intent = new Intent(context, ElectronicDocumentActivity.class);
        context.startActivity(intent);
    }

    @Override
    public int initContentView(@Nullable Bundle savedInstanceState) {
        return R.layout.activity_electronic_document;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void init() {
        binding.setPresenter(new Presenter());
    }

    public class Presenter{
        public void onSignClick(){
            SignatureActivity.startActivity(ElectronicDocumentActivity.this);
            finish();
        }
    }
}
