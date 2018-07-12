package com.jyt.autosms.app.model;

import android.content.Context;
import android.database.Cursor;

import com.jyt.autosms.bean.RemoteData;

/**
 * Created by v7 on 2016/9/23.
 */

public interface MainModel {
    void init(Context context);

    void destroy();

    void getPermission(String simNumber,CallBack callBack);

    void getRemotePhoneNumAndMsgContent(String userId,CallBack callBack);


    void autoSendSMS(CallBack callBack);

    void setRemoteData(RemoteData remoteData);

    void sendToRemoteService(CallBack callBack);

    void registerUser(String phoneNumber,String simNumber,CallBack callBack);

    void deleteSMS(Context context,long smsId);

    Cursor querySMS(Context context, String selection);

    void checkUpdate(Context context, String currentVersion, CallBack callBack);

    void getHomeText(Context context , CallBack callBack);

    void getScoreByUserId(Context context,String userId ,CallBack callBack);

    interface CallBack{
        void result(boolean isSuccess,Object json,String message);
    }
}
