package com.cin.facesign.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.baidu.aip.asrwakeup3.core.mini.AutoCheck;
import com.baidu.aip.asrwakeup3.core.recog.MyRecognizer;
import com.baidu.aip.face.FaceFilter;
import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.ui.camera.CameraNativeHelper;
import com.baidu.ocr.ui.camera.CameraView;
import com.baidu.speech.asr.SpeechConstant;
import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.cin.facesign.Constant;
import com.cin.facesign.R;
import com.cin.facesign.bean.FaceSignProgressBean;
import com.cin.facesign.utils.FileUtils;
import com.cin.facesign.utils.oss.UploadHelper;
import com.cin.mylibrary.base.BaseViewModel;
import com.cin.mylibrary.bean.BaseResponseBean;
import com.cin.mylibrary.bean.CheckFaceInfoResultBean;
import com.cin.mylibrary.http.FilterSubscriber;
import com.cin.mylibrary.http.RetrofitHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 王新超 on 2020/6/16.
 */
public class OnlineFaceSignViewModel extends BaseViewModel {
    private static final String TAG = "LogOnlineFaceSignViewModel";

    public ObservableField<List<String>> voiceText = new ObservableField<>();
    public ObservableField<Boolean> ocrAccess = new ObservableField<>();
    /**
     * 人脸和身份证是否认证通过
     */
    public MutableLiveData<Boolean> faceIdentifyAccess = new MutableLiveData<>();
    public ObservableField<List<FaceSignProgressBean>> progressData = new ObservableField<>();
    private String[] progressContents = new String[]{"身份验证", "产品及条款介绍", "信息询问", "电子文档展示", "客户签名"};

    public OnlineFaceSignViewModel(@NonNull Application application) {
        super(application);
        List<String> texts = new ArrayList<>();
        texts.add(getApplication().getResources().getString(R.string.face_sign_voice_text1));
        texts.add(getApplication().getResources().getString(R.string.face_sign_voice_text2));
        texts.add(getApplication().getResources().getString(R.string.face_sign_voice_text3));
        texts.add(getApplication().getResources().getString(R.string.face_sign_voice_text4));
        texts.add(getApplication().getResources().getString(R.string.face_sign_voice_text5));
        voiceText.set(texts);
    }

    /**
     * 上传识别的人脸
     */
    public void uploadHeadImg(Context context){
        UploadHelper.upload(context, SPUtils.getInstance().getInt(Constant.userId) + "_identifyHeadImg", Constant.FACE_PATH,
                new UploadHelper.UploadListener() {
                    @Override
                    public void onSuccess(String url) {
                        LogUtils.i("人脸上传成功,"+url);
                        uploadIDCardImg(context,url);
                    }

                    @Override
                    public void onFailure(String msg) {
                        showToast(msg);
                        faceIdentifyAccess.postValue(false);
                    }
                });
    }

    /**
     * 上传识别出的身份证
     */
    private void uploadIDCardImg(Context context,String faceImgUrl){
        UploadHelper.upload(context, SPUtils.getInstance().getInt(Constant.userId) + "_identifyIDCardImg", Constant.ID_CARD_PATH,
                new UploadHelper.UploadListener() {
                    @Override
                    public void onSuccess(String url) {
                        LogUtils.i("身份证上传成功,"+url);
                        compareFaceInfo(context,faceImgUrl,url);
                    }

                    @Override
                    public void onFailure(String msg) {
                        showToast(msg);
                        faceIdentifyAccess.postValue(false);
                    }
                });
    }

    /**
     * 校对人脸信息和身份证信息
     */
    private void  compareFaceInfo(Context context,String faceImgUrl,String idCardImgUrl){
        RetrofitHelper.getInstance().checkFaceInfo(faceImgUrl,idCardImgUrl,new FilterSubscriber<BaseResponseBean<CheckFaceInfoResultBean>>(context){
            @Override
            public void onNext(BaseResponseBean<CheckFaceInfoResultBean> checkFaceInfoResultBeanBaseResponseBean) {
                super.onNext(checkFaceInfoResultBeanBaseResponseBean);
                faceIdentifyAccess.postValue(true);
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                showToast(error);
            }
        });
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

    /**
     * 开始录音，点击“开始”按钮后调用。
     * 基于DEMO集成2.1, 2.2 设置识别参数并发送开始事件
     */
    @SuppressLint("HandlerLeak")
    public void startOSRSpeech(MyRecognizer recognizer) {
        // DEMO集成步骤2.1 拼接识别参数： 此处params可以打印出来，直接写到你的代码里去，最终的json一致即可。
        final Map<String, Object> params = new HashMap<>();
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME,false);
        LogUtils.i(TAG, "设置的start输入参数：" + params);
        // 复制此段可以自动检测常规错误
        (new AutoCheck(getApplication(), new Handler() {
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainErrorMessage(); // autoCheck.obtainAllMessage();
                        ; // 可以用下面一行替代，在logcat中查看代码
                         LogUtils.i("AutoCheckMessage", message);
                    }
                }
            }
        }, false)).checkAsr(params);

        // 这里打印出params， 填写至您自己的app中，直接调用下面这行代码即可。
        // DEMO集成步骤2.2 开始识别
        recognizer.start(params);
    }

    /**
     * 初始化列表数据
     */
    public void initProgressRecyclerView() {
        List<FaceSignProgressBean> list = new ArrayList<>();
        for (int i = 0; i < progressContents.length; i++) {
            FaceSignProgressBean bean = new FaceSignProgressBean();
            bean.setId(i);
            if (i==0){
                bean.setSelect(true);
            }
            bean.setContent(progressContents[i]);
            list.add(bean);
        }
        progressData.set(list);
    }

    /**
     * 保存图片
     */
    public void saveFaceBmp(FaceFilter.TrackedModel model) {
        String filePath = Constant.FACE_PATH;
        final Bitmap face = model.cropFace();
        if (face != null) {
            ImageUtils.save(face, filePath, Bitmap.CompressFormat.JPEG);
        }
        File path = new File(filePath);
        if (!path.exists()) {
            filePath = "";
        }
        final File file = new File(filePath);
        boolean saved = false;
        try {
            byte[] buf = FileUtils.readFile(file);
            if (buf.length > 0) {
                saved = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
       LogUtils.i("aaaa,头像保存成功");
    }

    public void saveIdCard(Bitmap bitmap){
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(Constant.ID_CARD_PATH);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            bitmap.recycle();
            fileOutputStream.close();

            LogUtils.i("aaaa,idCard保存成功");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
