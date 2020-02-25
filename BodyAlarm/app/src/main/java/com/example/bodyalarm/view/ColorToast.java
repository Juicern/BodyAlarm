package com.example.bodyalarm.view;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bodyalarm.R;

public class ColorToast {
    private Toast toast;
    private TextView text;

    public static final int WARNING = 0;
    public static final int MESSAGE = 1;
    public static final int DEFAULT = 2;

    public ColorToast(Activity activity) {
        toast = new Toast(activity.getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        View toastRoot = activity.getLayoutInflater().inflate(R.layout.view_color_toast, null);
        text = toastRoot.findViewById(R.id.tv_toast);
        toast.setView(toastRoot);
    }

    public void showHint(String msg){
        this.showHint(msg,DEFAULT);
    }

    public void showHint(String msg,int type){
        if(type == MESSAGE){
            text.setBackgroundColor(Color.parseColor("#ff99cc00"));
        }else if(type == WARNING){
            text.setBackgroundColor(Color.parseColor("#ffff4444"));
        }else{
            text.setBackgroundColor(Color.parseColor("#ff33b5e5"));
        }
        text.setText(msg);
        toast.show();
    }

    public void cancel() {
        toast.cancel();
    }

}
