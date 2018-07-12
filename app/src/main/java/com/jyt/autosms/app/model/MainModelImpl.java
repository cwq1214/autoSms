package com.jyt.autosms.app.model;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jyt.autosms.SharePrefrence.SPUtil;
import com.jyt.autosms.app.broadcastReceiver.SMSReceiver;
import com.jyt.autosms.app.event.Event;
import com.jyt.autosms.bean.BaseJson;
import com.jyt.autosms.bean.Config;
import com.jyt.autosms.bean.HomeText;
import com.jyt.autosms.bean.LoginBean;
import com.jyt.autosms.bean.MessageContent;
import com.jyt.autosms.bean.NewVersion;
import com.jyt.autosms.bean.RemoteData;
import com.jyt.autosms.http.HttpUrl;
import com.jyt.autosms.util.IntentHelper;
import com.jyt.autosms.util.LogUtil;
import com.jyt.autosms.util.SIMUtil;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;

/**
 * Created by v7 on 2016/9/23.
 */

public class MainModelImpl implements MainModel {
    String TAG = getClass().getSimpleName();

    String phoneNumKey = "userPhone";
    String userIdKey = "userId";
    String statusKey = "status";
    String balanceKey = "balance";
    String resultKey = "msgData";
    String simNumberKey = "sim";

    Context context;

    Config config;

    ArrayList<MessageContent> messageContents;

    public static boolean gotBalance = false;

    public static List<MessageContent> syncList = Collections.synchronizedList(new ArrayList());

    public static AtomicInteger callBackTimes = new AtomicInteger(0);
    public static Map<String, String> syncMap = Collections.synchronizedMap(new HashMap<String, String>());

    public static boolean canNext = false;

    private boolean testMode = false;

    @Override
    public void init(Context context) {
        this.context = context;
            LogUtil.e(TAG,"测试模式",testMode);
    }

    @Override
    public void destroy() {

    }

    @Override
    public void getPermission(String simNumber, final CallBack callBack) {
        LogUtil.e(TAG, "获取应用权限", "getPermission");
        OkHttpUtils.post().url(HttpUrl.domain + HttpUrl.getPermission).addParams(simNumberKey, simNumber).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                callBack.result(false, null, "网络错误，获取应用权限失败");
            }

