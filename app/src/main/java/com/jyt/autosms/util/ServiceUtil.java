package com.jyt.autosms.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ServiceInfo;

import java.util.ArrayList;

/**
 * Created by V7 on 2016/9/29.
 */

public class ServiceUtil {

    public static boolean isRunning(Context context,String className){

        System.out.println(className);
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        ArrayList<ActivityManager.RunningServiceInfo> list= (ArrayList) activityManager.getRunningServices(Integer.MAX_VALUE);

        if (list==null||list.size()==0){
            return false;
        }

        int size = list.size();
        for (int i=0;i<size;i++){
            if (list.get(i).service.getClassName().equals(className)){
                return true;
            }
        }

        return false;
    }
}
