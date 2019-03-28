package com.cdwoo.demowoo;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by cd on 2019/1/5.
 */

public class UploadDataService extends Service {
    @Override
    public void onCreate() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
        MyBroadcastReceiver receiver = new MyBroadcastReceiver();
        registerReceiver(receiver, filter);

        Log.i("Kathy","onCreate - Thread ID = " + Thread.currentThread().getId());
        super.onCreate();
        //开启一个线程，每半分钟上传一次数据
        new Timer().schedule(new TimerTask() {
            InputStream is = null;
            @Override
            public void run() {
                Socket sc = null;
                try {
                    sc = new Socket("139.159.154.26", 3232);
                    PrintWriter pw = new PrintWriter(sc.getOutputStream());
                    pw.println("01 01 01 01 01 01 01");
                    pw.flush();
                    pw.close();
                    sc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 1000 , 30*1000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("Kathy", "onStartCommand - startId = " + startId + ", Thread ID = " + Thread.currentThread().getId());
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("Kathy", "onBind - Thread ID = " + Thread.currentThread().getId());
        return null;
    }

    @Override
    public void onDestroy() {
        Log.i("Kathy", "onDestroy - Thread ID = " + Thread.currentThread().getId());
        super.onDestroy();
    }
}
