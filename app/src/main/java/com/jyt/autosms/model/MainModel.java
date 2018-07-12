package com.jyt.autosms.model;

import android.content.Context;

/**
 * Created by v7 on 2016/9/23.
 */

public interface MainModel {
    void init(Context context);

    void destroy();

    void getPermission(CallBack callBack);

    void getRemotePhoneNumAndMsgContent(CallBack callBack);

    void getRemoteConfig(CallBack callBack);

    void savePhoneNum();

    void saveMessageContent();

    void AutoSendSMS(CallBack callBack);

    void saveResultPhoneNum();

    void saveSelfBalance();

    void sendToRemoteService(CallBack callBack);

    void playMusic();


    interface CallBack{
        void result(boolean isSuccess,Object json,String message);
    }
}
