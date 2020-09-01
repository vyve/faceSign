package com.cin.facesign.viewmodel;

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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 王新超 on 2020/6/18.
 */
public class FaceSignFinishViewModel extends BaseViewModel {
    private static final String TAG = "视频面签";

    public MutableLiveData<Boolean> uploadResult = new MutableLiveData<>();
    private int insuranceId;
    private String firstVideoPath;
    private String secondVideoPath;
    private String thirdVideoPath;

    public FaceSignFinishViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * 开始静默上传第一段录制视频
     */
    public void uploadFirstVideo(Context context, int insuranceId) {
        this.insuranceId = insuranceId;
        showLoadingDialog();
        //上传视频
        UploadHelper.upload(context, SPUtils.getInstance().getInt(Constant.userId) + "_identifyFirstVideo", Constant.VIDEO_RECORD_PATH1,
                new UploadHelper.UploadListener() {
                    @Override
                    public void onSuccess(String url) {
                        LogUtils.i(TAG, "第一段视频上传成功," + url);
                        firstVideoPath = url;
                        uploadSecondVideo(context);
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
     * 开始静默上传第二段录制视频
     */
    public void uploadSecondVideo(Context context) {
        //上传视频
        UploadHelper.upload(context, SPUtils.getInstance().getInt(Constant.userId) + "_identifySecondVideo", Constant.SCREEN_RECORD_PATH,
                new UploadHelper.UploadListener() {
                    @Override
                    public void onSuccess(String url) {
                        LogUtils.i(TAG, "第二段视频上传成功," + url);
                        secondVideoPath = url;
                        uploadThirdVideo(context);
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
     * 开始静默上传第三段录制视频
     */
    public void uploadThirdVideo(Context context) {
        //上传视频
        UploadHelper.upload(context, SPUtils.getInstance().getInt(Constant.userId) + "_identifyThirdVideo", Constant.VIDEO_RECORD_PATH2,
                new UploadHelper.UploadListener() {
                    @Override
                    public void onSuccess(String url) {
                        LogUtils.i(TAG, "第三段视频上传成功," + url);
                        thirdVideoPath = url;
                        uploadSignImg(context);
                    }

                    @Override
                    public void onFailure(String msg) {
                        showToast(msg);
                        uploadResult.postValue(false);
                        dismissLoadingDialog();
                    }
                });

//       //上传视频
//        UploadHelper.upload(context, SPUtils.getInstance().getInt(Constant.userId) + "_identifyThirdVideo", Constant.SCREEN_RECORD_PATH,
//                new UploadHelper.UploadListener() {
//                    @Override
//                    public void onSuccess(String url) {
//                        LogUtils.i("视频上传成功,"+url);
//                        uploadSignImg(context,url,insuranceId);
//                    }
//
//                    @Override
//                    public void onFailure(String msg) {
//                        showToast(msg);
//                        uploadResult.postValue(false);
//                        dismissLoadingDialog();
//                    }
//                });

    }

    /**
     * 上传签名文件
     */
    public void uploadSignImg(Context context) {
        UploadHelper.upload(context, SPUtils.getInstance().getInt(Constant.userId) + "_identifySignImg", Constant.SIGN_PATH,
                new UploadHelper.UploadListener() {
                    @Override
                    public void onSuccess(String url) {
                        LogUtils.i(TAG, "签名文件上传成功," + url);
                        upload(context, url);
                    }

                    @Override
                    public void onFailure(String msg) {
                        showToast(msg);
                        uploadResult.postValue(false);
                        dismissLoadingDialog();
                    }
                });
    }

    private void upload(Context context, String signUrl) {
        CompleteOnlineIdentifyRequestBean bean = new CompleteOnlineIdentifyRequestBean();
        bean.setSignFile(signUrl);
        List<String> videos = new ArrayList<>();
        videos.add(firstVideoPath);
        videos.add(secondVideoPath);
        videos.add(thirdVideoPath);
        bean.setVideos(videos);
        RetrofitHelper.getInstance().completeOnlineIdentify(AuthUtil.getUserId(), insuranceId, bean, new FilterSubscriber<BaseResponseBean>(context) {
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
