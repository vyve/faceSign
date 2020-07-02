package com.cin.facesign.control;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Pair;

import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizeBag;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.blankj.utilcode.util.LogUtils;
import com.cin.facesign.utils.SpeechSynthesisAuthMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 该类是对SpeechSynthesizer的封装
 */

public class FaceSignSynthesizer {

    protected SpeechSynthesizer mSpeechSynthesizer;
    protected Context context;
    protected Handler mainHandler;

    private static final String TAG = "NonBlockSyntherizer";

    protected static volatile boolean isInitied = false;
    public static  final int SPEECH_SYNTHESIS_INIT_SUCCESS = 2;
    private OnSpeakListener listener;

    public interface OnSpeakListener{
        void onSpeakFinish();
    }

    public void setListener(OnSpeakListener listener) {
        this.listener = listener;
    }

    public FaceSignSynthesizer(Context context, Handler mainHandler) {
        if (isInitied) {
            throw new RuntimeException("MySynthesizer 对象里面 SpeechSynthesizer还未释放，请勿新建一个新对象。" +
                    "如果需要新建，请先调用之前MySynthesizer对象的release()方法。");
        }
        this.context = context;
        this.mainHandler = mainHandler;
        isInitied = true;
        init();
    }

    /**
     * 注意该方法需要在新线程中调用。且该线程不能结束。详细请参见NonBlockSyntherizer的实现
     */
    protected void init() {

//        sendToUiThread("初始化开始");
        mSpeechSynthesizer = SpeechSynthesizer.getInstance();

        mSpeechSynthesizer.setContext(context);

//        SpeechSynthesizerListener listener = config.getListener();

        // listener = new SwitchSpeakerListener(mainHandler,context,this); // 测试播放过程中切换发音人逻辑
        mSpeechSynthesizer.setSpeechSynthesizerListener(new SpeechSynthesizerListener() {
            @Override
            public void onSynthesizeStart(String s) {

            }

            @Override
            public void onSynthesizeDataArrived(String s, byte[] bytes, int i, int i1) {

            }

            @Override
            public void onSynthesizeFinish(String s) {

            }

            @Override
            public void onSpeechStart(String s) {

            }

            @Override
            public void onSpeechProgressChanged(String s, int i) {

            }

            @Override
            public void onSpeechFinish(String s) {
                if (listener!=null){
                    listener.onSpeakFinish();
                }
            }

            @Override
            public void onError(String s, SpeechError speechError) {

            }
        });


        // 请替换为语音开发者平台上注册应用得到的App ID ,AppKey ，Secret Key ，填写在SynthActivity的开始位置
        mSpeechSynthesizer.setAppId(SpeechSynthesisAuthMessage.getInstance(context).getAppId());
        mSpeechSynthesizer.setApiKey(SpeechSynthesisAuthMessage.getInstance(context).getAppKey(),
                SpeechSynthesisAuthMessage.getInstance(context).getSecretKey());

        setParams(getParams());
        // 初始化tts
        int result = mSpeechSynthesizer.initTts(TtsMode.MIX);
        if (result != 0) {
            LogUtils.e("initTts 初始化失败 + errorCode："+result);
        }

        // 设置播放的音频流类型，具体参数和组合见AudioAttributes,https://source.android.google.cn/devices/audio/attributes
        // mSpeechSynthesizer.setAudioAttributes(AudioAttributes.USAGE_MEDIA,AudioAttributes.CONTENT_TYPE_MUSIC);

        // 此时可以调用 speak和synthesize方法
        sendToUiThread(SPEECH_SYNTHESIS_INIT_SUCCESS, "合成引擎初始化成功");

    }

