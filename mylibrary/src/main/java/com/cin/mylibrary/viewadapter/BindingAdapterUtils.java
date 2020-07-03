package com.cin.mylibrary.viewadapter;


import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

/**
 * Created by 王新超 on 2020/3/6.
 */
public class BindingAdapterUtils {

    @BindingAdapter({"items"})
    public static <T> void setListItems(RecyclerView recyclerView, List<T> items) {
        BaseQuickAdapter<T, BaseViewHolder> adapter = (BaseQuickAdapter<T, BaseViewHolder>) recyclerView.getAdapter();
        if (adapter != null && items != null) {
            adapter.replaceData(items);
        }
    }
}
