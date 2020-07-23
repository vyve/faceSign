package com.cin.mylibrary.request_bean;

import lombok.Data;

/**
 * 人脸信息对比
 * Created by 王新超 on 2020/7/18.
 */
@Data
public class CheckFaceInfoRequestBean {
    private String idCardImage;
    private String image;
}
