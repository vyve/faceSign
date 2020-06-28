package com.cin.mylibrary.bean;

import lombok.Data;

/**
 * Created by 王新超 on 2020/6/16.
 */
@Data
public class FaceSDKRegisterBean {

    /**
     * error_code : 0
     * error_msg : SUCCESS
     * log_id : 2525252589750
     * timestamp : 1592271887
     * cached : 0
     * result : {"face_token":"d8c07c01981d45d5116f3f79822f595a","location":{"left":125.58,"top":210.76,"width":465,"height":474,"rotation":0}}
     */

    private int error_code;
    private String error_msg;
    private long log_id;
    private int timestamp;
    private int cached;
    private ResultBean result;

    @Data
    public static class ResultBean {
        /**
         * face_token : d8c07c01981d45d5116f3f79822f595a
         * location : {"left":125.58,"top":210.76,"width":465,"height":474,"rotation":0}
         */

        private String face_token;
        private LocationBean location;

        @Data
        public static class LocationBean {
            /**
             * left : 125.58
             * top : 210.76
             * width : 465
             * height : 474
             * rotation : 0
             */

            private double left;
            private double top;
            private int width;
            private int height;
            private int rotation;

        }
    }
}
