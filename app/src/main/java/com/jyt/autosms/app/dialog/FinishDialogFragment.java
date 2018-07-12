package com.jyt.autosms.app.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jyt.autosms.R;
import com.jyt.autosms.util.DensityUtils;
import com.jyt.autosms.util.ScreenUtils;

/**
 * Created by v7 on 2016/10/20.
 */

public class FinishDialogFragment extends DialogFragment {
    OnDoneClickListener onDoneClickListener;
    TextView done;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL,R.style.CustomDialog);
        setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_finish_dialog,null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        done = (TextView) view.findViewById(R.id.done);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) (ScreenUtils.getScreenWidth(getContext())*0.8), DensityUtils.dp2px(getContext(),48));
        params.gravity = Gravity.CENTER;
        int margin = DensityUtils.dp2px(getContext(),8);
        params.setMargins(0,margin,0,margin);
        done.setLayoutParams(params);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onDoneClickListener!=null)
                    onDoneClickListener.onDoneClick();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        int width = (int) (ScreenUtils.getScreenWidth(getContext()));
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setLayout(width, height);
    }

    public void setOnDoneClickListener(OnDoneClickListener onDoneClickListener) {
        this.onDoneClickListener = onDoneClickListener;
    }

    public interface OnDoneClickListener{
        void onDoneClick();
    }
}
