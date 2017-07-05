package com.youredone.youredonekids.GCM_Service;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.github.dubu.lockscreenusingservice.Lockscreen;
import com.github.dubu.lockscreenusingservice.SharedPreferencesUtil;
import com.youredone.youredonekids.R;
import com.youredone.youredonekids.common.Application;

/**
 * Created by 1030 on 4/7/2016.
 */
public class GcmBroadcastReceiver extends  WakefulBroadcastReceiver{
    public static final int NOTIFICATION_ID = 1;
    private Context mContext = null;
    Application instance;

    @Override
    public void onReceive(Context context, Intent intent) {

        instance = Application.getSharedInstance();
        mContext = context;
        String msg = null;
        String color, end_timestamp, child_address, child_id_str = "", invitation_id_str, invited_child_name_str, invitation_status = "";
        Bundle bundle = intent.getExtras();

        //Get locktime from push notification
        Object condition = bundle.get("locktime");
        Log.d("Push notification",String.valueOf(bundle));
        Object msg1 = bundle.get("msg");

        //Unlock phone-call stopLockscreenService
        if(Integer.parseInt(String.valueOf(condition)) == 0)
        {
            SharedPreferencesUtil.setBoolean(Lockscreen.ISLOCK, false);
            Lockscreen.getInstance(mContext).stopLockscreenService();
        }

        //lock phone when get push notification
        if(Integer.parseInt(String.valueOf(condition)) >= 0)
        {
            SharedPreferencesUtil.setBoolean(Lockscreen.ISLOCK, true);
            Lockscreen.getInstance(mContext).startLockscreenService(String.valueOf(condition));
        }
        //Get current time and saved in Localmemory using SharedPreferences
        Long cur_seconds = System.currentTimeMillis()/1000;
        Log.d("Current time",String.valueOf(cur_seconds));
        long cur_amount_time = Integer.parseInt(String.valueOf(condition));
        long end_time = cur_seconds + cur_amount_time * 60;
        //Saved in localmemory using sharedPreferences
        SharedPreferences settings = context.getSharedPreferences("Kids phone", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("end_time", end_time);
        editor.commit();

        NotificationManager mNM = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent parameter = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, parameter, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mNoti = new NotificationCompat.Builder(context)
                .setContentTitle("Youre done kids phone")
                .setContentText(String.valueOf(msg1))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("lock phone")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
        mNM.notify(NOTIFICATION_ID, mNoti.build());
        // Explicitly specify that GcmIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(), GcmIntentService.class.getName());
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, intent.setComponent(comp));
        setResultCode(Activity.RESULT_OK);
    }
}
