package com.jyt.autosms.http;

/**
 * Created by v7 on 2016/9/23.
 */

public class HttpUrl {
    public static String domain = "http://121.201.29.123:8080";
//    public static String domain = "http://120.76.161.36:8080";

    //userPhone
    public static String getPermission = "/server/user/check";

    ///server/msg/get?userId=1
    public static String getPhoneNumAndMsgContent = "/server/msg/get";

    //msgId,state(0：失败；1：成功；2发送中；) balance

    public static String sendResult = "/server/msg/state";

    //sim、userPhone
    public static String register = "/server/user/register";

    public static String checkUpdate = "/server/version/now";

    public static String getHomeText = "/server/home/get";

    public static String getScore = "/server/score/get?userId=";

}