    /**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     *
     * @return 合成参数Map
     */
    protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        // 以下参数均为选填
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>, 其它发音人见文档
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置合成的音量，0-15 ，默认 5
        params.put(SpeechSynthesizer.PARAM_VOLUME, "15");
        // 设置合成的语速，0-15 ，默认 5
        params.put(SpeechSynthesizer.PARAM_SPEED, "5");
        // 设置合成的语调，0-15 ，默认 5
        params.put(SpeechSynthesizer.PARAM_PITCH, "5");

//        if (!isOnlineSDK) {
//            // 免费的在线SDK版本没有此参数。
//
//            /*
//            params.put(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
//            // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
//            // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
//            // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
//            // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
//            // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
//            // params.put(SpeechSynthesizer.PARAM_MIX_MODE_TIMEOUT, SpeechSynthesizer.PARAM_MIX_TIMEOUT_TWO_SECOND);
//            // 离在线模式，强制在线优先。在线请求后超时2秒后，转为离线合成。
//            */
//            // 离线资源文件， 从assets目录中复制到临时目录，需要在initTTs方法前完成
//            OfflineResource offlineResource = createOfflineResource(offlineVoice);
//            // 声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
//            params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFilename());
//            params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, offlineResource.getModelFilename());
//        }
        return params;
    }

    /**
     * 合成并播放
     *
     * @param text 小于1024 GBK字节，即512个汉字或者字母数字
     * @return =0表示成功
     */
    public int speak(String text) {
        if (!isInitied) {
            throw new RuntimeException("TTS 还未初始化");
        }
        Log.i(TAG, "speak text:" + text);
        return mSpeechSynthesizer.speak(text);
    }

    /**
     * 合成并播放
     *
     * @param text        小于1024 GBK字节，即512个汉字或者字母数字
     * @param utteranceId 用于listener的回调，默认"0"
     * @return =0表示成功
     */
    public int speak(String text, String utteranceId) {
        if (!isInitied) {
            throw new RuntimeException("TTS 还未初始化");
        }
        return mSpeechSynthesizer.speak(text, utteranceId);
    }

    /**
     * 只合成不播放
     *
     * @param text 合成的文本
     * @return =0表示成功
     */
    public int synthesize(String text) {
        if (!isInitied) {
            // SpeechSynthesizer.getInstance() 不要连续调用
            throw new RuntimeException("TTS 还未初始化");
        }
        return mSpeechSynthesizer.synthesize(text);
    }

    public int synthesize(String text, String utteranceId) {
        if (!isInitied) {
            // SpeechSynthesizer.getInstance() 不要连续调用
            throw new RuntimeException("TTS 还未初始化");
        }
        return mSpeechSynthesizer.synthesize(text, utteranceId);
    }

    public int batchSpeak(List<Pair<String, String>> texts) {
        if (!isInitied) {
            throw new RuntimeException("TTS 还未初始化");
        }
        List<SpeechSynthesizeBag> bags = new ArrayList<SpeechSynthesizeBag>();
        for (Pair<String, String> pair : texts) {
            SpeechSynthesizeBag speechSynthesizeBag = new SpeechSynthesizeBag();
            speechSynthesizeBag.setText(pair.first);
            if (pair.second != null) {
                speechSynthesizeBag.setUtteranceId(pair.second);
            }
            bags.add(speechSynthesizeBag);

        }
        return mSpeechSynthesizer.batchSpeak(bags);
    }

    public void setParams(Map<String, String> params) {
        if (params != null) {
            for (Map.Entry<String, String> e : params.entrySet()) {
                mSpeechSynthesizer.setParam(e.getKey(), e.getValue());
            }
        }
    }

    public int pause() {
        return mSpeechSynthesizer.pause();
    }

    public int resume() {
        return mSpeechSynthesizer.resume();
    }

    public int stop() {
        return mSpeechSynthesizer.stop();
    }

    /**
     * 引擎在合成时该方法不能调用！！！
     * 注意 只有 TtsMode.MIX 才可以切换离线发音
     */
    public int loadModel(String modelFilename, String textFilename) {
        int res = mSpeechSynthesizer.loadModel(modelFilename, textFilename);
//        sendToUiThread("切换离线发音人成功。");
        return res;
    }

    /**
     * 设置播放音量，默认已经是最大声音
     * 0.0f为最小音量，1.0f为最大音量
     *
     * @param leftVolume  [0-1] 默认1.0f
     * @param rightVolume [0-1] 默认1.0f
     */
    public void setStereoVolume(float leftVolume, float rightVolume) {
        mSpeechSynthesizer.setStereoVolume(leftVolume, rightVolume);
    }

    public void release() {
        Log.i("MySyntherizer", "MySyntherizer release called");
        if (!isInitied) {
            // 这里报错是因为连续两次 new MySyntherizer。
            // 必须第一次new 之后，调用release方法
            throw new RuntimeException("TTS 还未初始化");
        }
        mSpeechSynthesizer.stop();
        mSpeechSynthesizer.release();
        mSpeechSynthesizer = null;
        isInitied = false;
    }
    protected void sendToUiThread(int action, String message) {
        Log.i(TAG, message);
        if (mainHandler == null) { // 可以不依赖mainHandler
            return;
        }
        Message msg = Message.obtain();
        msg.what = action;
        msg.obj = message + "\n";
        mainHandler.sendMessage(msg);
    }
}
