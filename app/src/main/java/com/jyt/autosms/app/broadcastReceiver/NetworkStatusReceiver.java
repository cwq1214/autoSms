package com.jyt.autosms.app.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.jyt.autosms.SharePrefrence.SPUtil;
import com.jyt.autosms.app.event.Event;
import com.jyt.autosms.util.NetUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by v7 on 2016/9/28.
 */

public class NetworkStatusReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (NetUtils.isConnected(context)){
            if (!TextUtils.isEmpty(SPUtil.getSerResult())){
                EventBus.getDefault().post(new Event(Event.KEY_SEND_RESULT));
            }
        }
    }
}
