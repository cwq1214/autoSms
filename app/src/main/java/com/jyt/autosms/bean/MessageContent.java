package com.jyt.autosms.bean;

/**
 * Created by v7 on 2016/9/23.
 */

public class MessageContent {
    public String content;
    public int msgId;
    public String phoneNum;
    public String balance;


    //0 失败
    //1 成功
    //2 发送中
    //-1 未操作
    public int status=-1;
}
