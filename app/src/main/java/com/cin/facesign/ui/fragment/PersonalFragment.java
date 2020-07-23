package com.cin.facesign.ui.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.SPUtils;
import com.chad.library.BR;
import com.cin.facesign.Constant;
import com.cin.facesign.R;
import com.cin.facesign.databinding.FragmentPersonalBinding;
import com.cin.facesign.ui.ElectronicDocumentActivity;
import com.cin.facesign.ui.FaceSignFinishActivity;
import com.cin.facesign.ui.LoginActivity;
import com.cin.facesign.utils.oss.UploadHelper;
import com.cin.facesign.viewmodel.PersonalViewModel;
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
        return BR.viewModel;
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
        }

        /**
         * 手机号码变更
         */
        public void onChangePhoneClick() {
            showToast("手机号码变更");
        }

        /**
         * 密码变更
         */
        public void onChangePasswordClick() {
            LoginActivity.startActivity(mActivity);
        }

        /**
         * 人脸信息变更
         */
        public void onChangeFaceClick() {
            showToast("人脸信息变更");

        }
    }
}
