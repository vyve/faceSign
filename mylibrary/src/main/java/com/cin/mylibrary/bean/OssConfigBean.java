package com.cin.mylibrary.bean;

import lombok.Data;

/**
 * Created by 王新超 on 2020/7/18.
 */
@Data
public class OssConfigBean {
    private String appKey;
    private String appSecret;
    private String expiration;
    private String securityToken;
}
