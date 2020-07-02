package com.cin.facesign.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.cin.facesign.Constant;
import com.cin.facesign.R;
import com.cin.facesign.bean.eventbus.SignatureFinishEvent;
import com.cin.facesign.databinding.ActivitySignatureBinding;
import com.cin.facesign.viewmodel.SignatureViewModel;
import com.cin.mylibrary.base.BaseActivity;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

/**
 * 客户签名
 * Created by 王新超 on 2020/6/18.
 */
public class SignatureActivity extends BaseActivity<ActivitySignatureBinding, SignatureViewModel> {

    public static void startActivity(Context context){
        context.startActivity(new Intent(context,SignatureActivity.class));
    }

    @Override
    public int initContentView(@Nullable Bundle savedInstanceState) {
        return R.layout.activity_signature;
    }

    @Override
    public int initVariableId() {
        return 0;
    }

    @Override
    public void init() {
        binding.setPresenter(new Presenter());
    }

    public class Presenter{
        /**
         * 重置
         */
        public void reset(){
            binding.linePathView.clear();
        }

        /**
         * 确认
         */
        public void ok(){
            if (binding.linePathView.getTouched()){
                try {
                    binding.linePathView.save(Constant.SIGN_PATH);
                    finish();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                showToast("您还没有签名");
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        EventBus.getDefault().post(new SignatureFinishEvent());
    }
}
