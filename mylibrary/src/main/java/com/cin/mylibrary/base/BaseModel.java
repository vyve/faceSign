package com.cin.mylibrary.base;

import lombok.Data;

/**
 * Created by 王新超 on 2020/3/27.
 */
@Data
public class BaseModel<T> {
    private boolean success;
    private String errorMsg;
    private T model;
}
