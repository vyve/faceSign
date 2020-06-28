package com.cin.facesign.utils;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 语音合成服务 相关鉴权信息
 * Created by 王新超 on 2020/6/16.
 */
public class SpeechSynthesisAuthMessage {
    private static volatile SpeechSynthesisAuthMessage instance;
    private String appId;
    private String appKey;
    private String secretKey;
    private String sn;

    public static SpeechSynthesisAuthMessage getInstance(Context context) {
        if (instance == null) {
            synchronized (SpeechSynthesisAuthMessage.class) {
                instance = new SpeechSynthesisAuthMessage(context);
            }
        }
        return instance;
    }

    private SpeechSynthesisAuthMessage(Context context) {
        Properties properties = null;
        try {
            InputStream is = context.getAssets().open("auth.properties");
            properties = new Properties();
            properties.load(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (properties != null) {
            String applicationId = properties.getProperty("applicationId");
            appId = properties.getProperty("appId");
            appKey = properties.getProperty("appKey");
            secretKey = properties.getProperty("secretKey");
            sn = properties.getProperty("sn"); //  收费纯离线版本需要序列号，免费离在线版本不需要
        }
    }

    public String getAppId() {
        return appId == null ? "" : appId;
    }

    public String getAppKey() {
        return appKey == null ? "" : appKey;
    }

    public String getSecretKey() {
        return secretKey == null ? "" : secretKey;
    }

    public String getSn() {
        return sn == null ? "" : sn;
    }
}
