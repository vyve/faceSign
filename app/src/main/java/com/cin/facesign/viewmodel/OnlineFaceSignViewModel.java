package com.cin.facesign.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.ui.camera.CameraNativeHelper;
import com.baidu.ocr.ui.camera.CameraView;
import com.cin.facesign.bean.FaceSignProgressBean;
import com.cin.mylibrary.base.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 王新超 on 2020/6/16.
 */
public class OnlineFaceSignViewModel extends BaseViewModel {

    public ObservableField<String> identityAuthText = new ObservableField<>();
    public ObservableField<Boolean> ocrAccess = new ObservableField<>();
    public ObservableField<List<FaceSignProgressBean>> progressData = new ObservableField<>();
    private String[] progressContents = new String[]{"身份验证", "产品及条款介绍", "信息询问", "电子文档展示", "客户签名"};

    public OnlineFaceSignViewModel(@NonNull Application application) {
        super(application);
        identityAuthText.set("请您正对摄像头，手持身份证，并把身份证正面放置于规定区域内。");
    }

    /**
     * OCR授权
     */
    public void initOCRAccess() {
        OCR.getInstance(getApplication()).initAccessToken(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken accessToken) {
                String token = accessToken.getAccessToken();
                ocrAccess.set(true);
                showToast("ocr 授权成功");

                CameraNativeHelper.init(getApplication(), OCR.getInstance(getApplication()).getLicense(),
                        new CameraNativeHelper.CameraNativeInitCallback() {
                            @Override
                            public void onError(int errorCode, Throwable e) {
                                String msg;
                                switch (errorCode) {
                                    case CameraView.NATIVE_SOLOAD_FAIL:
                                        msg = "加载so失败，请确保apk中存在ui部分的so";
                                        break;
                                    case CameraView.NATIVE_AUTH_FAIL:
                                        msg = "授权本地质量控制token获取失败";
                                        break;
                                    case CameraView.NATIVE_INIT_FAIL:
                                        msg = "本地质量控制";
                                        break;
                                    default:
                                        msg = String.valueOf(errorCode);
                                }
                                showToast("本地质量控制初始化错误，错误原因： " + msg);
                            }
                        });
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
                showToast("ocr 授权失败"+error.getErrorCode());

            }
        }, getApplication());
    }

    public void initProgressRecyclerView() {
        List<FaceSignProgressBean> list = new ArrayList<>();
        for (int i = 0; i < progressContents.length; i++) {
            FaceSignProgressBean bean = new FaceSignProgressBean();
            bean.setId(i);
            bean.setContent(progressContents[i]);
            if (i<=3){
                bean.setSelect(true);
            }
            list.add(bean);
        }
        progressData.set(list);
    }
}
