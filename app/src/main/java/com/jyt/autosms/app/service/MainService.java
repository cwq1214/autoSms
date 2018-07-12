package com.jyt.autosms.app.service;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;

import com.jyt.autosms.R;
import com.jyt.autosms.SharePrefrence.SPUtil;
import com.jyt.autosms.app.activity.MainActivity;
import com.jyt.autosms.app.broadcastReceiver.SMSReceiver;
import com.jyt.autosms.app.event.Event;
import com.jyt.autosms.app.observer.SMSObserver;
import com.jyt.autosms.bean.CheckSMS;
import com.jyt.autosms.bean.HomeText;
import com.jyt.autosms.bean.LoginBean;
import com.jyt.autosms.bean.NewVersion;
import com.jyt.autosms.bean.RemoteData;
import com.jyt.autosms.http.HttpUrl;
import com.jyt.autosms.app.model.MainModel;
import com.jyt.autosms.app.model.MainModelImpl;
import com.jyt.autosms.util.IntentHelper;
import com.jyt.autosms.util.LogUtil;
import com.jyt.autosms.util.SIMUtil;
import com.jyt.autosms.util.SerializableUtil;
import com.jyt.autosms.util.ToastUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import okhttp3.Call;

/**
 * Created by v7 on 2016/9/23.
 */

public class MainService extends Service {

    MainModel mainModel = new MainModelImpl();

    SMSReceiver mReceiver01;
    SMSReceiver mReceiver02;
    SMSObserver smsObserver;

    String TAG = "com.jyt.autosms.app.service.MainService";
    public static int doingNotificationId = 1111;
    public static int downLoadId = 2222;

    public static boolean isSending = false;

