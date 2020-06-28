package com.cin.facesign.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.BarUtils;
import com.cin.facesign.R;

/**
 * 标题
 * Created by 王新超 on 2020/6/12.
 */
public class TitleBar extends ConstraintLayout {

    private OnTitleClickListener listener;
    private TextView statusView;
    private TextView titleView;

    public TitleBar(Context context) {
        this(context,null);
    }

    public TitleBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public TitleBar(final Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        View view = LayoutInflater.from(context).inflate(R.layout.layout_title_bar, this);
        View leftLy = this.findViewById(R.id.titleBar_left);
        ImageView leftImage=this.findViewById(R.id.titleBar_leftImage);
        titleView = this.findViewById(R.id.titleBar_title);
        TextView rightTextView=this.findViewById(R.id.titleBar_rightText);
        View rightLy = this.findViewById(R.id.titleBar_right);
        ImageView rightImage = this.findViewById(R.id.titleBar_rightImage);
        statusView = this.findViewById(R.id.titleBar_status);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TitleBar);
        boolean haveBackView = array.getBoolean(R.styleable.TitleBar_haveBackView, true);
        String titleText = array.getString(R.styleable.TitleBar_title);
        String rightText = array.getString(R.styleable.TitleBar_rightText);
        int titleTextColor = array.getColor(R.styleable.TitleBar_titleTextColor, 0xff000000);
        int backgroundColor = array.getColor(R.styleable.TitleBar_backgroundColor, 0xffffffff);
        int rightImageId = array.getResourceId(R.styleable.TitleBar_rightImage, 0);
        boolean needStatusView = array.getBoolean(R.styleable.TitleBar_needStatusView, false);
        int statusColor = array.getColor(R.styleable.TitleBar_statusColor, 0xffffffff);
        int leftImageId = array.getResourceId(R.styleable.TitleBar_leftImage, 0);
        array.recycle();

        view.setBackgroundColor(backgroundColor);
        setTitle(titleText);
        titleView.setTextColor(titleTextColor);
        leftLy.setVisibility(haveBackView?VISIBLE:GONE);
        if (TextUtils.isEmpty(rightText)&&rightImageId==0){
            rightLy.setVisibility(GONE);
        }else {
            rightLy.setVisibility(VISIBLE);
            if (TextUtils.isEmpty(rightText)){
                rightTextView.setVisibility(GONE);
            }
            if (rightImageId==0){
                rightImage.setVisibility(GONE);
            }
            rightTextView.setText(rightText);
            rightImage.setImageResource(rightImageId);
        }

        if (leftImageId!=0){
            leftImage.setImageResource(leftImageId);
        }

        leftLy.setOnClickListener(v -> {
            if (context instanceof Activity){
                ((Activity) context).finish();
            }
        });

        rightLy.setOnClickListener(v -> {
            if (listener!=null){
                listener.onRightClick();
            }
        });

        statusView.setHeight(BarUtils.getStatusBarHeight());
        statusView.setBackgroundColor(statusColor);
        setStatusView(needStatusView);
    }

    public void setTitle(String title){
        titleView.setText(title);
    }

    private void setStatusView(boolean needStatusView){
        if (needStatusView){
            statusView.setVisibility(VISIBLE);
        }else {
            statusView.setVisibility(GONE);
        }
    }

    public void setOnTitleClickListener(OnTitleClickListener listener){
        this.listener = listener;
    }

    public interface OnTitleClickListener{
        void onRightClick();
    }
}
