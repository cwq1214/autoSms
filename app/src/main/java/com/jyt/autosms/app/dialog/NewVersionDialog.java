package com.jyt.autosms.app.dialog;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.jyt.autosms.R;
import com.jyt.autosms.app.event.Event;
import com.jyt.autosms.bean.NewVersion;
import com.jyt.autosms.util.DensityUtils;
import com.jyt.autosms.util.LogUtil;
import com.jyt.autosms.util.ScreenUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by v7 on 2016/10/12.
 */

public class NewVersionDialog extends DialogFragment {
    String TAG = getClass().getSimpleName();

    TextView content,currentTimes;

    OnClickListener onClickListener;

    NewVersion versionInfo;


    Handler timmer = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==111){
                currentTimes.setText(msg.arg1+"");
            }
        }
    };
    int times = 5;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setStyle(STYLE_NORMAL,R.style.CustomDialog);
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_new_version,null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        content = (TextView) view.findViewById(R.id.content);
        currentTimes = (TextView) view.findViewById(R.id.currentTimes);

        if (TextUtils.isEmpty(versionInfo.content)){
            content.setText("新版本更快更出色");
        }else {
            LogUtil.e(TAG,"contentStr",versionInfo.content);
            content.setVisibility(View.VISIBLE);
            content.setText(versionInfo.content);
        }


        timmer.post(new Runnable() {
            @Override
            public void run() {

                if (times>0){
                    times--;
                    timmer.postDelayed(this,1000);
                    Message message = new Message();
                    message.what=111;
                    message.arg1 =times;
                    timmer.sendMessage(message);
                }else {
                    dismiss();
                }
            }
        });
    }

    @Override
    public void dismiss() {
        super.dismiss();
        EventBus.getDefault().post(new Event(Event.KEY_DOWNLOAD,versionInfo));
    }

    @Override
    public void onResume() {
        super.onResume();
        int width = (int) (ScreenUtils.getScreenWidth(getContext()));
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setLayout(width,height);
    }

    public void setVersionInfo(NewVersion newVersion){
        versionInfo = newVersion;
    }
    public void setOnClickListener(OnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }

    public interface OnClickListener {
        void onClick(boolean isDone,Object data);
    }
}
