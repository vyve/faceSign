package com.cin.mylibrary.base;

import android.os.Bundle;

import androidx.annotation.Nullable;

/**
 * Created by 王新超 on 2020/5/28.
 */
public interface IBaseView<VM extends BaseViewModel> {
    /**
     * 初始化根布局
     *
     * @return 布局layout的id
     */
    int initContentView(@Nullable Bundle savedInstanceState);

    /**
     * 初始化ViewModel的id
     * @return BR的id
     */
    int initVariableId();

    void init();

    VM initViewModel();

    /**
     * 初始化界面观察者的监听
     */
    void initViewObservable();
}
