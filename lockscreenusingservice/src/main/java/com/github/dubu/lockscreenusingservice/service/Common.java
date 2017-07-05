package com.github.dubu.lockscreenusingservice.service;

import android.content.Context;
import android.content.Intent;

/**
 * Created by 1030 on 4/11/2016.
 */
public class Common {

   static public String fireTime = "";
   static public Context context = null;
   static public String parentnum = "";

   static public void stopLockscreenService(Context mContext) {
      Intent stopLockscreenViewIntent =  new Intent(mContext, LockscreenViewService.class);
      mContext.stopService(stopLockscreenViewIntent);
      Intent stopLockscreenIntent =  new Intent(mContext, LockscreenService.class);
      mContext.stopService(stopLockscreenIntent);
   }
}
