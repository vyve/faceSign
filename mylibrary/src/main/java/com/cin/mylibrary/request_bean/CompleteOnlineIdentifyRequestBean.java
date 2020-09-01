package com.cin.mylibrary.request_bean;

import java.util.List;

import lombok.Data;

/**
 * 提交在线认证数据入参
 * Created by 王新超 on 2020/7/18.
 */
@Data
public class CompleteOnlineIdentifyRequestBean {
    private String data;
    private String video;
    /**
     * 签名文件路径
     */
    private String signFile;
    private List<String> videos;
}
