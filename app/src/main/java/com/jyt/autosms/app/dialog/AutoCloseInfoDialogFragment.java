package com.jyt.autosms.app.dialog;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jyt.autosms.R;
import com.jyt.autosms.util.ScreenUtils;

/**
 * Created by v7 on 2016/10/18.
 */

public class AutoCloseInfoDialogFragment extends DialogFragment {

    TextView infoTitle,content,currentTimes;

    String titleStr,contentStr;

    final int WHAT_UPDATE_TIMES = 1;

    int times = 5;

    OnDialogDisMissListener onDialogDisMissListener;

    Handler timer = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==WHAT_UPDATE_TIMES){
                currentTimes.setText(msg.arg1+"");
            }
        }
    };


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.CustomDialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auto_close_info,null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        infoTitle = (TextView) view.findViewById(R.id.infoTitle);
        content = (TextView) view.findViewById(R.id.content);
        currentTimes = (TextView) view.findViewById(R.id.currentTimes);

        timer.post(new Runnable() {
            @Override
            public void run() {
                if (times>0){
                    times--;
                    Message message = new Message();
                    message.what = WHAT_UPDATE_TIMES;
                    message.arg1=times;
                    timer.sendMessage(message);
                    timer.postDelayed(this,1000);
                }else {
                    dismiss();

                }
            }
        });
        infoTitle.setText(titleStr);
        content.setText(contentStr);
    }

    @Override
    public void onResume() {
        super.onResume();
        int width = (int) (ScreenUtils.getScreenWidth(getContext()));
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setLayout(width, height);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (onDialogDisMissListener!=null){
            onDialogDisMissListener.onDisMiss();
        }
    }

    public void setTitleText(String text){
        titleStr = text;
    }
    public void setContentText(String text){
        contentStr = text;
    }
    public void setOnDialogDisMissListener(OnDialogDisMissListener onDialogDisMissListener) {
        this.onDialogDisMissListener = onDialogDisMissListener;
    }

    public interface OnDialogDisMissListener{
        void onDisMiss();
    }
}
