package com.jyt.autosms.util;

import android.content.Context;
import android.os.Handler;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by v7 on 2016/9/23.
 */

public class SIMUtil {

    private static int MAX_WHILE_OUT = 60;
    private static String balance = null;
    private static String phoneNumber = null;

    public static String getProvidersName(Context context) {
        String ProvidersName = "N/A";
        try{
            String IMSI = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getSubscriberId();
            // IMSI号前面3位460是国家，紧接着后面2位00 02是中国移动，01是中国联通，03是中国电信。
            System.out.println(TextUtils.isEmpty(IMSI)?"无SIMid":IMSI);
            if (TextUtils.isEmpty(IMSI)){
                return ProvidersName;
            }
            if (IMSI.startsWith("46000") || IMSI.startsWith("46002")) {
                ProvidersName = "中国移动";
            } else if (IMSI.startsWith("46001")) {
                ProvidersName = "中国联通";
            } else if (IMSI.startsWith("46003")) {
                ProvidersName = "中国电信";
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return ProvidersName;
    }


    public static String getBalance(final Context context){
        String balance = null;
//        balance = "10";
        sendMessageToGetBalance(context);

        Timer timer = new Timer();
        timer.schedule(new BalanceTimeOutTask(context.getApplicationContext(),60),0,1000);
        int times = 0;

        while (TextUtils.isEmpty(SIMUtil.balance)){
            try {
                Thread.sleep(1000);
                LogUtil.e("simutil","times",times);
                times++;
                if (times==MAX_WHILE_OUT){
                    new Handler(context.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtils.showShort(context,"无法读取到余额");
                        }
                    });
                    SIMUtil.balance="0";
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        timer.cancel();
        balance = SIMUtil.balance;

        SIMUtil.balance = null;

        return balance;
    }

    public static String getSIMNumber(Context context){
        String serNum = null;
        serNum = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getSimSerialNumber();
        return serNum;
    }

    private static void sendMessageToGetPhoneNumber(Context context){
        SmsManager smsManager = SmsManager.getDefault();

        String providersName = getProvidersName(context);


        if (providersName.equals("中国移动")){
            smsManager.sendTextMessage("10086",null,"bj",null,null);
        }else if (providersName.equals("中国联通")){
            //TODO 联通获取手机号码
            smsManager.sendTextMessage("10010",null,"cxll",null,null);
        }

    }

    private static void sendMessageToGetBalance(Context context){
        SmsManager smsManager = SmsManager.getDefault();

        String providersName = getProvidersName(context);


        if (providersName.equals("中国移动")){
            smsManager.sendTextMessage("10086",null,"101",null,null);
        }else if (providersName.equals("中国联通")){
            //TODO 联通获取手机余额
            smsManager.sendTextMessage("10010",null,"ye",null,null);
        }else if (providersName.equals("中国电信")){
            smsManager.sendTextMessage("10001",null,"102",null,null);

        }
    }

    public static void setBalance(String balance) {
        SIMUtil.balance = balance;
    }

    public static void setPhoneNumber(String phoneNumber) {
        SIMUtil.phoneNumber = phoneNumber;
    }

    static class PhoneNumberTimeOutTask extends TimerTask {
        int maxTimes=0;
        int currentTimes=0;
        Context context;

        public PhoneNumberTimeOutTask(Context context,int maxTimes) {
            this.maxTimes = maxTimes;
            this.context = context;
        }

        @Override
        public void run() {
            if (TextUtils.isEmpty(SIMUtil.phoneNumber)||currentTimes<=maxTimes){
                currentTimes++;
            }else if (currentTimes==60){
                currentTimes=0;
                sendMessageToGetPhoneNumber(context);
            }
        }
    }

    static class BalanceTimeOutTask extends TimerTask {
        int maxTimes=0;
        int currentTimes=0;
        Context context;

        public BalanceTimeOutTask(Context context,int maxTimes) {
            this.maxTimes = maxTimes;
            this.context = context;
        }

        @Override
        public void run() {
            if (TextUtils.isEmpty(SIMUtil.phoneNumber)||currentTimes<=maxTimes){
                currentTimes++;
            }else if (currentTimes==60){
                currentTimes=0;
                sendMessageToGetBalance(context);
            }
        }

    }
}
