package com.cin.facesign.viewmodel;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.cin.facesign.Constant;
import com.cin.facesign.utils.AuthUtil;
import com.cin.facesign.utils.oss.UploadHelper;
import com.cin.mylibrary.base.BaseViewModel;
import com.cin.mylibrary.bean.BaseResponseBean;
import com.cin.mylibrary.http.FilterSubscriber;
import com.cin.mylibrary.http.RetrofitHelper;
import com.cin.mylibrary.request_bean.CompleteOnlineIdentifyRequestBean;

/**
 * Created by 王新超 on 2020/6/18.
 */
public class FaceSignFinishViewModel extends BaseViewModel {

    public MutableLiveData<Boolean> uploadResult = new MutableLiveData<>();

    public FaceSignFinishViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * 上传数据
     */
    public void uploadData(Activity context,int insuranceId){
       showLoadingDialog();

       //上传视频
        UploadHelper.upload(context, SPUtils.getInstance().getInt(Constant.userId) + "_identifyVideo", Constant.VIDEO_RECORD_PATH,
                new UploadHelper.UploadListener() {
                    @Override
                    public void onSuccess(String url) {
                        LogUtils.i("视频上传成功,"+url);
                        uploadSignImg(context,url,insuranceId);
                    }

                    @Override
                    public void onFailure(String msg) {
                        showToast(msg);
                        uploadResult.postValue(false);
                        dismissLoadingDialog();
                    }
                });

    }

    /**
     * 上传签名文件
     */
    public void uploadSignImg(Context context,String videoUrl,int insuranceId){
        UploadHelper.upload(context, SPUtils.getInstance().getInt(Constant.userId) + "_identifySignImg", Constant.SIGN_PATH,
                new UploadHelper.UploadListener() {
                    @Override
                    public void onSuccess(String url) {
                        LogUtils.i("身份证上传成功,"+url);
                        uploadData(context,url,videoUrl,insuranceId);

                    }

                    @Override
                    public void onFailure(String msg) {
                        showToast(msg);
                        uploadResult.postValue(false);
                        dismissLoadingDialog();
                    }
                });
    }

    private void uploadData(Context context,String signUrl,String videoUrl,int insuranceId){
        CompleteOnlineIdentifyRequestBean bean = new CompleteOnlineIdentifyRequestBean();
        bean.setData(signUrl);
        bean.setVideo(videoUrl);
        RetrofitHelper.getInstance().completeOnlineIdentify(AuthUtil.getUserId(),insuranceId,bean,new FilterSubscriber<BaseResponseBean>(context){
            @Override
            public void onNext(BaseResponseBean baseResponseBean) {
                super.onNext(baseResponseBean);
                dismissLoadingDialog();
                uploadResult.postValue(true);
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                showToast(error);
                uploadResult.postValue(false);
                dismissLoadingDialog();
            }
        });
    }

}
