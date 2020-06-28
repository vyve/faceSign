package com.cin.facesign.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.BR;
import com.cin.facesign.Constant;
import com.cin.facesign.R;
import com.cin.facesign.bean.PhoneCodeBean;
import com.cin.facesign.databinding.ActivityRegisterBinding;
import com.cin.facesign.utils.FileUtils;
import com.cin.facesign.viewmodel.RegisterViewModel;
import com.cin.mylibrary.base.BaseActivity;
import com.cin.mylibrary.base.BaseModel;

import java.io.File;

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
        viewModel.phoneCodeBean.observe(this, new Observer<BaseModel<PhoneCodeBean>>() {
            @Override
            public void onChanged(BaseModel<PhoneCodeBean> phoneCodeBeanBaseModel) {
                if (phoneCodeBeanBaseModel.isSuccess()) {
                    ToastUtils.showShort("获取成功");
                } else {
                    ToastUtils.showShort(phoneCodeBeanBaseModel.getErrorMsg());
                }
            }
        });

        viewModel.registerResult.observe(this, new Observer<BaseModel<Object>>() {
            @Override
            public void onChanged(BaseModel<Object> baseModel) {
                dismissLoadingDialog();
                if (baseModel.isSuccess()) {
                    //注册成功
                    showToast("注册成功");
                    MainActivity.startActivity(RegisterActivity.this);
                } else {
                    showToast(baseModel.getErrorMsg());
                }
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
            if (TextUtils.isEmpty(viewModel.bitmapPath.get())) {
                showToast("请先进行人脸检测");
                return;
            }

            final File file = new File(Constant.FACE_IDENTIFY_LOCAL_PATH);
            if (!file.exists()) {
                showToast("文件不存在");
                return;
            }
            String base64Img = "";
            try {
                byte[] buf = FileUtils.readFile(file);

                base64Img = new String(Base64.encode(buf, Base64.NO_WRAP));

            } catch (Exception e) {
                e.printStackTrace();
            }

            showLoadingDialog();
            viewModel.register(base64Img);
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
