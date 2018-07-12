package com.jyt.autosms.app;

import android.app.Application;
import android.content.Intent;

import com.jyt.autosms.app.service.MainService;
import com.netease.nis.bugrpt.CrashHandler;
import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.HawkBuilder;
import com.orhanobut.hawk.LogLevel;
import com.orhanobut.hawk.Storage;
import com.zhy.http.okhttp.OkHttpUtils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by v7 on 2016/9/23.
 */

public class MApplication extends Application {

    long timeout = 5;
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.init(getApplicationContext());
        initSp();

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);


        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.writeTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout,TimeUnit.SECONDS)
                .connectTimeout(timeout,TimeUnit.SECONDS)
        .addNetworkInterceptor(httpLoggingInterceptor)
        ;


        OkHttpUtils.initClient(builder.build());

        startService(new Intent(this,MainService.class));

    }

    private void initSp(){
        Hawk.init(this).setPassword("2333").setLogLevel(LogLevel.FULL).setStorage(HawkBuilder.newSharedPrefStorage(this)).setEncryptionMethod(HawkBuilder.EncryptionMethod.MEDIUM).build();
    }
}
