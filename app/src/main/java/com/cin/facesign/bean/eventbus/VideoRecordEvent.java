package com.cin.facesign.bean.eventbus;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by 王新超 on 2020/8/17.
 */
@Data
@AllArgsConstructor
public class VideoRecordEvent {
    private boolean needGetFrame;
}
