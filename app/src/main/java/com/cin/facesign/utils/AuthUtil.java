package com.cin.facesign.utils;

import com.blankj.utilcode.util.SPUtils;
import com.cin.facesign.Constant;

/**
 * Created by 王新超 on 2020/7/18.
 */
public class AuthUtil {
    public static String getOSSAccessKeyId(){
        return SPUtils.getInstance().getString(Constant.ossAccessKeyId);
    }
    public static String getOSSAccessKeySecret(){
        return SPUtils.getInstance().getString(Constant.ossAccessKeySecret);
    }
    public static String getOSSSecurityToken(){
        return SPUtils.getInstance().getString(Constant.ossSecurityToken);
    }

    public static String getOSSEndPoint(){
        return "oss-cn-beijing.aliyuncs.com";
    }

    public static String getBucketName(){
        return "matrixsci";
    }

    public static void setOSSAccessKeyId(String sSSAccessKeyId){
         SPUtils.getInstance().put(Constant.ossAccessKeyId,sSSAccessKeyId);
    }
    public static void setOSSAccessKeySecret(String oSSAccessKeySecret){
         SPUtils.getInstance().put(Constant.ossAccessKeySecret,oSSAccessKeySecret);
    }
    public static void setOSSSecurityToken(String oSSSecurityToken){
        SPUtils.getInstance().put(Constant.ossSecurityToken,oSSSecurityToken);
    }

    /**
     * 获取用户ID
     */
    public static int getUserId(){
        return SPUtils.getInstance().getInt(Constant.userId);
    }
}
