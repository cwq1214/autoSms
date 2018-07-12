package com.jyt.autosms.app.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.jyt.autosms.app.event.Event;
import com.jyt.autosms.util.IntentHelper;
import com.jyt.autosms.util.LogUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by v7 on 2016/9/23.
 */

public class SMSReceiver extends BroadcastReceiver {
    public static String msg= "";
    String TAG = "SMSReceiver";
    public static boolean ready = false;
    public static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

    public SMSReceiver() {
        ready = true;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.e(TAG,"SMSReceiver","onReceive");
        Bundle bundle= intent.getExtras();
        if (bundle!=null) {
            Set<String> keySet = bundle.keySet();
            Iterator<String> iterator = keySet.iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                LogUtil.e(TAG, key, bundle.get(key));
            }
        }
        if (intent.getAction().equals(IntentHelper.SMS_SEND_ACTION)){

            String phoneNumber = intent.getStringExtra("phoneNumber");
            int partsIndex = intent.getIntExtra("partsIndex",0);
            int partsSize = intent.getIntExtra("partsSize",1);
            intent.putExtra("resultCode",getResultCode());
            EventBus.getDefault().post(new Event(Event.KEY_AFTER_ONE_SEND,intent));

        }
    }

}
