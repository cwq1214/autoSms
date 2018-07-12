package com.jyt.autosms.SharePrefrence;

import com.orhanobut.hawk.Hawk;

/**
 * Created by v7 on 2016/9/23.
 */

public class SPUtil {
    private static String SIM_NUMBER = "sim_number";
    private static String BALANCE = "balance";
    private static String NATIVE_PHONE_NUMBER = "native_phone_number";
    private static String PERMISSION = "permission";
    private static String SEND_MESSAGE_COUNT = "send_message_count";
    private static String MIN_INTERVAL = "min_interval";
    private static String MAX_INTERVAL = "max_interval";
    private static String USER_ID = "user_id";
    private static String SCORE = "score";

    private static String SER_PHONE_NUM = "ser_phone_num";
    private static String SER_MESSAGE_CONTENT = "ser_message_content";
    private static String SER_RESULT = "ser_result";
    private static String SER_SMS_TEMPLATE = "ser_sms_template";
    private static String SENDING = "sending";

    private static String SAVE_DATE = "save_date";
    private static String TODAY_SEND_TIMES = "today_send_times";
    private static String SER_HOME_TEXT = "ser_home_text";


    public static long getScore(){
        return Hawk.get(SCORE,0l);
    }
    public static void setScore(long score){
        Hawk.put(SCORE,score);
    }

    public static String getSerHomeText(){
        return Hawk.get(SER_HOME_TEXT,null);
    }

    public static void setSerHomeText(String ser){
        Hawk.put(SER_HOME_TEXT,ser);
    }

    public static String getSaveDate(){
        return Hawk.get(SAVE_DATE,null);
    }
    public static int getToadySendTimes(){
        return Hawk.get(TODAY_SEND_TIMES,0);
    }

    public static void setSaveDate(String date){
        Hawk.put(SAVE_DATE,date);
    }

    public static void setTodaySendTimes(int times){
        Hawk.put(TODAY_SEND_TIMES,times);
    }

    public static boolean getIsSending(){
        return Hawk.get(SENDING,false);
    }

    public static void setIsSending(boolean isSending){
        Hawk.put(SENDING,isSending);
    }

    public static String getUserId(){
        return Hawk.get(USER_ID,null);
    }

    public static void setUserId(String userId){
        Hawk.put(USER_ID,userId);
    }

    public static void setBalance(String balance){
        Hawk.put(BALANCE,balance);
    }

    public static String getBalance(){
        return Hawk.get(BALANCE,null);
    }

    public static void setSimNumber(String simNumber){
        Hawk.put(SIM_NUMBER,simNumber);
    }

    public static String getSimNumber(){
        return Hawk.get(SIM_NUMBER,null);
    }

    public static void setNativePhoneNumber(String phoneNumber){
        Hawk.put(NATIVE_PHONE_NUMBER,phoneNumber);
    }

    public static String getNativePhoneNumber(){
        return Hawk.get(NATIVE_PHONE_NUMBER,null);
    }

    public static void setSerSMSTemplate(String st){
        Hawk.put(SER_SMS_TEMPLATE,st);
    }

    public static String getSerSMSTemplate(){
        return Hawk.get(SER_SMS_TEMPLATE,null);
    }

    public static void setSerResult(String result){
        Hawk.put(SER_RESULT,result);
    }

    public static String getSerResult(){
        return Hawk.get(SER_RESULT,null);
    }

    public static void setSerMessageContent(String mc){
        Hawk.put(SER_MESSAGE_CONTENT,mc);
    }

    public static String getSerMessageContent(){
        return Hawk.get(SER_MESSAGE_CONTENT,null);
    }

    public static void setSerPhoneNum(String pn){
        Hawk.put(SER_PHONE_NUM,pn);
    }

    public static String getSerPhoneNum(){
        return Hawk.get(SER_PHONE_NUM,null);
    }

    public static boolean getPermission(){
        return Hawk.get(PERMISSION,false);
    }

    public static void setPermission(boolean permission){
        Hawk.put(PERMISSION,permission);
    }

    public static Integer getSendMessageCount(){
        return Hawk.get(SEND_MESSAGE_COUNT,null);
    }

    public static void setSendMessageCount(Integer count){
        Hawk.put(SEND_MESSAGE_COUNT,count);
    }

    public static Integer getMinInterval(){
        return Hawk.get(MIN_INTERVAL,null);
    }

    public static void setMinInterval(Integer interval){
        Hawk.put(MIN_INTERVAL,interval);
    }

    public static Integer getMaxInterval(){
        return Hawk.get(MAX_INTERVAL,null);
    }

    public static void setMaxInterval(Integer interval){
        Hawk.put(MAX_INTERVAL,interval);
    }
}
