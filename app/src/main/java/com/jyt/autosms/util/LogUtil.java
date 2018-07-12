package com.jyt.autosms.util;

import android.util.Log;

/**
 * Created by v7 on 2016/8/16.
 */
public class LogUtil {


    public static void e(String TAG, String key, Object value){
        Log.e(TAG, key+" "+(value==null?"value null ":value.toString()) );
    }
    public static void v(String TAG, String key, Object value){
        Log.v(TAG, key+" "+(value==null?"value null ":value.toString()) );
    }
}
