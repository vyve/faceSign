package com.cin.facesign.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.cin.facesign.bean.InsurancePolicyBean;
import com.cin.facesign.bean.ProductBean;
import com.cin.mylibrary.request_bean.BaseRequestBean;
import com.cin.mylibrary.base.BaseViewModel;
import com.cin.mylibrary.bean.BaseResponseBean;
import com.cin.mylibrary.http.FilterSubscriber;
import com.cin.mylibrary.http.RetrofitHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 王新超 on 2020/6/13.
 */
public class HomeViewModel extends BaseViewModel {
    public ObservableField<List<InsurancePolicyBean>> todoBean = new ObservableField<>();
    public ObservableField<List<ProductBean>> hotProductBean = new ObservableField<>();
    public ObservableField<List<String>> bannerBean = new ObservableField<>();
    public ObservableField<String> adUrl = new ObservableField<>();
    public HomeViewModel(@NonNull Application application) {
        super(application);
        adUrl.set("http://img4.imgtn.bdimg.com/it/u=1778364162,3539759504&fm=26&gp=0.jpg");
    }

    public void getTodoData() {
        List<InsurancePolicyBean> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            InsurancePolicyBean bean = new InsurancePolicyBean();
            list.add(bean);
        }
        todoBean.set(list);
    }

    public void getHotProductData() {
        List<ProductBean> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ProductBean bean = new ProductBean();
            list.add(bean);
        }
        hotProductBean.set(list);

        RetrofitHelper.getInstance().getAllInsurance(new BaseRequestBean(),new FilterSubscriber<BaseResponseBean>(getApplication()){
            @Override
            public void onNext(BaseResponseBean baseModel) {
                super.onNext(baseModel);

            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
            }
        });
    }

    public void getBannerData() {
        List<String> list = new ArrayList<>();
        list.add("http://img4.imgtn.bdimg.com/it/u=1778364162,3539759504&fm=26&gp=0.jpg");
        list.add("http://img3.imgtn.bdimg.com/it/u=3622639324,53139701&fm=26&gp=0.jpg");
        list.add("http://img2.imgtn.bdimg.com/it/u=2120945955,1146510737&fm=26&gp=0.jpg");
        bannerBean.set(list);
    }
}
