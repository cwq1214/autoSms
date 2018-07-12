package com.jyt.autosms.bean;

/**
 * Created by V7 on 2016/9/29.
 */

public class BaseJson<T> {
    public String ret;
    public String forWorker;
    public String forUser;
    public String version;
    public int code;
    public T data;
}
