package com.jyt.autosms.app.event;

import android.content.Intent;

/**
 * Created by v7 on 2016/9/23.
 */

public class Event {
    public static final int KEY_CERTIFICATION_PERMISSION = 0;
    public static final int KEY_SHOW_WAITING_DIALOG = 1;
    public static final int KEY_HIDE_WAITING_DIALOG = 2;
    public static final int KEY_SERVICE_START = 3;
    public static final int KEY_UNREGISTER_OBSERVER = 4;
    public static final int KEY_GOT_PHONE_NUMBER = 5;
    public static final int KEY_GOT_BALANCE = 6;
    public static final int KEY_SEND_MESSAGE_SUCCESS = 7;
    public static final int KEY_SEND_MESSAGE_FAIL = 8 ;
    public static final int KEY_SEND_MESSAGE_AGAIN = 9;
    public static final int KEY_SEND_RESULT = 10;
    public static final int KEY_SEND_MESSAGE_ALL_FINISH = 11;
    public static final int KEY_SEND_INDEX = 12;
    public static final int KEY_BUTTON_CAN_CLICK = 13;
    public static final int KEY_BUTTON_CAN_NOT_CLICK = 14;
    public static final int KEY_AFTER_ONE_SEND = 15;
    public static final int KEY_REGISTER_USER = 16;
    public static final int KEY_SHOW_UPDATE_DIALOG = 17;
    public static final int KEY_DOWNLOAD = 18;
    public static final int KEY_SHOW_TOAST = 19;
    public static final int KEY_CHECK_FUNCTION = 20;
    public static final int KEY_FINISH_ACTIVITY = 21;
    public static final int KEY_SHOW_INPUT_DIALOG = 22;
    public static final int KEY_CHANGE_BTN_TEXT = 23;
    public static final int KEY_SET_SCORE = 24;
    public static final int KEY_SET_HOME_TEXT = 25;
    public static final int KEY_GET_SCORE =26;
    public static final int KEY_GET_HOME_TEXT = 27;


    int eventKey;
    Object data;

    public Event(int eventKey) {
        this.eventKey = eventKey;
    }

    public Event(int eventKey, Object data) {
        this.eventKey = eventKey;
        this.data = data;
    }

    public int getEventKey() {
        return eventKey;
    }

    public void setEventKey(int eventKey) {
        this.eventKey = eventKey;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
