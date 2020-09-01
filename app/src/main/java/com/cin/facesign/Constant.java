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
    /**
     * 在线面签保存的识别的头像路径
     */
    public static final String FACE_PATH = FILE_SAVE_PATH + "face_head.jpg";
    /**
     * 识别到的身份证保存路径
     */
    public static final String ID_CARD_PATH = FILE_SAVE_PATH + "face_id_card.jpg";

    /**
     * 视频录制保存的路径1
     */
    public static final String VIDEO_RECORD_PATH1 = FILE_SAVE_PATH + "videoRecord1.mp4";
    /**
     * 视频录制保存的路径2
     */
    public static final String VIDEO_RECORD_PATH2 = FILE_SAVE_PATH + "videoRecord2.mp4";
    /**
     * 屏幕录制保存的路径
     */
    public static final String SCREEN_RECORD_PATH = FILE_SAVE_PATH + "screenRecord.mp4";

    /**
     * 屏幕录制帧保存路径
     */
    public static final String VIDEO_RECORD_FRAME = FILE_SAVE_PATH + "screenRecord_frame.jpg";

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

    public static final String ossAccessKeyId = "ossAccessKeyId";
    public static final String ossAccessKeySecret = "ossAccessKeySecret";
    public static final String ossSecurityToken = "ossSecurityToken";
}
