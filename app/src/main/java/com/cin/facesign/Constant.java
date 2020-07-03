package com.cin.facesign;

import java.io.File;

/**
 * Created by 王新超 on 2020/6/15.
 */
public class Constant {

    public static final String FILE_SAVE_PATH = MyApp.getInstance().getFilesDir() + File.separator;
    /**
     * 注册时缓存的头像路径
     */
    public static final String FACE_IDENTIFY_LOCAL_PATH = FILE_SAVE_PATH + "head_tmp.jpg";

    public static final String VIDEO_RECORD_PATH = FILE_SAVE_PATH + "recordVideo.mp4";

    /**
     * 签名文件保存路径
     */
    public static final String SIGN_PATH = FILE_SAVE_PATH+ "signature.jpg";
    /**
     * 用户是否登录了app
     * boolean
     */
    public static final String isLogin = "isLogin";
    /**
     * 用户登录保存的userId
     * int
     */
    public static final String userId = "userId";
    /**
     * 保存的用户昵称
     * String
     */
    public static final String username = "username";
}
