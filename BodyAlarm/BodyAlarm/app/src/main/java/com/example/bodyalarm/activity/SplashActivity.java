package com.example.bodyalarm.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.example.bodyalarm.R;
/**
 * 应用启动第一屏
 * 对于安卓6.0以上设备进行动态申请
 * */
public class SplashActivity extends BaseActivity {

    private final String[] PERMISSIONS = new String[] {Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.SEND_SMS,
            Manifest.permission.WAKE_LOCK};

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int DELAY_TIME = 500;

    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private Toast toast;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.init(R.layout.activity_splash);
        //设备系统为android 6.0以上需要动态申请权限
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M){
            requestPremise();
        }else{
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    doStart();
                }
            },SplashActivity.DELAY_TIME);
        }
    }

    @Override
    public void initView() {
        //初始化toast
        toast = Toast.makeText(SplashActivity.this,"",Toast.LENGTH_SHORT);
    }

    @Override
    public void initParams() {
    }

    /**
     * 显示弹窗提示
     * @param msg 消息内容
     * */
    private void showToast(String msg) {
        toast.setText(msg);
        toast.show();
    }

    /**
     * android6.0及其以上权限请求
     * */
    private void requestPremise() {
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean isAllGranted = checkPermissionAllGranted(PERMISSIONS);
                if (isAllGranted) {
                    doStart();
                }
                ActivityCompat.requestPermissions(
                        SplashActivity.this,
                        PERMISSIONS,
                        PERMISSION_REQUEST_CODE
                );
            }
        }, SplashActivity.DELAY_TIME);
    }

    /**
     * 检查需要的权限是否授予
     * @param permissions 权限描述字符串数组
     * @return state 权限是否都获取
     * */
    private boolean checkPermissionAllGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 权限请求结果回调
     * @param requestCode 请求码
     * @param permissions 权限文本数组
     * @param grantResults 各权限获取状态
     * */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean isAllGranted = true;
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }
            if (isAllGranted) {
                doStart();
            }else{
                showToast("授权未完成,请重新打开应用");
            }
        }
    }

    /**
     * 跳转主页面
     * */
    private void doStart() {
        startActivity(new Intent(SplashActivity.this,MainActivity.class));
        finish();
    }
}
