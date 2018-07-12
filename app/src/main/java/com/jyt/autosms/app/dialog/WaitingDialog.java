package com.jyt.autosms.app.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.jyt.autosms.R;
import com.jyt.autosms.util.ScreenUtils;

/**
 * Created by V7 on 2016/9/29.
 */

public class WaitingDialog extends DialogFragment{

    View rootView;

    TextView message;

    String messageText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL,R.style.CustomDialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setCancelable(false);

        if (rootView==null) {
            rootView = inflater.inflate(R.layout.fragment_waiting_dialog, null);
            findById();
        }
        return rootView;
    }

    private void findById(){
        message = (TextView) rootView.findViewById(R.id.message);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        String messageStr = getArguments().getString("message");
        if(!TextUtils.isEmpty(messageStr)){
            message.setText(messageStr);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        int width = (int) (ScreenUtils.getScreenWidth(getContext()));
        int height = WindowManager.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setLayout(width, height);
    }

    public void setMessageText(String text){

        message.setText(text);
    }

}

