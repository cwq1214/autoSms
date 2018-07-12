package com.jyt.autosms.app.dialog;

import android.app.Dialog;
import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Layout;
import android.util.LayoutDirection;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jyt.autosms.R;
import com.jyt.autosms.util.DensityUtils;
import com.jyt.autosms.util.ScreenUtils;

/**
 * Created by v7 on 2016/10/11.
 */

public class InputDialog extends DialogFragment {

      View rootView;

    TextView done;
    EditText inputText;

    OnClickListener onClickListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL,R.style.CustomDialog);
        setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (rootView==null){
            rootView = inflater.inflate(R.layout.fragment_input,null);
        }
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        done = (TextView) view.findViewById(R.id.done);
        inputText = (EditText) view.findViewById(R.id.input);


        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onClick(true,inputText.getText().toString());
            }
        });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) (ScreenUtils.getScreenWidth(getContext())*0.8), DensityUtils.dp2px(getContext(),48));
        params.gravity = Gravity.CENTER;
        int margin = DensityUtils.dp2px(getContext(),8);
        params.setMargins(0,margin,0,margin);
        done.setLayoutParams(params);
    }

    @Override
    public void onResume() {
        super.onResume();
        int width = (int) (ScreenUtils.getScreenWidth(getContext()));
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setLayout(width, height);
    }

    public void setOnClickListener(OnClickListener onClickListener){
        this.onClickListener=onClickListener;
    }

    public interface OnClickListener{
        void onClick(boolean isDone,String input);
    }

}
