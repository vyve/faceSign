package com.cin.facesign.ui.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.BR;
import com.cin.facesign.R;
import com.cin.facesign.databinding.FragmentPolicyBinding;
import com.cin.facesign.ui.adapter.TodoAdapter;
import com.cin.facesign.viewmodel.PolicyViewModel;
import com.cin.mylibrary.base.BaseFragment;

/**
 * 我的保单
 * Created by 王新超 on 2020/6/12.
 */
public class PolicyFragment extends BaseFragment<FragmentPolicyBinding, PolicyViewModel> {
    @Override
    public int initContentView(@Nullable Bundle savedInstanceState) {
        return R.layout.fragment_policy;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void init() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        TodoAdapter todoAdapter = new TodoAdapter();

        binding.recyclerView.setAdapter(todoAdapter);

        viewModel.getTodoData();

    }
}
