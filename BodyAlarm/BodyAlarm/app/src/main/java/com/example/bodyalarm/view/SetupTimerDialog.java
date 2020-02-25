package com.example.bodyalarm.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.example.bodyalarm.R;


public class SetupTimerDialog extends Dialog implements View.OnClickListener {

    private View view;
    private Context context;

    private LinearLayout parentLayout;
    private Spinner spinner;
    private TextView tvTime;

    private Toast toast;

    private DialogButtonClickListener listener;
    //默认时间参数
    private int value = 3;

    public SetupTimerDialog(@NonNull Context context) {
        this(context, 0, null);
    }

    private SetupTimerDialog(Context context, int theme, View contentView) {
        super(context, theme == 0 ? R.style.TimeDialogStyle : theme);
        this.view = contentView;
        this.context = context;
        if (view == null) {
            view = View.inflate(context, R.layout.view_timer_dialog, null);
        }
        this.setContentView(view);
        initView();
        setLayoutParams();
        setCanceledOnTouchOutside(true);
    }

    private void initView() {
        parentLayout = view.findViewById(R.id.parent_layout);

        Button btnAdd = findViewById(R.id.btn_add);
        Button btnMinus = findViewById(R.id.btn_minus);

        tvTime = findViewById(R.id.tv_time);

        spinner = view.findViewById(R.id.sp_time_type);

        TextView tvSave = view.findViewById(R.id.tv_save);
        TextView tvCancel = view.findViewById(R.id.tv_cancel);

        toast = Toast.makeText(context,"",Toast.LENGTH_SHORT);

        tvTime.setText((value < 10) ? "0" + String.valueOf(value) : String.valueOf(value));

        btnAdd.setOnClickListener(this);
        btnMinus.setOnClickListener(this);
        tvSave.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
    }


    private void setLayoutParams() {
        parentLayout.setLayoutParams(new FrameLayout.LayoutParams((int) (getMobileWidth(context) * 0.5),
                LayoutParams.WRAP_CONTENT));
    }

    private static int getMobileWidth(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    public interface DialogButtonClickListener {
        void onLeftButtonClicked(int inputValue, int type);
    }

    public void setDialogButtonClickListener(DialogButtonClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add:
                if(value >= 99) {
                    toast.setText("时间最大 99");
                    toast.show();
                }else {
                    value++;
                    tvTime.setText((value < 10) ? "0" + String.valueOf(value) : String.valueOf(value));
                }
                break;
            case R.id.btn_minus:
                if(value <= 1) {
                    toast.setText("时间最小 1");
                    toast.show();
                }else {
                    value--;
                    tvTime.setText((value < 10) ? "0" + String.valueOf(value) : String.valueOf(value));
                }
                break;
            case R.id.tv_save:
                if(listener == null) {
                    dismiss();
                    return;
                }
                listener.onLeftButtonClicked(value, spinner.getSelectedItemPosition());
                dismiss();
                break;
            case R.id.tv_cancel:
                dismiss();
                break;
        }
    }
}
