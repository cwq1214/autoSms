package com.jyt.autosms;

import junit.framework.TestCase;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest extends TestCase{

    String phoneNumberStr = "尊敬的客户：您本机号码为10086，属于神州行品牌。更多套餐详情可点击 gd.10086.cn/app26 下载“广东移动10086”APP 查询。中国移动";
    String balanceStr = "尊敬的客户：您当前账户余额12.67元，下一个月结日为2016年10月14日。如需充值可点击 http://gd.10086.cn/cz 。中国移动";

    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void getPhoneNumber(){
        Pattern pattern = Pattern.compile("号码\\D*\\d{3,11}");
        Matcher matcher = pattern.matcher(phoneNumberStr);
        if (matcher.find()){
            String subString = matcher.group(0);
            System.out.println(subString);

            Pattern pattern1 = Pattern.compile("\\d+");
            Matcher matcher1 = pattern1.matcher(subString);
            if (matcher1.find()){
                String phoneNumber = matcher1.group(0);
                System.out.println(phoneNumber);
            }
        }
    }

    @Test
    public void getBalance(){
        Pattern pattern = Pattern.compile("余额\\D*\\d*\\.\\d*元");
        Matcher matcher = pattern.matcher(balanceStr);
        if (matcher.find()){
            String subString = matcher.group(0);
            System.out.println(subString);
            Pattern pattern1 = Pattern.compile("\\d*\\.\\d*");
            Matcher matcher1 = pattern1.matcher(subString);
            if (matcher1.find()){
                String balance = matcher1.group(0);
                System.out.println(balance);
            }
        }

    }



}