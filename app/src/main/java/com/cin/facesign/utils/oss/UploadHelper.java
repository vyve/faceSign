package com.cin.facesign.utils.oss;

import android.content.Context;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.blankj.utilcode.util.ToastUtils;
import com.cin.facesign.utils.AuthUtil;
import com.cin.mylibrary.bean.BaseResponseBean;
import com.cin.mylibrary.bean.OssConfigBean;
import com.cin.mylibrary.http.FilterSubscriber;
import com.cin.mylibrary.http.RetrofitHelper;


/**
 * Created by 王新超 on 2020/7/18.
 */
public class UploadHelper {

    public interface UploadListener{
        void onSuccess(String url);

        void onFailure(String msg);
    }

    private static OSS getClient(Context context) {

        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // connction time out default 15s
        conf.setSocketTimeout(15 * 1000); // socket timeout，default 15s
        conf.setMaxConcurrentRequest(5); // synchronous request number，default 5
        conf.setMaxErrorRetry(2); // retry，default 2
        OSSLog.enableLog(); //write local log file ,path is SDCard_path\OSSLog\logs.csv

        OSSCredentialProvider credentialProvider =
                new OSSStsTokenCredentialProvider(AuthUtil.getOSSAccessKeyId(),
                        AuthUtil.getOSSAccessKeySecret(),
                        AuthUtil.getOSSSecurityToken());

        // 明文设置secret的方式建议只在测试时使用，更多鉴权模式请参考后面的`访问控制`章节
        return new OSSClient(context, AuthUtil.getOSSEndPoint(), credentialProvider, conf);
    }

    /**
     * 上传的最终方法，成功返回则一个路径
     *
     * @param objKey 上传上去后，在服务器上的独立的KEY
     * @param path   需要上传的文件的路径
     * @return 存储的地址
     */
    public static void upload(Context context, String objKey, String path,UploadListener listener) {


        RetrofitHelper.getInstance().getOSSConfig(new FilterSubscriber<BaseResponseBean<OssConfigBean>>(context){
            @Override
            public void onNext(BaseResponseBean<OssConfigBean> bean) {
                super.onNext(bean);
                AuthUtil.setOSSAccessKeyId(bean.getData().getAppKey());
                AuthUtil.setOSSAccessKeySecret(bean.getData().getAppSecret());
                AuthUtil.setOSSSecurityToken(bean.getData().getSecurityToken());
                // 构造一个上传请求
                PutObjectRequest request = new PutObjectRequest(AuthUtil.getBucketName(), objKey, path);

                // 异步上传时可以设置进度回调。
                request.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
                    @Override
                    public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                        Log.d("PutObject", "currentSize: " + currentSize + " totalSize: " + totalSize);
                    }
                });

                OSSAsyncTask task = getClient(context).asyncPutObject(request, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
                    @Override
                    public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                        Log.d("PutObject", "UploadSuccess");
                        Log.d("ETag", result.getETag());
                        Log.d("RequestId", result.getRequestId());
                        listener.onSuccess(getUrl(context,objKey));
                    }

                    @Override
                    public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                        // 请求异常。
                        if (clientExcepion != null) {
                            // 本地异常，如网络异常等。
                            clientExcepion.printStackTrace();
                            listener.onFailure("网络异常");
                        }
                        if (serviceException != null) {
                            // 服务异常。
                            Log.e("ErrorCode", serviceException.getErrorCode());
                            Log.e("RequestId", serviceException.getRequestId());
                            Log.e("HostId", serviceException.getHostId());
                            Log.e("RawMessage", serviceException.getRawMessage());
                            listener.onFailure(serviceException.getRawMessage());
                        }
                    }
                });
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                ToastUtils.showShort(error);
            }
        });


    }
    private static String getUrl(Context context,String key){
        return getClient(context).presignPublicObjectURL(AuthUtil.getBucketName(), key);
    }
}
