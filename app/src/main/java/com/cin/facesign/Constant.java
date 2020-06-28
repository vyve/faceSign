package com.cin.facesign;

import java.io.File;

/**
 * Created by 王新超 on 2020/6/15.
 */
public class Constant {
    /**
     * 注册时缓存的头像路径
     */
    public static final String FACE_IDENTIFY_LOCAL_PATH = MyApp.getInstance().getFilesDir() + File.separator + "head_tmp.jpg";

    public static final String VIDEO_RECORD_PATH = MyApp.getInstance().getFilesDir() + File.separator + "recordVideo.mp4";

    /**
     * 签名文件保存路径
     */
    public static final String SIGN_PATH = MyApp.getInstance().getFilesDir() + File.separator + "signature.jpg";

    public static final String isLogin = "isLogin";
}
