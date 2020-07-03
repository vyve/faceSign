package com.cin.mylibrary.request_bean;

import lombok.Data;

/**
 * Created by 王新超 on 2020/7/2.
 */
@Data
public class RegisterRequestBean {
    private String identityCard;
    private String image;
    private String name;
    private String password;
    private String phone;
    private String sex;
    private String userName;
}
