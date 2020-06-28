package com.cin.mylibrary.bean;

import lombok.Data;

/**
 * Created by 王新超 on 2020/6/28.
 */
@Data
public class BaseResponseBean<T> {
    private int code;
    private String message;
    private T data;
}
