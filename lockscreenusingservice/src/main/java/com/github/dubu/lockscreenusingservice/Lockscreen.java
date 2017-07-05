package com.github.dubu.lockscreenusingservice;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.dubu.lockscreenusingservice.service.Common;
import com.github.dubu.lockscreenusingservice.service.LockscreenService;
import com.github.dubu.lockscreenusingservice.service.LockscreenViewService;

/**
 * Created by DUBULEE on 15. 5. 20..
 */
public class Lockscreen {
    private Context mContext = null;
    public static final String ISSOFTKEY = "ISSOFTKEY";
    public static final String ISLOCK = "ISLOCK";
    public static final String ISLOGGED = "ISLOGGED";
    private static Lockscreen mLockscreenInstance;
    public static Lockscreen getInstance(Context context) {
        if (mLockscreenInstance == null) {
            if (null != context) {
                mLockscreenInstance = new Lockscreen(context);
            }
            else {
                mLockscreenInstance = new Lockscreen();
            }
        }
        return mLockscreenInstance;
    }

    private Lockscreen() {
        mContext = null;
    }

    private Lockscreen(Context context) {
        mContext = context;
        Common.context = context;
    }

    public void startLockscreenService(String fireTime) {

        //When get the number for downcount
        Common.fireTime = fireTime;
        Log.d("phonenumber", Common.parentnum);
        SharedPreferencesUtil.init(mContext);
        Intent startLockscreenIntent = new Intent(mContext, LockscreenService.class);
//        startLockscreenIntent.putExtra(LockscreenService.LOCKSCREENSERVICE_FIRST_START, true);
        mContext.startService(startLockscreenIntent);
    }
    public void stopLockscreenService() {
        Intent stopLockscreenViewIntent =  new Intent(mContext, LockscreenViewService.class);
        mContext.stopService(stopLockscreenViewIntent);
        Intent stopLockscreenIntent =  new Intent(mContext, LockscreenService.class);
        mContext.stopService(stopLockscreenIntent);
    }
}
