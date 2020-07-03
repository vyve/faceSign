package com.cin.facesign.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.BR;
import com.cin.facesign.Constant;
import com.cin.facesign.R;
import com.cin.facesign.databinding.ActivityRegisterBinding;
import com.cin.facesign.viewmodel.RegisterViewModel;
import com.cin.mylibrary.base.BaseActivity;
import com.cin.mylibrary.base.BaseModel;


/**
 * 注册
 * Created by 王新超 on 2020/6/12.
 */
public class RegisterActivity extends BaseActivity<ActivityRegisterBinding, RegisterViewModel> {

    private static final int REQUEST_CODE_DETECT_FACE = 1000;

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, RegisterActivity.class));
    }

    @Override
    public int initContentView(@Nullable Bundle savedInstanceState) {
        return R.layout.activity_register;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void init() {
        binding.setPresenter(new Presenter());
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();

        binding.sexRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId){
                case R.id.sex_man:
                    viewModel.sex.set("男");
                    break;
                case R.id.sex_woman:
                    viewModel.sex.set("女");
                    break;
            }
        });

    }

    public class Presenter {
        public void onGetCodeClick() {
            viewModel.getPhoneCode();
        }

        /**
         * 开始注册
         */
        public void onRegisterClick() {
            viewModel.register(RegisterActivity.this);
        }

        /**
         * 面部识别
         */
        public void startFaceIdentify() {
            FaceIdentifyActivity.startActivityForResult(RegisterActivity.this, REQUEST_CODE_DETECT_FACE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //人脸识别返回
        if (requestCode == REQUEST_CODE_DETECT_FACE && resultCode == Activity.RESULT_OK) {
            viewModel.bitmapPath.set(Constant.FACE_IDENTIFY_LOCAL_PATH);
            Glide.with(this).load(Constant.FACE_IDENTIFY_LOCAL_PATH)
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true))
                    .into(binding.registerFaceImg);

        }
    }
}