            @Override
            public void onResponse(String response, int id) {
                BaseJson<LoginBean> baseJson = new Gson().fromJson(response, new TypeToken<BaseJson<LoginBean>>() {
                }.getType());
                if (baseJson.ret.equals("success")) {
                    LogUtil.e(TAG, "userId", baseJson.data.userId);
                    SPUtil.setUserId(baseJson.data.userId + "");
                    callBack.result(true, null, null);
                    EventBus.getDefault().post(new Event(Event.KEY_SET_SCORE, baseJson.data.score));
                } else {
                    callBack.result(false, null, baseJson.forUser);
                    if (baseJson.code == 1200) {//无此用户
                        EventBus.getDefault().post(new Event(Event.KEY_SHOW_INPUT_DIALOG));
                    } else if (baseJson.code == 1204) {//用户被禁用
                        EventBus.getDefault().post(new Event(Event.KEY_CHANGE_BTN_TEXT, "等待激活"));
                    }
                }
            }
        });
    }

    @Override
    public void getRemotePhoneNumAndMsgContent(String userId, final CallBack callBack) {
        OkHttpUtils.get().url(HttpUrl.domain + HttpUrl.getPhoneNumAndMsgContent + "?" + userIdKey + "=" + userId).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                callBack.result(false, null, "网络错误,获取信息失败");
            }

            @Override
            public void onResponse(String response, int id) {
                BaseJson<RemoteData> baseJson = new Gson().fromJson(response, new TypeToken<BaseJson<RemoteData>>() {
                }.getType());
                if (baseJson.ret.equals("success")) {
                    if (baseJson.data == null || baseJson.data.configFile == null || baseJson.data.msgData == null) {
                        LogUtil.e(TAG, "msg", "服务器无数据");
                        callBack.result(false, null, "服务器无数据");
                    } else {
                        LogUtil.e(TAG, "msg", "保存数据");
                        callBack.result(true, baseJson.data, null);
                        config = baseJson.data.configFile;
                        messageContents = baseJson.data.msgData;
                        LogUtil.e(TAG, "睡眠时间最小", config.intervalNum1);
                        LogUtil.e(TAG, "睡眠时间最大", config.intervalNum2);
                        LogUtil.e(TAG, "发送限制", config.msgLimit);
                        LogUtil.e(TAG, "列表大小", messageContents.size());
                        LogUtil.e(TAG, "X次查短信", config.balanceNum);
                    }
                } else {
                    LogUtil.e(TAG, "msg", "失败");
                    callBack.result(false, null, baseJson.forUser);
                }
            }
        });
    }


    @Override
    public void autoSendSMS(final CallBack callBack) {

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    super.run();
                    int space = (int) ((config.intervalNum1 + Math.random() * (config.intervalNum2 - config.intervalNum1)) * 60 * 1000);
                    LogUtil.e(TAG, "睡眠时间", space);

                    SmsManager smsManager = SmsManager.getDefault();

                    if (testMode) {
//                        space = 0;
//                        config.msgLimit = 10;
                    }

                    syncList.clear();
                    SMSReceiver.msg = "";


//                    int sendListSize = Math.min(config.msgLimit-SPUtil.getToadySendTimes(),messageContents.size());
                    if (messageContents == null || messageContents.size() == 0) {
                        EventBus.getDefault().post(new Event(Event.KEY_SHOW_TOAST, "已到达今日发送上限,发送限制" + config.msgLimit));
                        return;
                    }
                    for (int i = 0; i < messageContents.size(); i++) {
//                        if(SPUtil.getToadySendTimes()>config.msgLimit-1){
//                            EventBus.getDefault().post(new Event(Event.KEY_SHOW_TOAST,"已到达今日发送上限,今日发送"+(SPUtil.getToadySendTimes())+",发送限制"+config.msgLimit));
//                            return;
//                        }
//                        SPUtil.setTodaySendTimes(SPUtil.getToadySendTimes()+1);

                        MessageContent temp = new MessageContent();


                        MessageContent messageContent = messageContents.get(i);

                        String message = messageContent.content;
                        String phoneNumber = null;
                        if (testMode) {
                            phoneNumber = 1 + "";
                        } else {
                            phoneNumber = messageContent.phoneNum;
                        }
                        int msgId = messageContent.msgId;

                        temp.msgId = messageContent.msgId;
                        temp.status = -1;
                        syncList.add(temp);

                        Intent itSend = new Intent(IntentHelper.SMS_SEND_ACTION);
                        itSend.putExtra("phoneNumber", phoneNumber);
                        itSend.putExtra("message", message);
                        itSend.putExtra("msgId", msgId);
//                        itSend.putExtra("listSize",sendListSize);
                        itSend.putExtra("listSize", messageContents.size());
                        itSend.putExtra("currentIndex", i);

                        boolean checkBalance = false;
//                        if (i!=0&&config.balanceNum!=0&&i%(config.balanceNum-1)==0||i==sendListSize-1){//查询余额
//                            checkBalance = true;
//                        }else {
//                            checkBalance =false;
//                        }
                        if (config.balanceNum != 0 && i % (config.balanceNum - 1) == 0 || i == messageContents.size() - 1||i==0) {//查询余额
                            checkBalance = true;
                        } else {
                            checkBalance = false;
                        }
                        itSend.putExtra("checkBalance", checkBalance);


                        EventBus.getDefault().post(new Event(Event.KEY_SEND_INDEX, "正在发送短信:" + phoneNumber));

                        if (message.length() > 70) {
                            LogUtil.e(TAG, "多段发送", phoneNumber + "\n" + message);
                            ArrayList<String> msgs = smsManager.divideMessage(message);
                            itSend.putExtra("partsSize", msgs.size());
                            ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
                            for (int j = 0; j < msgs.size(); j++) {
                                LogUtil.e(TAG,"多段发送 index",j);
                                itSend.putExtra("isMultiPart", true);
                                itSend.putExtra("partsIndex", j);
                                PendingIntent sentPI = PendingIntent.getBroadcast(context, i+j+1000, itSend, 0);
                                sentIntents.add(sentPI);
                            }
                            smsManager.sendMultipartTextMessage(phoneNumber, null, msgs, sentIntents, null);
                        } else {
                            itSend.putExtra("isMultiPart", false);
                            PendingIntent sentPI = PendingIntent.getBroadcast(context, i+10000, itSend, 0);

                            LogUtil.e(TAG, "一段发送", phoneNumber + "\n" + message);
                            smsManager.sendTextMessage(phoneNumber, null, message, sentPI, null);
                        }
                        EventBus.getDefault().post(new Event(Event.KEY_SEND_INDEX, phoneNumber + "已发送"));

                        if (checkBalance) {
                            EventBus.getDefault().post(new Event(Event.KEY_SEND_INDEX, "正在查询余额"));
                            int times = 0;
                            while (!gotBalance) {
                                System.out.println("等余额 " + times);
                                times++;
                                sleep(1000);
                                if (times >= 90) {
                                    SIMUtil.setBalance("0");
                                    break;
                                }
                            }
                            gotBalance = false;
                        }

                        EventBus.getDefault().post(new Event(Event.KEY_SEND_INDEX, "休眠" + (space / 1000) + "秒"));
                        sleep(1000 + space);

                        LogUtil.e(TAG, "max size", syncList.size());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();


    }

    private void sendMsgFinish(CallBack callBack) {


        while (true) {
            int i = syncList.size();
            int sum = 0;
            try {
                for (MessageContent content : syncList) {
                    if (content.status == 0 || content.status == 1) {
                        sum++;
                    } else {
                        break;
                    }
                }
                if (sum == i) {
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
//            System.out.println(sum);
        }

        String msgData = new Gson().toJson(syncList);
        LogUtil.e(TAG, "sendMsgFinish", msgData);

        OkHttpUtils.post().url(HttpUrl.domain + HttpUrl.sendResult)
                .addParams(userIdKey, SPUtil.getUserId())
                .addParams(balanceKey, SIMUtil.getBalance(context))
                .addParams("msgData", msgData)
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                try {
                    System.out.println(e.getMessage());
                } catch (Exception ex) {
                    ex.getMessage();
                }
                LogUtil.e(TAG, "sendMsgFinish error", e.getMessage());
            }

            @Override
            public void onResponse(String response, int id) {
                LogUtil.e(TAG, "sendMsgFinish response", response);
                EventBus.getDefault().post(new Event(Event.KEY_SEND_MESSAGE_ALL_FINISH));

            }
        });
        syncList.clear();
    }

    @Override
    public void setRemoteData(RemoteData remoteData) {
        messageContents = remoteData.msgData;
        config = remoteData.configFile;
    }


    @Override
    public void sendToRemoteService(CallBack callBack) {

    }

    @Override
    public void registerUser(String phoneNumber, String simNumber, final CallBack callBack) {
        OkHttpUtils.post().url(HttpUrl.domain + HttpUrl.register)
                .addParams(simNumberKey, simNumber)
                .addParams(phoneNumKey, phoneNumber)
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {

                callBack.result(false, null, "网络错误");
            }

            @Override
            public void onResponse(String response, int id) {
                BaseJson<LoginBean> baseJson = new Gson().fromJson(response, new TypeToken<BaseJson<LoginBean>>() {
                }.getType());
                if (baseJson.ret.equals("success")) {
                    SPUtil.setUserId(String.valueOf(baseJson.data.userId));
                    callBack.result(true, null, null);
                } else {
                    callBack.result(false, null, baseJson.forUser);
                    if (baseJson.code == 1204) {
                        EventBus.getDefault().post(new Event(Event.KEY_CHANGE_BTN_TEXT, "等待激活"));
                    } else if (baseJson.code == 1202) {
                        EventBus.getDefault().post(new Event(Event.KEY_CERTIFICATION_PERMISSION));
                    }
                }
            }
        });
    }

    @Override
    public void deleteSMS(Context context, long smsId) {
        Uri uri;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            uri = Telephony.Sms.CONTENT_URI;
        } else {
            uri = Uri.parse("content://sms");
        }
        context.getContentResolver().delete(uri, "_id = " + smsId, null);
    }

    @Override
    public Cursor querySMS(Context context, String selection) {
        Uri uri;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            uri = Telephony.Sms.CONTENT_URI;
        } else {
            uri = Uri.parse("content://sms");
        }
        return context.getContentResolver().query(uri, null, selection, null, null);
    }

    @Override
    public void checkUpdate(Context context, final String currentVersion, final CallBack callBack) {
        OkHttpUtils.get().url(HttpUrl.domain + HttpUrl.checkUpdate).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                callBack.result(false, null, "网络错误，无法获取最新版本");
            }

            @Override
            public void onResponse(String response, int id) {
                BaseJson<NewVersion> baseJson = new Gson().fromJson(response, new TypeToken<BaseJson<NewVersion>>() {
                }.getType());
                if (baseJson.ret.equals("success")) {
                    if (!TextUtils.isEmpty(baseJson.data.version) && baseJson.data.version.compareTo(currentVersion) > 0) {
                        callBack.result(true, baseJson.data, null);
                    }
                } else {
                    callBack.result(false, null, baseJson.forUser);
                }
            }
        });
    }

    @Override
    public void getHomeText(Context context, final CallBack callBack) {
        OkHttpUtils.get().url(HttpUrl.domain + HttpUrl.getHomeText).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                callBack.result(false, null, "网络错误");
            }

            @Override
            public void onResponse(String response, int id) {
                BaseJson<HomeText> baseJson = new Gson().fromJson(response, new TypeToken<BaseJson<HomeText>>() {
                }.getType());
                if (baseJson.ret.equals("success")) {
                    callBack.result(true, baseJson.data, null);
                } else {
                    callBack.result(false, null, baseJson.forUser);
                }
            }
        });
    }

    @Override
    public void getScoreByUserId(Context context, String userId, final CallBack callBack) {
        OkHttpUtils.get().url(HttpUrl.domain + HttpUrl.getScore + userId).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                callBack.result(false, null, "网络错误");
            }

            @Override
            public void onResponse(String response, int id) {
                BaseJson<LoginBean> beanBaseJson = new Gson().fromJson(response, new TypeToken<BaseJson<LoginBean>>() {
                }.getType());
                if (beanBaseJson.ret.equals("success")) {
                    callBack.result(true, beanBaseJson.data, null);
                } else {
                    callBack.result(false, null, beanBaseJson.forUser);
                }
            }
        });
    }


}
