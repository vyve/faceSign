package com.cin.facesign.ui.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.cin.facesign.R;
import com.cin.facesign.databinding.FragmentPersonalBinding;
import com.cin.facesign.ui.FaceSignFinishActivity;
import com.cin.facesign.ui.LoginActivity;
import com.cin.facesign.ui.SignatureActivity;
import com.cin.facesign.ui.WebActivity;
import com.cin.facesign.viewmodel.PersonalViewModel;
import com.cin.facesign.widget.ApplyAcceptDialog;
import com.cin.facesign.widget.TurnHumanServiceDialog;
import com.cin.mylibrary.base.BaseFragment;

/**
 * 个人设置
 * Created by 王新超 on 2020/6/12.
 */
public class PersonalFragment extends BaseFragment<FragmentPersonalBinding, PersonalViewModel> {
    @Override
    public int initContentView(@Nullable Bundle savedInstanceState) {
        return R.layout.fragment_personal;
    }

    @Override
    public int initVariableId() {
        return 0;
    }

    @Override
    public void init() {
        binding.setPresenter(new Presenter());
    }

    public class Presenter {
        /**
         * 个人信息变更
         */
        public void onChangeInfoClick() {
            showToast("个人信息变更");

            SignatureActivity.startActivity(mActivity);
        }

        /**
         * 手机号码变更
         */
        public void onChangePhoneClick() {
            showToast("手机号码变更");
            FaceSignFinishActivity.startActivity(mActivity);
        }

        /**
         * 密码变更
         */
        public void onChangePasswordClick() {
//            showToast("密码变更");
//            ApplyAcceptDialog dialog = new ApplyAcceptDialog(mActivity);
//            dialog.show();
            LoginActivity.startActivity(mActivity);
        }

        /**
         * 人脸信息变更
         */
        public void onChangeFaceClick() {
            showToast("人脸信息变更");
            TurnHumanServiceDialog dialog = new TurnHumanServiceDialog(mActivity);
            dialog.show();
            dialog.setOnButton1ClickListener(v -> {
                showToast("人工辅助");
                WebActivity.startActivity(mActivity,"http://www.baidu.com");
            });
            dialog.setOnButton2ClickListener(v -> showToast("线下面签"));
        }
    }
}
