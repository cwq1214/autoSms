package com.jyt.autosms.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by v7 on 2016/9/26.
 */

public class MatchUtil {

    public static String matchNativePhoneNumber(String body){
        String phoneNumber = null;
        Pattern pattern = Pattern.compile("号码\\D*\\d{3,11}");
        Matcher matcher = pattern.matcher(body);
        if (matcher.find()){
            String subString = matcher.group(0);
            System.out.println(subString);

            Pattern pattern1 = Pattern.compile("\\d+");
            Matcher matcher1 = pattern1.matcher(subString);
            if (matcher1.find()){
                phoneNumber = matcher1.group(0);
                System.out.println(phoneNumber);
            }
        }
        return phoneNumber;
    }

    public static String matchBalance(String body){
        String balance = null;
        Pattern pattern = Pattern.compile("余额\\D*\\d*\\.\\d*元");
        Matcher matcher = pattern.matcher(body);
        if (matcher.find()){
            String subString = matcher.group(0);
            System.out.println(subString);
            Pattern pattern1 = Pattern.compile("\\d*\\.\\d*");
            Matcher matcher1 = pattern1.matcher(subString);
            if (matcher1.find()){
                balance = matcher1.group(0);
                System.out.println(balance);
            }
        }
        return balance;
    }
}
