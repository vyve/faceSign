package com.cin.facesign.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.baidu.aip.ImageFrame;
import com.baidu.aip.face.camera.CameraImageSource;
import com.baidu.aip.face.DetectRegionProcessor;
import com.baidu.aip.face.FaceDetectManager;
import com.baidu.aip.face.FaceFilter;
import com.baidu.aip.face.camera.PermissionCallback;
import com.baidu.idl.facesdk.FaceInfo;
import com.blankj.utilcode.util.ScreenUtils;
import com.chad.library.BR;
import com.cin.facesign.R;
import com.cin.facesign.databinding.ActivityFaceIdentifyBinding;
import com.cin.facesign.utils.WaveHelper;
import com.cin.facesign.viewmodel.FaceIdentifyViewModel;
import com.cin.facesign.widget.face.WaveView;
import com.cin.mylibrary.base.BaseActivity;

import java.lang.ref.WeakReference;

/**
 * 人脸识别、检测
 * Created by 王新超 on 2020/6/15.
 */
public class FaceIdentifyActivity extends BaseActivity<ActivityFaceIdentifyBinding, FaceIdentifyViewModel> {

    private static final double ANGLE = 15;

    private FaceDetectManager faceDetectManager;

    private boolean mGoodDetect = false;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mCurrentFaceId = -1;
    private String mCurrentTips;
    private long mLastTipsTime = 0;
    private WaveView mWaveView;
    private WaveHelper mWaveHelper;

    /**
     * 开始人脸检测
     */
    private boolean mBeginDetect = false;
    private DetectRegionProcessor cropProcessor = new DetectRegionProcessor();

    private boolean mDetectStopped = false;
    private InnerHandler mHandler;

    public static void startActivityForResult(Activity activity, int code) {
        activity.startActivityForResult(new Intent(activity, FaceIdentifyActivity.class),code);
    }

    @Override
    public int initContentView(@Nullable Bundle savedInstanceState) {
        return R.layout.activity_face_identify;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void init() {

        faceDetectManager = new FaceDetectManager(this);
        mScreenWidth = ScreenUtils.getScreenWidth();
        mScreenHeight = ScreenUtils.getScreenHeight();

        CameraImageSource cameraImageSource = new CameraImageSource(this,true);
        cameraImageSource.setPreviewView(binding.faceIdentifyPreviewView);
        faceDetectManager.setImageSource(cameraImageSource);

        viewModel.setBrightness();

        initFaceSDK();

        faceDetectManager.setOnTrackListener(new FaceFilter.OnTrackListener() {
            @Override
            public void onTrack(FaceFilter.TrackedModel trackedModel) {
                if (trackedModel.meetCriteria() && mGoodDetect) {
                    mGoodDetect = false;
                    Boolean aBoolean = viewModel.mSavedBitmap.get();
                    if (aBoolean!=null) {
                        if (!aBoolean && mBeginDetect) {
                            if (viewModel.saveFaceBmp(trackedModel)) {
                                setResult(RESULT_OK);
                                finish();
                            }
                        }
                    }
                }
            }
        });

        cameraImageSource.getCameraControl().setPermissionCallback(new PermissionCallback() {
            @Override
            public boolean onRequestPermission() {
                ActivityCompat.requestPermissions(FaceIdentifyActivity.this,
                        new String[]{Manifest.permission.CAMERA}, 100);
                return true;
            }
        });

        binding.faceIdentifyFaceRoundView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                start();
                binding.faceIdentifyFaceRoundView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        cameraImageSource.getCameraControl().setPreviewView(binding.faceIdentifyPreviewView);
        // 设置检测裁剪处理器
        faceDetectManager.addPreProcessor(cropProcessor);

        binding.faceIdentifySuccess.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (binding.faceIdentifySuccess.getTag() == null) {
                    Rect rect = binding.faceIdentifyFaceRoundView.getFaceRoundRect();
                    RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) binding.faceIdentifySuccess.getLayoutParams();
                    int w = (int) getResources().getDimension(R.dimen.faceIdentify_success_width);
                    rlp.setMargins(
                            rect.centerX() - (w / 2),
                            rect.top - (w / 2),
                            0,
                            0);
                    binding.faceIdentifySuccess.setLayoutParams(rlp);
                    binding.faceIdentifySuccess.setTag("seLayout");
                }
                binding.faceIdentifySuccess.setVisibility(View.GONE);
                binding.faceIdentifySuccess.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        mHandler = new InnerHandler(this);
        mHandler.sendEmptyMessageDelayed(1, 500);
        mHandler.sendEmptyMessageDelayed(2, 500);
    }

