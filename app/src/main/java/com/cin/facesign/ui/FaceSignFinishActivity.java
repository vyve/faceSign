package com.cin.facesign.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.chad.library.BR;
import com.cin.facesign.R;
import com.cin.facesign.databinding.ActivityFaceSignFinishBinding;
import com.cin.facesign.viewmodel.FaceSignFinishViewModel;
import com.cin.mylibrary.base.BaseActivity;


/**
 * 面签完成
 * Created by 王新超 on 2020/6/18.
 */
public class FaceSignFinishActivity extends BaseActivity<ActivityFaceSignFinishBinding, FaceSignFinishViewModel> {


    public static void startActivity(Context context, int insuranceId) {
        Intent intent = new Intent(context, FaceSignFinishActivity.class);
        intent.putExtra("insuranceId", insuranceId);
        context.startActivity(intent);
    }

    @Override
    public int initContentView(@Nullable Bundle savedInstanceState) {
        return R.layout.activity_face_sign_finish;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void init() {

        int insuranceId = getIntent().getIntExtra("insuranceId", 0);

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        viewModel.uploadFirstVideo(this, insuranceId);
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        viewModel.uploadResult.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean b) {
                if (b) {
                    binding.contentLayout.setVisibility(View.VISIBLE);

                }
            }
        });
    }
}