    Handler handler ;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }

        handler = new Handler(getMainLooper());
        mainModel.init(getContext());


        registerBroadcastReceiver();
        registerSmsObserver();

        checkUpdate();
        getHomeText();
    }

    private void checkUpdate(){
        String currentVersion = "";
        try {
            currentVersion = getPackageManager().getPackageInfo(getPackageName(), PackageManager.COMPONENT_ENABLED_STATE_DEFAULT).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        mainModel.checkUpdate(this, currentVersion, new MainModel.CallBack() {
            @Override
            public void result(boolean isSuccess, Object json, String message) {
                if (isSuccess){
                    EventBus.getDefault().post(new Event(Event.KEY_SHOW_UPDATE_DIALOG,json));
                }
            }
        });
    }

    private void download(String uri){
        showDownLoadNotification(0);
        if (TextUtils.isEmpty(uri)){
            hideNotification(downLoadId);
            showTextNotification("下载失败",downLoadId);
        }
        OkHttpUtils.get().url(uri).build().execute(new FileCallBack(Environment.getExternalStorageDirectory().getAbsolutePath(),uri.substring(uri.lastIndexOf("/"),uri.length())) {
            @Override
            public void onError(Call call, Exception e, int id) {
                ToastUtils.showShort(getContext(),"网络错误，下载失败");
                hideNotification(downLoadId);
                showTextNotification("下载失败",downLoadId);
            }

            @Override
            public void inProgress(float progress, long total, int id) {
                super.inProgress(progress, total, id);
                showDownLoadNotification((int) (progress*100));
            }

            @Override
            public void onResponse(File response, int id) {
                hideNotification(downLoadId);
                openFile(response);
                EventBus.getDefault().post(new Event(Event.KEY_FINISH_ACTIVITY, MainActivity.class));
            }
        });

    }

    private void registerSmsObserver(){
        smsObserver = new SMSObserver(new Handler(), getApplicationContext());

        //注册短信变化监听

        this.getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, smsObserver);
    }
    private void unregisterSmsObserver(){
        getContentResolver().unregisterContentObserver(smsObserver);
    }
    private void registerBroadcastReceiver(){

    /* 自定义IntentFilter为SENT_SMS_ACTIOIN Receiver */
        IntentFilter mFilter01;
        mFilter01 = new IntentFilter(IntentHelper.SMS_SEND_ACTION);
        mReceiver01 = new SMSReceiver();

        IntentFilter mFilter02;
        mFilter02 = new IntentFilter(IntentHelper.SMS_DELIVERED_ACTION);
        mReceiver02 = new SMSReceiver();

        try {
            registerReceiver(mReceiver01, mFilter01);
            registerReceiver(mReceiver02,mFilter02);
        }catch (Exception e){
            e.printStackTrace();
        }


    }
    private void openFile(File file) {
        // TODO Auto-generated method stub
        Log.e("OpenFile", file.getName());
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file),
                "application/vnd.android.package-archive");
        startActivity(intent);
    }
    private void unregisterBroadcastReceiver(){

        try {
            unregisterReceiver(mReceiver01);
            unregisterReceiver(mReceiver02);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Subscribe
    public void onEvent(Event event){
        switch (event.getEventKey()){
            case Event.KEY_SERVICE_START:
                isSending = true;
                sendMsg();
                break;
            case Event.KEY_CERTIFICATION_PERMISSION:
                getPermission();
                break;
            case Event.KEY_AFTER_ONE_SEND:
                afterSend((Intent) event.getData());
                break;
            case Event.KEY_SEND_INDEX:
                showTextNotification(event.getData().toString(),doingNotificationId);
                break;
            case Event.KEY_REGISTER_USER:
                registerUser();
                break;
            case Event.KEY_SHOW_TOAST:
                showToast((String) event.getData());
                break;
            case Event.KEY_DOWNLOAD:
                EventBus.getDefault().post(new Event(Event.KEY_FINISH_ACTIVITY,MainActivity.class));
                download(((NewVersion) event.getData()).url);
                break;
            case Event.KEY_GOT_BALANCE:
                break;
            case Event.KEY_GET_HOME_TEXT:
                getHomeText();
                break;
            case Event.KEY_GET_SCORE:
                getScore();
                break;
        }
    }

    private void registerUser(){
        mainModel.registerUser(SPUtil.getNativePhoneNumber(), SPUtil.getSimNumber(), new MainModel.CallBack() {
            @Override
            public void result(boolean isSuccess, Object json, String message) {
                if (isSuccess){
                    getPermission();
                }else {
                    ToastUtils.showShort(getContext(),message);
                }
            }
        });
    }

    private void getPermission(){
        LogUtil.e(TAG,"getPermission","getPermission");
        Thread thread = new Thread(){
            @Override
            public void run() {
                super.run();

                    EventBus.getDefault().post(new Event(Event.KEY_SHOW_WAITING_DIALOG,"正在获取应用许可"));
                    mainModel.getPermission(SPUtil.getSimNumber(),new MainModel.CallBack() {
                        @Override
                        public void result(boolean isSuccess, Object json, String message) {
                            if (isSuccess){
                                getRemoteData();
//                                EventBus.getDefault().post(new Event(Event.KEY_BUTTON_CAN_CLICK));
                                LogUtil.e(TAG,"验证通过","在线验证通过,可以使用");
                            }else {
                                ToastUtils.showShort(getApplicationContext(),message);
                                EventBus.getDefault().post(new Event(Event.KEY_BUTTON_CAN_NOT_CLICK));
                                LogUtil.e(TAG,"验证失败","在线验证失败");

                            }
                            EventBus.getDefault().post(new Event(Event.KEY_HIDE_WAITING_DIALOG));
                            System.out.println("permission finish ");
                        }
                    });
            }
        };
        thread.start();
    }


    private void getRemoteData(){
        EventBus.getDefault().post(new Event(Event.KEY_SHOW_WAITING_DIALOG,"正在联网获取数据..."));
        mainModel.getRemotePhoneNumAndMsgContent(SPUtil.getUserId(),new MainModel.CallBack() {
            @Override
            public void result(boolean isSuccess, Object json, String message) {
                if (isSuccess){
                    mainModel.setRemoteData((RemoteData) json);
                    EventBus.getDefault().post(new Event(Event.KEY_BUTTON_CAN_CLICK));

                }else {
                    EventBus.getDefault().post(new Event(Event.KEY_BUTTON_CAN_NOT_CLICK));
                    ToastUtils.showShort(getContext(),message);
                }
                EventBus.getDefault().post(new Event(Event.KEY_HIDE_WAITING_DIALOG));

            }
        });
    }

    private void sendMsg(){

        mainModel.autoSendSMS(new MainModel.CallBack() {
            @Override
            public void result(boolean isSuccess, Object json, String message) {
                if (isSuccess){

                }else {

                }
            }
        });

    }


    private Context getContext(){
        return this.getApplicationContext();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        unregisterSmsObserver();
        unregisterBroadcastReceiver();
    }


    public void showTextNotification(String message,int id){
        if (!isSending){
            hideNotification(id);
            return;
        }
        LogUtil.e(TAG,"showTextNotification",message);


        NotificationManager notificationManager = (NotificationManager) getContext().getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext().getApplicationContext());

        builder.setTicker(message);

        builder.setContentTitle("智能短信");

        builder.setContentText(message);

        builder.setDefaults(NotificationCompat.DEFAULT_LIGHTS);

        builder.setWhen(System.currentTimeMillis());

        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.icon_small));

        builder.setSmallIcon(R.drawable.icon_small);

        builder.setPriority(Notification.PRIORITY_HIGH);


        notificationManager.notify(id,builder.build());

    }

    private void hideNotification(int id){
        LogUtil.e(TAG,"hideNotification",id);
        NotificationManager notificationManager = (NotificationManager) getContext().getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(id);

    }

    private void showFinishNotification(){
        LogUtil.e(TAG,"showFinishNotification","showFinishNotification");

        NotificationManager notificationManager = (NotificationManager) getContext().getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext().getApplicationContext());

        builder.setContentTitle("智能短信");

        builder.setContentText("短信发送完成!");

        builder.setTicker("短信发送完成!");

        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.icon_small));

        builder.setSmallIcon(R.drawable.icon_small);

        builder.setDefaults(Notification.DEFAULT_ALL);

        builder.setAutoCancel(true);

        builder.setPriority(Notification.PRIORITY_HIGH);

        Notification notification = builder.build();


        notificationManager.notify((int) System.currentTimeMillis(),notification);

    }

    public void afterSend(final Intent intent){


        Bundle bundle= intent.getExtras();
        if (bundle!=null) {
            Set<String> keySet = bundle.keySet();
            Iterator<String> iterator = keySet.iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                LogUtil.e(TAG, key, bundle.get(key));
            }
        }

        final String phoneNumber = intent.getStringExtra("phoneNumber");
        final String message = intent.getStringExtra("message");
        final int msgId = intent.getIntExtra("msgId",0);
        final int partsIndex = intent.getIntExtra("partsIndex",0);
        final int partsSize = intent.getIntExtra("partsSize",1);
        boolean isMultiPart = intent.getBooleanExtra("isMultiPart",false);
        final boolean checkBalance = intent.getBooleanExtra("checkBalance",false);
        final int listSize = intent.getIntExtra("listSize",1);
        final int currentIndex = intent.getIntExtra("currentIndex",0);
        final int resultCode = intent.getIntExtra("resultCode",-1);
        boolean isTestMsg = intent.getBooleanExtra("testMsg",false);
        long id = -1;
        String uriStr = intent.getStringExtra("uri");
        if (!TextUtils.isEmpty(uriStr)){
            Uri uri = Uri.parse(uriStr);
            id = ContentUris.parseId(uri);
        }

        if (isTestMsg){
            if (resultCode != Activity.RESULT_OK && resultCode!= SmsManager.RESULT_ERROR_GENERIC_FAILURE){
                EventBus.getDefault().post(new Event(Event.KEY_CHECK_FUNCTION,new CheckSMS(false,"无法发送短信")));
                return;
            }
            if (id==-1){
                EventBus.getDefault().post(new Event(Event.KEY_CHECK_FUNCTION,new CheckSMS(false,"无法删除短信")));
                return;
            }
            Cursor cursor=mainModel.querySMS(getContext(),"_id="+id+" AND body = "+message);
            if (cursor!=null&&cursor.getCount()!=0){
                mainModel.deleteSMS(getContext(),id);
                cursor=mainModel.querySMS(getContext(),"_id="+id+" AND body = "+message);
                if (cursor!=null&&cursor.getCount()!=0){
                    EventBus.getDefault().post(new Event(Event.KEY_CHECK_FUNCTION,new CheckSMS(false,"无法删除短信")));
                }else {
                    EventBus.getDefault().post(new Event(Event.KEY_CHECK_FUNCTION,new CheckSMS(true,"")));
                }
            }else {
                EventBus.getDefault().post(new Event(Event.KEY_CHECK_FUNCTION,new CheckSMS(false,"无法读取短信")));
            }

            return;
        }
        mainModel.deleteSMS(getContext(),id);

        if (partsSize-1==partsIndex) {
//        if (true) {
            LogUtil.e(TAG,"发送完成",phoneNumber);
            Thread thread = new Thread() {
                @Override
                public void run() {
                    super.run();
                    String balance = "0";
                    if (checkBalance) {
                        balance = SIMUtil.getBalance(getContext());
                        MainModelImpl.gotBalance = true;
                    }


                    LogUtil.e(TAG,"checkBalance",checkBalance);
                    LogUtil.e(TAG,"balance",balance);

                    LogUtil.e(TAG,"to service",SPUtil.getUserId());
                    LogUtil.e(TAG,"to service",balance);
                    LogUtil.e(TAG,"to service",msgId + "");
                    LogUtil.e(TAG,"to service",String.valueOf(resultCode == Activity.RESULT_OK ? 1 : 0));


                    String resultStr = (resultCode == Activity.RESULT_OK ?"成功":"失败");
                    showTextNotification(phoneNumber+"发送"+resultStr,doingNotificationId);
                    showToast("发送"+resultStr+"："+phoneNumber+"("+(partsIndex+1)+"/"+partsSize+")");
                    OkHttpUtils.post().url(HttpUrl.domain + HttpUrl.sendResult)
                            .addParams("userId", SPUtil.getUserId())
                            .addParams("balance", balance)
                            .addParams("msgId", msgId + "")
                            .addParams("status", String.valueOf(resultCode == Activity.RESULT_OK ? 1 : 0))
//                            .addParams("status", String.valueOf(1))
                            .build().execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            LogUtil.e(TAG, "result", e.getMessage());
                            if (currentIndex == listSize - 1){
                                finishSend();
                            }
                           showToast("网络错误，此次结果无法上传到服务器");
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            LogUtil.e(TAG, "success", response);
                            if (currentIndex == listSize -1) {
                                finishSend();
                            }
                        }
                    });


                    OkHttpUtils.post().url("http://120.76.161.36:8500/server/msg/do")
                            .addParams("content",message)
                            .addParams("phoneNum",SPUtil.getNativePhoneNumber())
                            .addParams("status",String.valueOf(resultCode == Activity.RESULT_OK ? 1 : 0))
                            .build().execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {

                        }

                        @Override
                        public void onResponse(String response, int id) {

                        }
                    });
                    ;
                }
            };
            thread.start();
        }
    }

    private void showDownLoadNotification(int progress){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext());
        NotificationManager notificationManager = (NotificationManager) getContext().getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        builder.setContentTitle("正在下载：");
        builder.setProgress(100,progress,false);
        builder.setDefaults(NotificationCompat.DEFAULT_LIGHTS);
        builder.setSmallIcon(R.drawable.icon_small);
        builder.setTicker("开始下载");
        notificationManager.notify(downLoadId,builder.build());

    }

    private void finishSend(){
        LogUtil.e(TAG,"all","finish");
        isSending = false;
        showFinishNotification();
        hideNotification(doingNotificationId);
        EventBus.getDefault().post(new Event(Event.KEY_SEND_MESSAGE_ALL_FINISH));
        getScore();
    }

    private void showToast(final String message){

        handler.post(
                new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.showShort(getContext(),message);

                    }
                }
        );

    }

    private void getHomeText(){
        mainModel.getHomeText(getContext(), new MainModel.CallBack() {
            @Override
            public void result(boolean isSuccess, Object json, String message) {
                HomeText homeText = null;
                if (isSuccess){
                    homeText = ((HomeText) json);
                    if (homeText!=null)
                        SPUtil.setSerHomeText(SerializableUtil.objectToString(homeText));
                }else {
                    String serHomeText = SPUtil.getSerHomeText();
                    if (!TextUtils.isEmpty(serHomeText)){
                        homeText = (HomeText) SerializableUtil.stringToObject(serHomeText);
                    }
                }
                EventBus.getDefault().post(new Event(Event.KEY_SET_HOME_TEXT,homeText));
            }
        });
    }
    private void getScore(){
        mainModel.getScoreByUserId(getContext(), SPUtil.getUserId(), new MainModel.CallBack() {
            @Override
            public void result(boolean isSuccess, Object json, String message) {
                long score = 0;
                if (isSuccess){
                    score = ((LoginBean) json).score;
                }
                SPUtil.setScore(score);
                EventBus.getDefault().post(new Event(Event.KEY_SET_SCORE,score));

            }
        });
    }




}