    /**
     * 初始化faceSDK
     */
    private void initFaceSDK() {

        faceDetectManager.setOnFaceDetectListener(new FaceDetectManager.OnFaceDetectListener() {
            @Override
            public void onDetectFace(int status, FaceInfo[] infos, ImageFrame frame) {
                String str = "";
                if (status == 0) {
                    if (infos != null && infos[0] != null) {
                        FaceInfo info = infos[0];
                        boolean distance = false;
                        if (frame != null) {
                            if (info.mWidth >= (0.9 * frame.getWidth())) {
                                str = getResources().getString(R.string.detect_zoom_out);
                            } else if (info.mWidth <= 0.4 * frame.getWidth()) {
                                str = getResources().getString(R.string.detect_zoom_in);
                            } else {
                                distance = true;
                            }
                        }
                        boolean headUpDown;
                        if (info.headPose[0] >= ANGLE) {
                            headUpDown = false;
                            str = getResources().getString(R.string.detect_head_up);
                        } else if (info.headPose[0] <= -ANGLE) {
                            headUpDown = false;
                            str = getResources().getString(R.string.detect_head_down);
                        } else {
                            headUpDown = true;
                        }

                        boolean headLeftRight;
                        if (info.headPose[1] >= ANGLE) {
                            headLeftRight = false;
                            str = getResources().getString(R.string.detect_head_left);
                        } else if (info.headPose[1] <= -ANGLE) {
                            headLeftRight = false;
                            str = getResources().getString(R.string.detect_head_right);
                        } else {
                            headLeftRight = true;
                        }

                        mGoodDetect = distance && headUpDown && headLeftRight;

                    }
                } else if (status == 1) {
                    str = getResources().getString(R.string.detect_head_up);
                } else if (status == 2) {
                    str = getResources().getString(R.string.detect_head_down);
                } else if (status == 3) {
                    str = getResources().getString(R.string.detect_head_left);
                } else if (status == 4) {
                    str = getResources().getString(R.string.detect_head_right);
                } else if (status == 5) {
                    str = getResources().getString(R.string.detect_low_light);
                } else if (status == 6) {
                    str = getResources().getString(R.string.detect_face_in);
                } else if (status == 7) {
                    str = getResources().getString(R.string.detect_face_in);
                } else if (status == 10) {
                    str = getResources().getString(R.string.detect_keep);
                } else if (status == 11) {
                    str = getResources().getString(R.string.detect_occ_right_eye);
                } else if (status == 12) {
                    str = getResources().getString(R.string.detect_occ_left_eye);
                } else if (status == 13) {
                    str = getResources().getString(R.string.detect_occ_nose);
                } else if (status == 14) {
                    str = getResources().getString(R.string.detect_occ_mouth);
                } else if (status == 15) {
                    str = getResources().getString(R.string.detect_right_contour);
                } else if (status == 16) {
                    str = getResources().getString(R.string.detect_left_contour);
                } else if (status == 17) {
                    str = getResources().getString(R.string.detect_chin_contour);
                }

                boolean faceChanged = true;
                if (infos != null && infos[0] != null) {
                    Log.d("DetectLogin", "face id is:" + infos[0].face_id);
                    faceChanged = infos[0].face_id != mCurrentFaceId;
                    mCurrentFaceId = infos[0].face_id;
                }

                if (faceChanged) {
                    showProgressBar(false);
                    onRefreshSuccessView(false);
                }

                final int resultCode = status;
                if (!(mGoodDetect && status == 0)) {
                    if (faceChanged) {
                        showProgressBar(false);
                        onRefreshSuccessView(false);
                    }
                }

                if (status == 6 || status == 7 || status < 0) {
                    binding.faceIdentifyFaceRoundView.processDrawState(true);
                } else {
                    binding.faceIdentifyFaceRoundView.processDrawState(false);
                }

                mCurrentTips = str;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if ((System.currentTimeMillis() - mLastTipsTime) > 1000) {
                            binding.faceIdentifyNameView.setText(mCurrentTips);
                            mLastTipsTime = System.currentTimeMillis();
                        }
                        if (mGoodDetect && resultCode == 0) {
                            binding.faceIdentifyNameView.setText("");
                            onRefreshSuccessView(true);
                            showProgressBar(true);
                        }
                    }
                });

                if (infos == null) {
                    mGoodDetect = false;
                }
            }
        });
    }

    private void start() {

        Rect dRect = binding.faceIdentifyFaceRoundView.getFaceRoundRect();

        int preGap = getResources().getDimensionPixelOffset(R.dimen.faceIdentify_preview_margin);
        int w = getResources().getDimensionPixelOffset(R.dimen.faceIdentify_detect_out);

        int orientation = getResources().getConfiguration().orientation;
        boolean isPortrait = (orientation == Configuration.ORIENTATION_PORTRAIT);
        if (isPortrait) {
            // 检测区域矩形宽度
            int rWidth = mScreenWidth - 2 * preGap;
            // 圆框宽度
            int dRectW = dRect.width();
            // 检测矩形和圆框偏移
            int h = (rWidth - dRectW) / 2;
            int rRight = rWidth - w;
            int rTop = dRect.top - h - preGap + w;
            int rBottom = rTop + rWidth - w;

            RectF newDetectedRect = new RectF(w, rTop, rRight, rBottom);
            cropProcessor.setDetectedRect(newDetectedRect);
        } else {
            int rLeft = mScreenWidth / 2 - mScreenHeight / 2 + w;
            int rRight = mScreenWidth / 2 + mScreenHeight / 2 + w;
            int rTop = 0;
            int rBottom = mScreenHeight;

            RectF newDetectedRect = new RectF(rLeft, rTop, rRight, rBottom);
            cropProcessor.setDetectedRect(newDetectedRect);
        }


        faceDetectManager.startFaceIdentify();
        initWaveView(dRect);
    }

    /**
     * 人脸识别登录波浪纹
     */
    private void initWaveView(Rect rect) {

        RelativeLayout.LayoutParams waveParams = new RelativeLayout.LayoutParams(
                rect.width(), rect.height());

        waveParams.setMargins(rect.left, rect.top, rect.left, rect.top);
        waveParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        waveParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

        mWaveView = new WaveView(this);
        binding.faceIdentifyRootView.addView(mWaveView, waveParams);

        mWaveHelper = new WaveHelper(mWaveView);

        mWaveView.setShapeType(WaveView.ShapeType.CIRCLE);
        mWaveView.setWaveColor(
                Color.parseColor("#28ffffff"),
                Color.parseColor("#3cffffff"));

        int mBorderColor = Color.parseColor("#28f16d7a");
        mWaveView.setBorder(0, mBorderColor);
    }

    private void showProgressBar(final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (show) {
                    if (mWaveView != null) {
                        mWaveView.setVisibility(View.VISIBLE);
                        mWaveHelper.start();
                    }
                } else {
                    if (mWaveView != null) {
                        mWaveView.setVisibility(View.GONE);
                        mWaveHelper.cancel();
                    }
                }

            }
        });
    }


    /**
     * 检测成功
     */
    private void onRefreshSuccessView(final boolean isShow) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.faceIdentifySuccess.setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
            }
        });
    }

    private void visibleView() {
        binding.faceIdentifyCameraLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDetectStopped) {
            faceDetectManager.startFaceIdentify();
            mDetectStopped = false;
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        faceDetectManager.stop();
        mDetectStopped = true;
        onRefreshSuccessView(false);
        if (mWaveView != null) {
            mWaveView.setVisibility(View.GONE);
            mWaveHelper.cancel();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mWaveView != null) {
            mWaveHelper.cancel();
            mWaveView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    private static class InnerHandler extends Handler {
        private WeakReference<FaceIdentifyActivity> mWeakReference;

        public InnerHandler(FaceIdentifyActivity activity) {
            super();
            this.mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            if (mWeakReference == null || mWeakReference.get() == null) {
                return;
            }
            FaceIdentifyActivity activity = mWeakReference.get();
            if (activity == null) {
                return;
            }
            switch (msg.what) {
                case 1:
                    activity.visibleView();
                    break;
                case 2:
                    activity.mBeginDetect = true;
                    break;
                default:
                    break;
            }
        }
    }

}
