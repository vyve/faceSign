package com.cin.facesign.ui.adapter;

import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cin.facesign.R;
import com.cin.facesign.utils.imageloader.ImageLoader;
import com.youth.banner.adapter.BannerAdapter;
import com.youth.banner.util.BannerUtils;

import java.util.List;

/**
 * Created by 王新超 on 2020/6/17.
 */
public class HomeBannerAdapter extends BannerAdapter<String, HomeBannerAdapter.BannerViewHolder> {
    public HomeBannerAdapter(List<String> datas) {
        super(datas);
    }

    @Override
    public BannerViewHolder onCreateHolder(ViewGroup parent, int viewType) {
        ImageView imageView= (ImageView) BannerUtils.getView(parent,R.layout.adapter_home_banner);
        //通过裁剪实现圆角
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BannerUtils.setBannerRound(imageView,20);
        }
        return new BannerViewHolder(imageView);
    }

    @Override
    public void onBindView(BannerViewHolder holder, String data, int position, int size) {
        ImageLoader.getInstance().load(data).centerCrop().into(holder.imageView);
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imageView = (ImageView) itemView;
        }
    }
}
