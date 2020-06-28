package com.cin.facesign.ui.adapter;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.cin.facesign.R;
import com.cin.facesign.bean.InsurancePolicyBean;
import com.cin.facesign.databinding.AdapterTodoInsurancePolicyBinding;

/**
 * 待办事项
 * Created by 王新超 on 2020/6/17.
 */
public class TodoAdapter extends BaseQuickAdapter<InsurancePolicyBean, BaseViewHolder> {
    public TodoAdapter() {
        super(R.layout.adapter_todo_insurance_policy);

        addChildClickViewIds(R.id.faceSign);
    }

    @Override
    protected void onItemViewHolderCreated(BaseViewHolder viewHolder, int viewType) {
        DataBindingUtil.bind(viewHolder.itemView);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, InsurancePolicyBean item) {
        AdapterTodoInsurancePolicyBinding binding = baseViewHolder.getBinding();
        if (binding != null) {
            binding.setBean(item);
        }

    }
}
