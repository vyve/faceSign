package com.cin.facesign.ui.adapter;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.cin.facesign.R;
import com.cin.facesign.databinding.AdapterHotProductBinding;
import com.cin.mylibrary.bean.InsuranceBean;

/**
 * 热销产品
 * Created by 王新超 on 2020/6/17.
 */
public class HotProductAdapter extends BaseQuickAdapter<InsuranceBean, BaseViewHolder> {
    public HotProductAdapter() {
        super(R.layout.adapter_hot_product);
    }

    @Override
    protected void onItemViewHolderCreated(@NonNull BaseViewHolder viewHolder, int viewType) {
        super.onItemViewHolderCreated(viewHolder, viewType);
        DataBindingUtil.bind(viewHolder.itemView);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, InsuranceBean bean) {
        AdapterHotProductBinding binding = baseViewHolder.getBinding();
        if (binding!=null){
            binding.setBean(bean);
        }
    }
}
