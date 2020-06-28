package com.cin.facesign;

import android.app.Application;

import com.baidu.aip.FaceEnvironment;
import com.baidu.aip.FaceSDKManager;
import com.baidu.idl.facesdk.FaceTracker;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.Utils;
import com.cin.facesign.utils.imageloader.GlideLoader;
import com.cin.facesign.utils.imageloader.ImageLoader;
import com.cin.mylibrary.AppConstant;
import com.cin.mylibrary.bean.FaceSDKToken;
import com.cin.mylibrary.http.RetrofitHelper;
import com.tencent.bugly.crashreport.CrashReport;

import rx.Subscriber;

/**
 * Created by 王新超 on 2020/6/12.
 */
public class MyApp extends Application {
    private static MyApp instance;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initFaceSDK();
        Utils.init(this);
        ImageLoader.getInstance().setImageLoader(new GlideLoader());

        getFaceSDKToken();
        CrashReport.initCrashReport(getApplicationContext(), "efd17bb886", false);
    }

    /**
     * 更新faceSDK token
     */
    private void getFaceSDKToken() {
        RetrofitHelper.getInstance().getSDKToken(new Subscriber<FaceSDKToken>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(FaceSDKToken faceSDKToken) {
                SPUtils.getInstance().put(AppConstant.SP_FACE_TOKEN,faceSDKToken.getAccess_token());
            }
        });
    }

    /**
     * 初始化FaceSDK
     */
    private void initFaceSDK() {
        // 为了android和ios 区分授权，appId=appname_face_android ,其中appname为申请sdk时的应用名
        // 应用上下文
        // 申请License取得的APPID
        // assets目录下License文件名
        FaceSDKManager.getInstance().init(this, AppConstant.faceSDKLicenseID,  AppConstant.faceSDKLicenseFileName);
        FaceTracker tracker = FaceSDKManager.getInstance().getFaceTracker(this);  //.getFaceConfig();
        // SDK初始化已经设置完默认参数（推荐参数），您也根据实际需求进行数值调整

        // 模糊度范围 (0-1) 推荐小于0.7
        tracker.set_blur_thr(FaceEnvironment.VALUE_BLURNESS);
        // 光照范围 (0-1) 推荐大于40
        tracker.set_illum_thr(FaceEnvironment.VALUE_BRIGHTNESS);
        // 裁剪人脸大小
        tracker.set_cropFaceSize(FaceEnvironment.VALUE_CROP_FACE_SIZE);
        // 人脸yaw,pitch,row 角度，范围（-45，45），推荐-15-15
        tracker.set_eulur_angle_thr(FaceEnvironment.VALUE_HEAD_PITCH, FaceEnvironment.VALUE_HEAD_ROLL,
                FaceEnvironment.VALUE_HEAD_YAW);

        // 最小检测人脸（在图片人脸能够被检测到最小值）80-200， 越小越耗性能，推荐120-200
        tracker.set_min_face_size(FaceEnvironment.VALUE_MIN_FACE_SIZE);
        //
        tracker.set_notFace_thr(FaceEnvironment.VALUE_NOT_FACE_THRESHOLD);
        // 人脸遮挡范围 （0-1） 推荐小于0.5
        tracker.set_occlu_thr(FaceEnvironment.VALUE_OCCLUSION);
        // 是否进行质量检测
        tracker.set_isCheckQuality(true);
        // 是否进行活体校验
        tracker.set_isVerifyLive(false);
    }

    public static MyApp getInstance(){
        return instance;
    }
}
