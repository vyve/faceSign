package com.cin.mylibrary.request_bean;

import lombok.Data;

/**
 * Created by 王新超 on 2020/6/15.
 */
@Data
public class RegisterFaceBean {
    private String image;
    private String group_id;
    private String user_id;
    private String user_info;
    private String liveness_control;
    private String image_type;
    private String quality_control;
}
