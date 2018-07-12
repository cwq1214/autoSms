package com.jyt.autosms.app.activity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jyt.autosms.R;
import com.jyt.autosms.SharePrefrence.SPUtil;
import com.jyt.autosms.app.broadcastReceiver.SMSReceiver;
import com.jyt.autosms.app.dialog.AutoCloseInfoDialogFragment;
import com.jyt.autosms.app.dialog.FinishDialogFragment;
import com.jyt.autosms.app.dialog.InputDialog;
import com.jyt.autosms.app.dialog.NewVersionDialog;
import com.jyt.autosms.app.dialog.WaitingDialog;
import com.jyt.autosms.app.event.Event;
import com.jyt.autosms.app.service.MainService;
import com.jyt.autosms.bean.CheckSMS;
import com.jyt.autosms.bean.HomeText;
import com.jyt.autosms.bean.NewVersion;
import com.jyt.autosms.util.IntentHelper;
import com.jyt.autosms.util.LogUtil;
import com.jyt.autosms.util.SIMUtil;
import com.jyt.autosms.util.ServiceUtil;
import com.jyt.autosms.util.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    String TAG = getClass().getSimpleName();

    SmsManager smsManager = SmsManager.getDefault();

    WaitingDialog dialog;

    Button startBtn;

    TextView simNum, phoneNumber, version, score,ruleTitle,ruleContent,about;


    Uri uri = Uri.parse("content://sms");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        EventBus.getDefault().register(this);

        startBtn = (Button) findViewById(R.id.button);
        simNum = (TextView) findViewById(R.id.simNum);
        phoneNumber = (TextView) findViewById(R.id.phoneNumber);
        version = (TextView) findViewById(R.id.version);
        score = (TextView) findViewById(R.id.score);
        ruleTitle = (TextView) findViewById(R.id.ruleTitle);
        ruleContent = (TextView) findViewById(R.id.ruleContent);
        about = (TextView) findViewById(R.id.about);

        try {
            version.setText("版本号 v" + getPackageManager().getPackageInfo(getPackageName(), PackageManager.COMPONENT_ENABLED_STATE_DEFAULT).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        checkDate();

//        startBtn.setEnabled(true);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new Event(Event.KEY_SERVICE_START));
                ToastUtils.showShort(getContext(), "后台发送短信");
                finish();

//                showFinishDialog();
//                NewVersion newVersion = new NewVersion();
//                showUpdateDialog(newVersion);
//                final AutoCloseInfoDialogFragment dialogFragment = new AutoCloseInfoDialogFragment();
//                dialogFragment.setTitleText("我们暂时无法为您的手机提供服务");
//                dialogFragment.setContentText("版本不兼容，即将退出");
//                dialogFragment.setOnDialogDisMissListener(new AutoCloseInfoDialogFragment.OnDialogDisMissListener() {
//                    @Override
//                    public void onDisMiss() {
//                        dialogFragment.dismissAllowingStateLoss();
//                        finish();
//                    }
//                });
//                dialogFragment.show(getSupportFragmentManager(),null);

            }
        });

        if (!MainService.isSending) {
            LogUtil.e(TAG, "is sending", "没有发短信");

            //手机功能测试
//            if (!SPUtil.getPermission()) {
                functionTest();
//                return;
//            }
            if (!checkSim()) {
                return;
            }
            ;
            setPhoneNumber(SPUtil.getNativePhoneNumber());
            setSimNum(SPUtil.getSimNumber());
            if (SPUtil.getPermission()) {
                getPermission();
            }
        } else {
            LogUtil.e(TAG, "is sending", "正在发短信");
            setPhoneNumber(SPUtil.getNativePhoneNumber());
            setSimNum(SPUtil.getSimNumber());
            setScore(SPUtil.getScore()+"");
            startBtn.setText("发送中...");
            startBtn.setBackgroundResource(R.drawable.bg_btn_doing);
        }


    }

    private void checkDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = simpleDateFormat.format(new Date());
        LogUtil.e(TAG, "currentDate", currentDate);

        if (!currentDate.equals(SPUtil.getSaveDate())) {
            SPUtil.setSaveDate(currentDate);
            SPUtil.setTodaySendTimes(0);
        }
    }


    private boolean checkSim() {
        String phoneNumber = SPUtil.getNativePhoneNumber();
        String simNumber = SIMUtil.getSIMNumber(getContext());
        String saveSimNumber = SPUtil.getSimNumber();

        if (TextUtils.isEmpty(simNumber)) {
            ToastUtils.showShort(getContext(), "获取SIM卡卡号失败，请检查SIM卡是否正常");
            return false;
        }
        if (TextUtils.isEmpty(phoneNumber) || !simNumber.equals(saveSimNumber) || TextUtils.isEmpty(SPUtil.getUserId())) {
            showInputDialog();

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String currentDate = simpleDateFormat.format(new Date());
            SPUtil.setSaveDate(currentDate);
            SPUtil.setTodaySendTimes(0);
            SPUtil.setScore(0);
            return false;
        }
        return true;
    }


    private void functionTest() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                while (!SMSReceiver.ready) {
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                String providersName = SIMUtil.getProvidersName(getContext());
                Intent intent = new Intent(IntentHelper.SMS_SEND_ACTION);
                intent.putExtra("testMsg", true);


                String phoneNumber = "1";
                String message = "1";
//                if (providersName.equals("中国移动")){
//                    phoneNumber="10086";
//                }else if (providersName.equals("中国联通")){
//                    phoneNumber="10010";
//                }else if (providersName.equals("中国电信")){
//                    phoneNumber="10001";
//                }
                intent.putExtra("address", phoneNumber);
                intent.putExtra("message", message);
                PendingIntent spIntent = PendingIntent.getBroadcast(getContext(), 0, intent, 0);

                smsManager.sendTextMessage(phoneNumber, null, message, spIntent, null);
            }
        }.start();


    }

    private void getPermission() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                while (!ServiceUtil.isRunning(getContext(), MainService.class.getName())) {
                }

                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                EventBus.getDefault().post(new Event(Event.KEY_CERTIFICATION_PERMISSION));
                EventBus.getDefault().post(new Event(Event.KEY_GET_SCORE));


            }
        };
        thread.start();
    }

    @Subscribe
    public void onEvent(Event event) {
        switch (event.getEventKey()) {
            case Event.KEY_SHOW_WAITING_DIALOG:
                try {
                    showDialog(((String) event.getData()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case Event.KEY_HIDE_WAITING_DIALOG:
                hideDialog();
                break;
            case Event.KEY_BUTTON_CAN_CLICK:
                startBtn.setEnabled(true);
                break;
            case Event.KEY_BUTTON_CAN_NOT_CLICK:
                startBtn.setEnabled(false);
                break;
            case Event.KEY_SEND_MESSAGE_ALL_FINISH:
                startBtn.setEnabled(true);
                startBtn.setText("开始运行");
                startBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.sel_button));
                showFinishDialog();
                EventBus.getDefault().post(new Event(Event.KEY_GET_SCORE));
                break;
            case Event.KEY_SHOW_UPDATE_DIALOG:
                showUpdateDialog((NewVersion) event.getData());
                break;
            case Event.KEY_CHECK_FUNCTION:
                CheckSMS checkSMS = (CheckSMS) event.getData();
                checkFunction(checkSMS.isSuccess, checkSMS.message);
                break;
            case Event.KEY_FINISH_ACTIVITY:
                if (MainActivity.class.equals(event.getData())) {
                    finish();
                }
                break;
            case Event.KEY_SHOW_INPUT_DIALOG:
                showInputDialog();
                break;
            case Event.KEY_CHANGE_BTN_TEXT:
                changeBtnText((String) event.getData());
                break;
            case Event.KEY_SET_SCORE:
                setScore(String.valueOf(event.getData()));
                break;
            case Event.KEY_SET_HOME_TEXT:
                setHomeText((HomeText) event.getData());
                break;
        }
    }

    private void checkFunction(boolean hadFunction, String message) {
        LogUtil.e(TAG, "checkFunction", hadFunction);
        SPUtil.setPermission(hadFunction);
        if (hadFunction) {
            if (!checkSim()) {
                return;
            }
            ;
            getPermission();
        } else {
            final AutoCloseInfoDialogFragment dialogFragment = new AutoCloseInfoDialogFragment();
            dialogFragment.setTitleText("我们暂时无法为您的手机提供服务");
            dialogFragment.setContentText(TextUtils.isEmpty(message) ? "版本不兼容，即将退出" : (message + "，即将退出"));
            dialogFragment.setOnDialogDisMissListener(new AutoCloseInfoDialogFragment.OnDialogDisMissListener() {
                @Override
                public void onDisMiss() {
                    dialogFragment.dismissAllowingStateLoss();
                    finish();
                }
            });
            getSupportFragmentManager().beginTransaction().add(dialogFragment, null).commitAllowingStateLoss();
        }
    }

    private Context getContext() {
        return this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void showDialog(final String message) throws Exception {
        System.out.println("show dialog");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    if (dialog != null) {
                        dialog.dismissAllowingStateLoss();
                        dialog = null;
                    }
                    if (dialog == null) {
                        dialog = new WaitingDialog();
                        Bundle bundle = new Bundle();
                        bundle.putString("message", message);
                        dialog.setArguments(bundle);
                        getSupportFragmentManager().beginTransaction().add(dialog, null).commitAllowingStateLoss();
                    }
                }
            }
        });

    }

    private void hideDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dialog != null) {
                    dialog.dismissAllowingStateLoss();
                    dialog = null;
                }
            }
        });

    }

    private void showInputDialog() {
        final InputDialog dialog = new InputDialog();
        dialog.setOnClickListener(new InputDialog.OnClickListener() {
            @Override
            public void onClick(boolean isDone, String input) {
                if (isDone) {
                    if (TextUtils.isEmpty(input)) {
                        ToastUtils.showShort(getContext(), "手机号不能为空");
                        return;
                    }
                    SPUtil.setNativePhoneNumber(input.trim());
                    SPUtil.setSimNumber(SIMUtil.getSIMNumber(getContext()));
                    dialog.dismissAllowingStateLoss();
                    setPhoneNumber(input.trim());
                    setSimNum(SPUtil.getSimNumber());
                    EventBus.getDefault().post(new Event(Event.KEY_REGISTER_USER));
                } else {
                    ToastUtils.showShort(getContext(), "用户取消输入，程序关闭");
                    dialog.dismissAllowingStateLoss();
                    finish();
                }

            }
        });
        getSupportFragmentManager().beginTransaction().add(dialog, null).commitAllowingStateLoss();
    }

    private void showUpdateDialog(final NewVersion newVersion) {
        final NewVersionDialog dialog = new NewVersionDialog();
        dialog.setVersionInfo(newVersion);
        dialog.show(getSupportFragmentManager(), "newVersion");
    }

    private void showFinishDialog() {
        if (!isFinishing()) {
            final FinishDialogFragment dialogFragment = new FinishDialogFragment();

            dialogFragment.setOnDoneClickListener(new FinishDialogFragment.OnDoneClickListener() {
                @Override
                public void onDoneClick() {
                    dialogFragment.dismissAllowingStateLoss();
                    finish();
                }
            });
            getSupportFragmentManager().beginTransaction().add(dialogFragment, null).commitAllowingStateLoss();
        }
    }

    private void setSimNum(String simNum) {
        this.simNum.setText(simNum);
    }

    private void setPhoneNumber(String phoneNumber) {
        this.phoneNumber.setText(phoneNumber);
    }

    private void setScore(String score) {
        this.score.setText(score);
    }

    public void changeBtnText(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startBtn.setText(text);
            }
        });
    }

    public void setHomeText(HomeText homeText) {
        if (homeText != null) {
            ruleTitle.setText(homeText.title);
            ruleContent.setText(homeText.content);
            about.setText(homeText.about);
        }
    }
}
