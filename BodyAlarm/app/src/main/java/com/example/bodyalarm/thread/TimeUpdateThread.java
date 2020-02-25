package com.example.bodyalarm.thread;

import android.os.Handler;

public class TimeUpdateThread extends Thread{
    private Handler handler;
    public TimeUpdateThread(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            handler.sendEmptyMessage(200);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
