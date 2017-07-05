package com.youredone.youredonekids;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by win on 5/1/2016.
 */
public class UpdateService extends Service {

    BroadcastReceiver mReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        // register receiver that handles screen on and screen off logic
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new MyReceiver();
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onDestroy() {

        unregisterReceiver(mReceiver);
        Log.i("onDestroy Reciever", "Called");

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return START_NOT_STICKY;
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO Auto-generated method stub
        return null;
    }
}
