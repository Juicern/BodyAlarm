package com.example.bodyalarm.activity;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.bodyalarm.R;
import com.example.bodyalarm.service.AlarmService;
import com.example.bodyalarm.thread.ConnectTask;
import com.example.bodyalarm.thread.TimeUpdateThread;
import com.example.bodyalarm.utils.Const;
import com.example.bodyalarm.view.ColorToast;
import com.example.bodyalarm.view.GifView;
import com.example.bodyalarm.view.SetupTimerDialog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout parentLayout;

    private EditText edSensorIP,edSensorPort;
    private EditText edBuzzerIP,edBuzzerPort;
    private EditText edPhoneNum,edSmsContent;

    private TextView tvInfo;
    private TextView tvWifiState,tvTime;

    private GifView gifView;
    private ColorToast colorToast;
    private SetupTimerDialog timeDialog;

    private Button btnConnect;
    private Button btnSetTimer;
    private boolean runState = false;

    private final Timer timer = new Timer();
    private ConnectTask connectTask;

    private TimeUpdateThread timeUpdateThread;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private BroadcastReceiver broadcastReceiver;

    private Drawable wifiOpenIcon,wifiCloseIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //主界面全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        //初始化相关
        initView();
        initParams();

        //初始化广播接收器
        //在onReceive接口中处理wifi状态改变事件
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                    switch (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)) {
                        case WifiManager.WIFI_STATE_DISABLED:
                            updateWifiIconState("已关闭", false);
                            break;
                        case WifiManager.WIFI_STATE_ENABLED:
                            //wifi打开后延时3秒
                            //等待设备连接上已保存的无线网络
                            //再更新信息显示
                            mainHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    checkWifiState();
                                }
                            },3000);
                            break;
                        case WifiManager.WIFI_STATE_UNKNOWN:
                            updateWifiIconState("未知网络", false);
                            break;
                    }
                }
            }
        };

        //wifi状态更新广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(broadcastReceiver,filter);

        // 定时检查是否有人才发送短信
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // 发送短信
                if (Const.BODY != null && Const.BODY && Const.SMS) {
                    SmsManager manager = SmsManager.getDefault();
                    String phone = edPhoneNum.getText().toString();
                    String content = edSmsContent.getText().toString();
                    if (isCanUseSim() && manager != null && !phone.isEmpty() && !content.isEmpty()) {
                        manager.sendTextMessage(phone, null, content, null, null);
                        Const.SMS = false;// 只发送一次，发送完置false
                    } else {
                        colorToast.showHint("短信发送失败!");
                    }
                }
            }
        };
        timer.schedule(task, 2000, 2000); // 定时

        //由services启动时自动运行
        Intent intent = getIntent();
        if(intent != null) {
            int auto = intent.getIntExtra("auto",-1);
            if(auto == 200) {
                btnConnect.setText("断开");
                btnConnect.setTextColor(Color.RED);
                onConnectClicked();
            }
        }
    }

    /**
     * 初始化界面组件
     * */
    private void initView() {
        parentLayout = findViewById(R.id.layout_parent);

        edSensorIP = findViewById(R.id.ed_sensor_ip);
        edSensorPort = findViewById(R.id.ed_sensor_port);
        edBuzzerIP = findViewById(R.id.ed_speaker_ip);
        edBuzzerPort = findViewById(R.id.ed_speaker_port);
        edPhoneNum = findViewById(R.id.ed_phone_number);
        edSmsContent = findViewById(R.id.ed_sms_content);

        btnConnect = findViewById(R.id.btn_connect);
        btnSetTimer = findViewById(R.id.btn_set_timer);

        tvInfo = findViewById(R.id.tv_info);
        gifView = findViewById(R.id.gifview);

        tvWifiState = findViewById(R.id.tv_wifi_state);
        tvTime = findViewById(R.id.tv_time);

        colorToast = new ColorToast(MainActivity.this);
    }

    /**
     * 初始化参数配置
     * */
    private boolean serviceState;
    private void initParams() {
        tvInfo.setText("请点击连接!");

        btnConnect.setOnClickListener(this);
        btnSetTimer.setOnClickListener(this);
        tvWifiState.setOnClickListener(this);
        parentLayout.setOnClickListener(this);

        //启动时间显示线程
        timeUpdateThread = new TimeUpdateThread(handler);
        timeUpdateThread.start();

        //wifi可用图标
        wifiOpenIcon = getResources().getDrawable(R.mipmap.ic_wifi_open);
        wifiOpenIcon.setBounds(0, 0, wifiOpenIcon.getMinimumWidth(), wifiOpenIcon.getMinimumHeight());
        //wifi不可用图标
        wifiCloseIcon = getResources().getDrawable(R.mipmap.ic_wifi_close);
        wifiCloseIcon.setBounds(0, 0, wifiCloseIcon.getMinimumWidth(), wifiCloseIcon.getMinimumHeight());
        //检查设备wifi连接状态
        checkWifiState();
        //判断定时任务服务开启状态
        serviceState = isServiceRunning(MainActivity.this,
                "com.example.bodyalarm.service.AlarmService");
        if(serviceState) {
            colorToast.showHint("定时任务运行中");
        }
    }

    /**
     * 连接按钮点击事件
     * */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_connect:
                //连接按钮按下，执行连接任务
                onConnectClicked();
                break;
            case R.id.btn_set_timer:
               onSetupTimeClicked();
                break;
            case R.id.tv_wifi_state:
                //点击wifi状态信息栏，刷新wifi连接信息
                checkWifiState();
                colorToast.showHint("已更新网络信息");
                break;
            case R.id.layout_parent:
                //点击空白区隐藏输入法
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(edSmsContent.getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                break;
            default:
                break;
        }
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.what == 200) {
                //更新时间显示
                tvTime.setText(formatTime());
            }
            return true;
        }
    });

    /**
     * 双击返回键退出
     * */
    private long firstTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN){
            if(System.currentTimeMillis() - firstTime > 2000){
                colorToast.showHint("再按一次「返回」将退出程序");
                firstTime = System.currentTimeMillis();
            }else{
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void onConnectClicked() {
        runState = !runState;
        if(runState) {
            // 获取IP和端口
            String BODY_IP = edSensorIP.getText().toString().trim();
            String BODY_PORT = edSensorPort.getText().toString().trim();
            String BUZZER_IP = edBuzzerIP.getText().toString().trim();
            String BUZZER_PORT = edBuzzerPort.getText().toString().trim();

            if(checkIpPort(BODY_IP, BODY_PORT) && checkIpPort(BUZZER_IP, BUZZER_PORT)){
                Const.BODY_IP = BODY_IP;
                Const.BODY_PORT = Integer.parseInt(BODY_PORT);
                Const.BUZZER_IP = BUZZER_IP;
                Const.BUZZER_PORT = Integer.parseInt(BUZZER_PORT);
            }else{
                colorToast.showHint("配置信息不正确,请重输！",ColorToast.WARNING);
                return;
            }

            // 显示动画
            gifView.setVisibility(View.VISIBLE);

            // 开启任务
            connectTask = new ConnectTask(MainActivity.this, tvInfo, gifView);
            connectTask.setCIRCLE(true);
            connectTask.execute();

            btnConnect.setText("断开");
            btnConnect.setTextColor(Color.RED);
        }else {
            // 取消动画
            gifView.setVisibility(View.INVISIBLE);
            // 取消任务
            if (connectTask != null && connectTask.getStatus() == AsyncTask.Status.RUNNING) {
                connectTask.setCIRCLE(false);
                // 如果Task还在运行，则先取消它
                connectTask.cancel(true);
                connectTask.closeSocket();
            }
            btnConnect.setText("连接");
            btnConnect.setTextColor(Color.WHITE);

            tvInfo.setText("请点击连接！");
            tvInfo.setTextColor(Color.DKGRAY);
        }
    }

    private void onSetupTimeClicked() {
        if(timeDialog == null) {
            timeDialog = new SetupTimerDialog(MainActivity.this);
            timeDialog.setDialogButtonClickListener(new SetupTimerDialog.DialogButtonClickListener() {
                @Override
                public void onLeftButtonClicked(int inputValue, int type) {
                    //stop last service
                    if(serviceState) {
                        stopService(new Intent(MainActivity.this,AlarmService.class));
                        serviceState = false;
                    }
                    int realTime = (type == 0) ? inputValue : inputValue * 60;
                    writeTime(realTime);
                    Intent startIntent = new Intent(MainActivity.this, AlarmService.class);
                    startIntent.putExtra("minutes",realTime);
                    startService(startIntent);
                    serviceState = true;
                    colorToast.showHint("设定成功！");
                }
            });
        }
        //弹出对话框
        timeDialog.show();
    }

    /**
     * 检查wifi状态
     */
    private void checkWifiState() {
        WifiManager wifiMgr = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        switch (wifiMgr.getWifiState()) {
            case WifiManager.WIFI_STATE_DISABLED:
                updateWifiIconState("已关闭", false);
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                ConnectivityManager connManager = (ConnectivityManager)getApplicationContext()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if(networkInfo.isConnected()) {
                    //wifi打开并且连接，显示当前连接名称
                    updateWifiIconState(getWifiSSID(), true);
                }else {
                    updateWifiIconState("未连接", false);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 更新wifi连接图标状态
     * @param ssid 连接的wifi信号名称
     * @param wifiEnable wifi是否可用
     * */
    private void updateWifiIconState(String ssid, boolean wifiEnable) {
        tvWifiState.setText(": "  + ssid);
        tvWifiState.setCompoundDrawables(wifiEnable ? wifiOpenIcon : wifiCloseIcon,
                null, null, null);
    }

    /**
     * 获取wifi SSID
     * 需要根据设备安卓系统版本适配
     * @return 获取的wifi ssid
     */
    public String getWifiSSID() {
        //android 8.0及其以下，android 9.0
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O || Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
            WifiManager mWifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if(mWifiManager != null) {
                WifiInfo info = mWifiManager.getConnectionInfo();
                return info.getSSID();
            }
        //android 8.0 MR1版本需要特殊处理
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1){
            ConnectivityManager connManager = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if(connManager != null) {
                NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
                if (networkInfo.isConnected()) {
                    if (networkInfo.getExtraInfo()!=null){
                        return networkInfo.getExtraInfo().replace("\"","");
                    }
                }
            }
        }
        return "未知网络";
    }

    /**
     * IP地址可用端口号验证，可用端口号（1024-65536）
     * @param ip IP地址
     * @param port 端口号
     * @return 是否通过检查
     */
    private static boolean checkIpPort(String ip, String port){
        boolean ipPass,portPass;

        //check ip or port isEmpty
        if(ip == null || ip.isEmpty()) {
            return false;
        }
        if(port == null || port.isEmpty()) {
            return false;
        }
        //check ip or port length
        if( ip.length() < 7 || ip.length() > 15 || port.length() < 4 || port.length() > 5) {
            return false;
        }
        //判断IP格式和范围
        String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";

        Pattern pat = Pattern.compile(rexp);
        Matcher mat = pat.matcher(ip);
        ipPass = mat.find();
        //判断端口
        int portNum;
        try {
            portNum = Integer.parseInt(port);
        }catch (NumberFormatException e) {
            return false;
        }
        portPass = (portNum > 1024 && portNum < 65536);

        return (ipPass && portPass);
    }

    /**
     * sim卡是否可读
     * @return 是否可读
     */
    public boolean isCanUseSim() {
        try {
            TelephonyManager mgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            return TelephonyManager.SIM_STATE_READY == mgr.getSimState();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取当前的时间信息
     * @return 时间格式化后的文本
     * */
    public static String formatTime() {
        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss",Locale.CHINA);
        return format.format(new Date(System.currentTimeMillis()));
    }


    /**
     * 判断定时任务服务是否正在运行
     * */
    private boolean isServiceRunning(Context context, String serviceName){
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        if(am == null){
            return false;
        }
        List<ActivityManager.RunningServiceInfo> services = am.getRunningServices(100);
        for (ActivityManager.RunningServiceInfo info : services) {
            String name = info.service.getClassName();
            if (serviceName.equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 保存用户输入的任务定时时间
     * */
    private void writeTime(int minutes){
        SharedPreferences sharedPreferences = this.getSharedPreferences("app_env", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("minutes",minutes);
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        colorToast.cancel();
        timeUpdateThread.interrupt();
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
