package com.cin.mylibrary.http;

public class ApiException extends RuntimeException {

    public int errorCode;
    public String message;
    public ApiException(int code, String message) {
        super(message);
        errorCode = code;
        this.message = message;
    }
}