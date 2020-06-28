package com.cin.mylibrary.bean;

import lombok.Data;

/**
 * faceSDK token
 */
@Data
public class FaceSDKToken {

//    {
//        "refresh_token": "25.b55fe1d287227ca97aab219bb249b8ab.315360000.1798284651.282335-8574074",
//            "expires_in": 2592000,
//            "scope": "public wise_adapt",
//            "session_key": "9mzdDZXu3dENdFZQurfg0Vz8slgSgvvOAUebNFzyzcpQ5EnbxbF+hfG9DQkpUVQdh4p6HbQcAiz5RmuBAja1JJGgIdJI",
//            "access_token": "24.6c5e1ff107f0e8bcef8c46d3424a0e78.2592000.1485516651.282335-8574074",
//            "session_secret": "dfac94a3489fe9fca7c3221cbf7525ff"
//    }

    private String access_token;

    private int expires_in;

    private String session_key;

    private String session_secret;
}

