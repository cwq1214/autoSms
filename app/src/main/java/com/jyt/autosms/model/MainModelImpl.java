package com.jyt.autosms.model;

import android.content.Context;

import com.jyt.autosms.http.HttpUrl;
import com.jyt.autosms.util.SIMUtil;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import okhttp3.Call;

/**
 * Created by v7 on 2016/9/23.
 */

public class MainModelImpl implements MainModel {
    String phoneNumKey = "";

    Context context;

    @Override
    public void init(Context context) {
        this.context = context;
    }

    @Override
    public void destroy() {

    }

    @Override
    public void getPermission(final CallBack callBack) {

        callBack.result(true,null,null);
        return;
//        OkHttpUtils.post().url(HttpUrl.domain+HttpUrl.getPermission).addParams(phoneNumKey,SIMUtil.getNativePhoneNumber(context)).build().execute(new StringCallback() {
//            @Override
//            public void onError(Call call, Exception e, int id) {
//
//                callBack.result(false,null,"网络错误");
//            }
//
//            @Override
//            public void onResponse(String response, int id) {
//
//                callBack.result(true,null,null);
//            }
//        });
    }

    @Override
    public void getRemotePhoneNumAndMsgContent(CallBack callBack) {

    }

    @Override
    public void getRemoteConfig(CallBack callBack) {

    }

    @Override
    public void savePhoneNum() {

    }

    @Override
    public void saveMessageContent() {

    }

    @Override
    public void AutoSendSMS(CallBack callBack) {

    }

    @Override
    public void saveResultPhoneNum() {

    }

    @Override
    public void saveSelfBalance() {

    }

    @Override
    public void sendToRemoteService(CallBack callBack) {

    }

    @Override
    public void playMusic() {

    }
}
