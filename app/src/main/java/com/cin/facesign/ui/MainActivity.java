package com.cin.facesign.ui;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import com.cin.facesign.R;
import com.cin.facesign.databinding.ActivityMainBinding;
import com.cin.facesign.ui.fragment.BusinessFragment;
import com.cin.facesign.ui.fragment.HomeFragment;
import com.cin.facesign.ui.fragment.PersonalFragment;
import com.cin.facesign.ui.fragment.PolicyFragment;
import com.cin.facesign.viewmodel.MainViewModel;
import com.cin.mylibrary.base.BaseActivity;

public class MainActivity extends BaseActivity<ActivityMainBinding, MainViewModel> {

    private HomeFragment homeFragment;
    private PolicyFragment policyFragment;
    private BusinessFragment businessFragment;
    private PersonalFragment personalFragment;

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, MainActivity.class));
    }

    @Override
    public int initContentView(@Nullable Bundle savedInstanceState) {
        return R.layout.activity_main;
    }

    @Override
    public int initVariableId() {
        return 0;
    }

    @Override
    public void init() {
        binding.setPresenter(new Presenter());
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        showHomeFragment(transaction);
    }

    public class Presenter {
        public void onHomeClick() {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            hideFragment(transaction);
            showHomeFragment(transaction);
        }

        /**
         * 我的保单
         */
        public void onPolicyClick() {
            mImmersionBar.statusBarDarkFont(true).init();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            hideFragment(transaction);
            binding.mainPolicyImg.setSelected(true);
            binding.mainPolicyText.setSelected(true);
            if (policyFragment == null) {
                policyFragment = (PolicyFragment) getSupportFragmentManager().findFragmentByTag("policyFragment");
                if (policyFragment == null) {
                    policyFragment = new PolicyFragment();
                    transaction.add(R.id.main_container, policyFragment, "policyFragment");
                }
            } else {

                transaction.show(policyFragment);
            }
            transaction.commitAllowingStateLoss();
        }

        /**
         * 业务办理
         */
        public void onBusinessClick() {
            mImmersionBar.statusBarDarkFont(true).init();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            hideFragment(transaction);
            binding.mainBusinessImg.setSelected(true);
            binding.mainBusinessText.setSelected(true);
            if (businessFragment == null) {
                businessFragment = (BusinessFragment) getSupportFragmentManager().findFragmentByTag("businessFragment");
                if (businessFragment == null) {
                    businessFragment = new BusinessFragment();
                    transaction.add(R.id.main_container, businessFragment, "businessFragment");
                }
            } else {
                transaction.show(businessFragment);
            }
            transaction.commitAllowingStateLoss();
        }

        /**
         * 个人设置
         */
        public void onPersonalClick() {

            mImmersionBar.statusBarDarkFont(false).init();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            hideFragment(transaction);
            binding.mainPersonalImg.setSelected(true);
            binding.mainPersonalText.setSelected(true);
            if (personalFragment == null) {
                personalFragment = (PersonalFragment) getSupportFragmentManager().findFragmentByTag("personalFragment");
                if (personalFragment == null) {
                    personalFragment = new PersonalFragment();
                    transaction.add(R.id.main_container, personalFragment, "personalFragment");
                }
            } else {
                transaction.show(personalFragment);
            }
            transaction.commitAllowingStateLoss();
        }
    }

    /**
     * 展示首页
     */
    private void showHomeFragment(FragmentTransaction transaction) {
        mImmersionBar.statusBarDarkFont(false).init();
        binding.mainHomeImg.setSelected(true);
        binding.mainHomeText.setSelected(true);
        if (homeFragment == null) {
            homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("homeFragment");
            if (homeFragment == null) {
                homeFragment = new HomeFragment();
                transaction.add(R.id.main_container, homeFragment, "homeFragment");
            }
        } else {
            transaction.show(homeFragment);
        }
        transaction.commitAllowingStateLoss();
    }

    /**
     * 隐藏所有fragment
     */
    private void hideFragment(FragmentTransaction transaction) {
        binding.mainHomeImg.setSelected(false);
        binding.mainHomeText.setSelected(false);
        binding.mainPolicyImg.setSelected(false);
        binding.mainPolicyText.setSelected(false);
        binding.mainBusinessImg.setSelected(false);
        binding.mainBusinessText.setSelected(false);
        binding.mainPersonalImg.setSelected(false);
        binding.mainPersonalText.setSelected(false);
        if (homeFragment != null) {
            transaction.hide(homeFragment);
        }
        if (policyFragment != null) {
            transaction.hide(policyFragment);
        }
        if (businessFragment != null) {
            transaction.hide(businessFragment);
        }
        if (personalFragment != null) {
            transaction.hide(personalFragment);
        }
    }
}