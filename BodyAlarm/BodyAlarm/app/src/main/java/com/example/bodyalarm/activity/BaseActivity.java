package com.example.bodyalarm.activity;

import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
/**
 * baseActivity抽象类，本应用的基础activity
 * 提供初始化部分的声明，
 * 子类必须自己实现对应的功能
 * */
public abstract class BaseActivity extends AppCompatActivity {
    /**
     * 子类调用时必须先setContentView
     * 再调用父类的onCreate方法
     * */
    protected void init(int layoutId) {
        //在android5.0以上系统修改底部虚拟按键
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            //修改底部虚拟按键栏颜色
            getWindow().setNavigationBarColor(Color.parseColor("#1FA9F1"));
            //修改顶栏透明
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(layoutId);
        initView();
        initParams();
    }

    /**
     * 初始化布局元素相关
     * */
    public abstract void initView();

    /**
     * 初始化数据相关
     * */
    public abstract void initParams();
}
