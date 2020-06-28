package com.cin.mylibrary.base;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProvider;


import com.blankj.utilcode.util.ToastUtils;
import com.cin.mylibrary.widget.LoadingDialog;
import com.gyf.barlibrary.ImmersionBar;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;


/**
 * Created by 王新超 on 2020/3/9.
 */
public abstract class BaseActivity<V extends ViewDataBinding, VM extends BaseViewModel> extends AppCompatActivity implements IBaseView {
    protected V binding;
    protected VM viewModel;
    public ImmersionBar mImmersionBar;
    private LoadingDialog mDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViewDataBinding(savedInstanceState);
        mImmersionBar = ImmersionBar.with(this);
        mImmersionBar.transparentStatusBar()
                .statusBarDarkFont(true)
                .init();
        init();
        initViewObservable();

    }

    /**
     * 注入绑定
     */
    @SuppressWarnings("unchecked")
    private void initViewDataBinding(Bundle savedInstanceState) {
        binding = DataBindingUtil.setContentView(this, initContentView(savedInstanceState));

        viewModel = initViewModel();
        if (viewModel == null) {
            Class modelClass;
            Type type = getClass().getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                modelClass = (Class) ((ParameterizedType) type).getActualTypeArguments()[1];
            } else {
                //如果没有指定泛型参数，则默认使用BaseViewModel
                modelClass = BaseViewModel.class;
            }
            viewModel = (VM) ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()).create(modelClass);
        }

        //关联ViewModel
        binding.setVariable(initVariableId(), viewModel);
        binding.setLifecycleOwner(this);
        //让ViewModel拥有View的生命周期感应
        getLifecycle().addObserver(viewModel);
    }

    @Override
    public VM initViewModel() {
        return null;
    }

    @Override
    public void initViewObservable() {

    }

    protected void showLoadingDialog() {
        if (mDialog == null) {
            mDialog = new LoadingDialog(this);
        }
        mDialog.show();
    }

    protected void dismissLoadingDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    protected void showToast(String msg) {
        ToastUtils.showShort(msg);
    }

    /**
     * 设置sp 字体大小不随系统字体大小改变而改变
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.fontScale != 1) {
            getResources();
        }
    }

    @Override
    public Resources getResources() {
        Resources resources = super.getResources();
        //fontScale 不为1 需要强制设置成1
        if (resources.getConfiguration().fontScale != 1) {
            Configuration newConfig = new Configuration();
            //设置默认值即fontScale为1
            newConfig.setToDefaults();
            resources.updateConfiguration(newConfig, resources.getDisplayMetrics());
        }
        return resources;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mImmersionBar != null) {
            mImmersionBar.destroy();  //必须调用该方法，防止内存泄漏，不调用该方法，如果界面bar发生改变，在不关闭app的情况下，退出此界面再进入将记忆最后一次bar改变的状态
        }
    }
}
