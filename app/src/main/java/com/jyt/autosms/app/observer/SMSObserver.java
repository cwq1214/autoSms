package com.jyt.autosms.app.observer;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.jyt.autosms.SharePrefrence.SPUtil;
import com.jyt.autosms.app.event.Event;
import com.jyt.autosms.util.LogUtil;
import com.jyt.autosms.util.MatchUtil;
import com.jyt.autosms.util.NetUtils;
import com.jyt.autosms.util.SIMUtil;
import com.jyt.autosms.util.ToastUtils;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by v7 on 2016/9/26.
 */

public class SMSObserver extends ContentObserver {
    String TAG = getClass().getSimpleName();

    Context mContext;

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public SMSObserver(Handler handler , Context context) {
        super(handler);
        mContext = context.getApplicationContext();
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        LogUtil.e(TAG,"----","短信数据库发生变化");

        //content://sms/inbox 收件箱
        //content://sms/sent 已发送
        //content://sms/draft 草稿
        //content://sms/outbox 发件箱
        //content://sms/failed 发送失败
        //content://sms/queued 待发送列表
        Uri uri = Uri.parse("content://sms/inbox");

        String[] projection = null;

        String selection = null;

        String[] selectionArgs = null;

        String sortOrder = "date desc";

        selection = "(address = '10086'OR address = '10010') AND (date > "+(System.currentTimeMillis()-(1*60*1000)) +")";


        Cursor c = mContext.getContentResolver().query(uri, projection, selection, selectionArgs,sortOrder);

        try {
            readMsg(c);
        }catch (Exception e){
            ToastUtils.showShort(mContext,"无法读取短信，请在系统中开启读取短信权限");
            NetUtils.openSetting(mContext);
            e.printStackTrace();
        }finally {
            if (!c.isClosed()) {
                c.close();
            }
        }


    }

    private void readMsg(Cursor c){
        if (c != null&&c.getCount()>0) {
//            while (c.moveToNext()) {
            c.moveToFirst();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date d = new Date(c.getLong(c.getColumnIndex("date")));
            String date = dateFormat.format(d);
            StringBuilder sb = new StringBuilder();
            sb.append("发件人手机号码: " + c.getString(c.getColumnIndex("address")))
                    .append("信息内容: " + c.getString(c.getColumnIndex("body")))
                    .append(" 是否查看: " + c.getInt(c.getColumnIndex("read")))
                    .append(" 类型： " + c.getInt(c.getColumnIndex("type"))).append(date);
            Log.i("xxx", sb.toString());
//            }
            String msgBody = c.getString(c.getColumnIndex("body"));
            String balance = MatchUtil.matchBalance(msgBody);
            if (TextUtils.isEmpty(balance)) {
                String phoneNumber = MatchUtil.matchNativePhoneNumber(msgBody);
                if (TextUtils.isEmpty(phoneNumber)) {
                    //TODO 再发一次短信
                    EventBus.getDefault().post(new Event(Event.KEY_SEND_MESSAGE_AGAIN));
                } else {
                    LogUtil.e(TAG, "手机号", phoneNumber);
                    SPUtil.setBalance(phoneNumber);
                    SPUtil.setSimNumber(SIMUtil.getSIMNumber(mContext));
                    SIMUtil.setPhoneNumber(phoneNumber);
                    EventBus.getDefault().post(new Event(Event.KEY_GOT_PHONE_NUMBER));

                }
            } else {
                SPUtil.setBalance(balance);
                LogUtil.e(TAG, "余额", balance);
                SIMUtil.setBalance(balance);
                EventBus.getDefault().post(new Event(Event.KEY_GOT_BALANCE));
            }


            c.close();
        }
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
    }
}
