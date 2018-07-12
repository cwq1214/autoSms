package com.jyt.autosms.bean;

/**
 * Created by v7 on 2016/10/21.
 */

public class CheckSMS {
    public boolean isSuccess;
    public String message;

    public CheckSMS(boolean isSuccess, String message) {
        this.isSuccess = isSuccess;
        this.message = message;
    }
}
