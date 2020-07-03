package com.cin.facesign.ui.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.SizeUtils;
import com.chad.library.BR;
import com.cin.facesign.R;
import com.cin.facesign.databinding.FragmentHomeBindingImpl;
import com.cin.facesign.ui.OnlineFaceSignActivity;
import com.cin.facesign.ui.InsurancePolicyDetailActivity;
import com.cin.facesign.ui.adapter.HomeBannerAdapter;
import com.cin.facesign.ui.adapter.HotProductAdapter;
import com.cin.facesign.ui.adapter.TodoAdapter;
import com.cin.facesign.viewmodel.HomeViewModel;
import com.cin.mylibrary.base.BaseFragment;
import com.cin.mylibrary.widget.GridSpacingItemDecoration;
import com.youth.banner.indicator.CircleIndicator;


/**
 * 首页
 * Created by 王新超 on 2020/6/12.
 */
public class HomeFragment extends BaseFragment<FragmentHomeBindingImpl, HomeViewModel> {
    @Override
    public int initContentView(@Nullable Bundle savedInstanceState) {
        return R.layout.fragment_home;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void init() {

        binding.todoRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        TodoAdapter todoAdapter = new TodoAdapter();
        binding.todoRecyclerView.setAdapter(todoAdapter);
        viewModel.getTodoData();

        binding.hotProductRecyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));
        binding.hotProductRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, SizeUtils.dp2px(9),false));
        HotProductAdapter hotProductAdapter = new HotProductAdapter();
        binding.hotProductRecyclerView.setAdapter(hotProductAdapter);

        viewModel.getHotProductData();

        viewModel.getBannerData();

        binding.banner.setAdapter(new HomeBannerAdapter(viewModel.bannerBean.get()))
                .setIndicator(new CircleIndicator(getContext()))
                .start();

        todoAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (isFastDoubleClick()){
                return;
            }
            //远程面签
            if (view.getId()==R.id.faceSign){
                OnlineFaceSignActivity.startActivity(mActivity);
            }
        });

        todoAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (isFastDoubleClick()){
                return;
            }
            InsurancePolicyDetailActivity.startActivity(mActivity);
        });
    }
}
