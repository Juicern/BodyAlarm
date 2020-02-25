package com.example.bodyalarm.service;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;

import com.example.bodyalarm.activity.MainActivity;

import java.util.List;

public class AlarmService extends Service {

    private boolean flag = false;

    public AlarmService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int realTime = 0;
        //如果改应用因手机内存不足kill掉此Service
        //未来的某个时间内，当系统内存足够可用的情况下，系统将会尝试重新创建此Service
        //Intent将是null需要进行分别处理
        if(intent != null){
            //intent 不为null 直接从intent读取定时时间
            realTime = intent.getIntExtra("minutes",-1);
        }
        if(realTime == -1){
            //intent二次提醒后将丢失内部数据，需要从应用记录的SharedPreferences读取定时时间
            SharedPreferences sharedPreferences = this.getSharedPreferences("app_env", MODE_PRIVATE);
            realTime = sharedPreferences.getInt("minutes",0);
        }

        if(flag) {
            handler.sendEmptyMessage(100);
        }

        long triggerAtTime;
        //计算出未来提醒的时间(毫秒数)
        if(realTime >= 60){
            int numOfHour = realTime / 60;
            int leftOfMinute = realTime % 60;
            triggerAtTime = numOfHour * 3600 * 1000 + leftOfMinute * 60 * 1000;
        }else{
            triggerAtTime = realTime * 60 * 1000;
        }

        //设置提醒时间 = 当前时间 + 计算出未来提醒的时间(毫秒数)
        triggerAtTime += System.currentTimeMillis();

        if(!flag) {
            flag = true;
            Intent i = new Intent(this,AlarmReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(this,0,i,PendingIntent.FLAG_UPDATE_CURRENT);
            manager.set(AlarmManager.RTC_WAKEUP,triggerAtTime,pi);
            manager.setExact(AlarmManager.RTC_WAKEUP,triggerAtTime,pi);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.what==100) {
                //震动提醒
                Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                long [] pattern = {100,500,100,500};
                vibrator.vibrate(pattern,-1);

                //获取当前所有活动app
                ActivityManager manager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningAppProcessInfo> processInfos = manager.getRunningAppProcesses();
                for(ActivityManager.RunningAppProcessInfo processInfo : processInfos) {
                    if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        //当前正打开的界面为本app时不做跳转
                        if(getPackageName().equals(processInfo.processName)) {
                            processInfos.clear();
                            stopSelf();
                            return true;
                        }
                    }
                }
                processInfos.clear();

                //跳转至应用主界面
                Intent intent = new Intent(AlarmService.this,MainActivity.class);
                intent.putExtra("auto",200);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                stopSelf();
            }
            return true;
        }
    });

    public void onDestroy()  {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
