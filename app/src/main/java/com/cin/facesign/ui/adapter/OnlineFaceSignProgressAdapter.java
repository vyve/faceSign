package com.cin.facesign.ui.adapter;

import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.blankj.utilcode.util.SizeUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.cin.facesign.R;
import com.cin.facesign.bean.FaceSignProgressBean;
import com.cin.facesign.databinding.AdapterOnlinFaceSignProgressBinding;
import com.noober.background.drawable.DrawableCreator;

/**
 * Created by 王新超 on 2020/6/18.
 */
public class OnlineFaceSignProgressAdapter extends BaseQuickAdapter<FaceSignProgressBean, BaseViewHolder> {

    private final Drawable contentSelectDrawable;
    private final Drawable contentUnSelectDrawable;

    public OnlineFaceSignProgressAdapter() {
        super(R.layout.adapter_onlin_face_sign_progress);

        contentSelectDrawable = new DrawableCreator.Builder()
                .setCornersRadius(SizeUtils.dp2px(20))
                .setSolidColor(0x80000000)
                .build();
        contentUnSelectDrawable = new DrawableCreator.Builder()
                .setCornersRadius(SizeUtils.dp2px(20))
                .setSolidColor(0x33000000)
                .build();
    }
    @Override
    protected void onItemViewHolderCreated(@NonNull BaseViewHolder viewHolder, int viewType) {
        super.onItemViewHolderCreated(viewHolder, viewType);
        DataBindingUtil.bind(viewHolder.itemView);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, FaceSignProgressBean faceSignProgressBean) {
        AdapterOnlinFaceSignProgressBinding binding=baseViewHolder.getBinding();
        if (binding!=null){
            binding.setBean(faceSignProgressBean);
            //第一个
            if (baseViewHolder.getAdapterPosition()==0){
                binding.view1.setVisibility(View.INVISIBLE);
            }
            //最后一个
            if (baseViewHolder.getAdapterPosition()==getItemCount()-1){
                binding.view2.setVisibility(View.INVISIBLE);
            }
            if (faceSignProgressBean.isSelect()){
                binding.content.setBackground(contentSelectDrawable);
                binding.content.setTextColor(0xffffffff);
                binding.view1.setBackgroundColor(0xffffffff);
                binding.view2.setBackgroundColor(0xffffffff);
                binding.checkedImg.setImageResource(R.mipmap.face_sign_progress_img_true);
            }else {
                binding.content.setBackground(contentUnSelectDrawable);
                binding.content.setTextColor(0x80ffffff);
                binding.view1.setBackgroundColor(0xffB0B0B2);
                binding.view2.setBackgroundColor(0xffB0B0B2);
                binding.checkedImg.setImageResource(R.mipmap.face_sign_progress_img_false);
            }
        }

    }

}
