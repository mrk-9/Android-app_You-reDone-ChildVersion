package com.github.dubu.lockscreenusingservice.service;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.dubu.lockscreenusingservice.Lockscreen;
import com.github.dubu.lockscreenusingservice.LockscreenUtil;
import com.github.dubu.lockscreenusingservice.R;
import com.github.dubu.lockscreenusingservice.SharedPreferencesUtil;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;

import org.w3c.dom.Text;


/**
 * Created by DUBULEE on 15. 5. 20..
 */
public class LockscreenViewService extends Service {
    private final int LOCK_OPEN_OFFSET_VALUE = 50;
    private Context mContext = null;
    private LayoutInflater mInflater = null;
    private View mLockscreenView = null;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParams;
    private RelativeLayout mBackgroundLayout = null;
    private LinearLayout mBackgroundInLayout = null;
    private ImageView mBackgroundLockImageView = null;
    private LinearLayout mForgroundLayout = null;
    private RelativeLayout mStatusBackgruondDummyView = null;
    private RelativeLayout mStatusForgruondDummyView = null;
    private ShimmerTextView mShimmerTextView = null;
    private boolean mIsLockEnable = false;
    private boolean mIsSoftkeyEnable = false;
    private int mDeviceWidth = 0;
    private int mDevideDeviceWidth = 0;
    private float mLastLayoutX = 0;
    private int mServiceStartId = 0;
    private SendMassgeHandler mMainHandler = null;
    public TextView time_txtView;
    public TextView remain_txtView;
    private int hour;
    private int min;
    private int sec;
    final Handler handler = new Handler();
    final Handler btnHandler = new Handler();
    private Button emergency_bttn;
    private Button hangup_btn;
    public Timer timer;

    private class SendMassgeHandler extends android.os.Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            changeBackGroundLockView(mLastLayoutX);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        SharedPreferencesUtil.init(mContext);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mMainHandler = new SendMassgeHandler();
        if (isLockScreenAble()) {
            if (null != mWindowManager) {
                if (null != mLockscreenView) {
                    mWindowManager.removeView(mLockscreenView);
                }
                mWindowManager = null;
                mParams = null;
                mInflater = null;
                mLockscreenView = null;
            }
            initState();
            initView();
            attachLockScreenView();
        }
        return LockscreenViewService.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        dettachLockScreenView();
    }

    private void initState() {

        mIsLockEnable = LockscreenUtil.getInstance(mContext).isStandardKeyguardState();
        if (mIsLockEnable) {
            mParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            mParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
                    PixelFormat.TRANSLUCENT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mIsLockEnable && mIsSoftkeyEnable) {
                mParams.flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            } else {
                mParams.flags = WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;
            }
        } else {
            mParams.flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        }

        if (null == mWindowManager) {
            mWindowManager = ((WindowManager) mContext.getSystemService(WINDOW_SERVICE));
        }
    }

    private void initView() {
        if (null == mInflater) {
            mInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        if (null == mLockscreenView) {
            mLockscreenView = mInflater.inflate(R.layout.freezescreen, null);
        }
    }

    private boolean isLockScreenAble() {
        boolean isLock = SharedPreferencesUtil.get(Lockscreen.ISLOCK);
        if (isLock) {
            isLock = true;
        } else {
            isLock = false;
        }
        return isLock;
    }

    //Start lock screen including slide function
    private void attachLockScreenView() {

        if (null != mWindowManager && null != mLockscreenView && null != mParams) {
            mLockscreenView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                }
            });
            mWindowManager.addView(mLockscreenView, mParams);
            settingLockView();
        }

    }

    //Finish lock screen
    private boolean dettachLockScreenView() {
        if (null != mWindowManager && null != mLockscreenView) {
            mWindowManager.removeView(mLockscreenView);
            mLockscreenView = null;
            mWindowManager = null;
            stopSelf(mServiceStartId);
            return true;
        } else {
            return false;
        }
    }

    //Setting for lock screen and freeze screen
    private void settingLockView() {

//      mForgroundLayout.setOnTouchListener(mViewTouchListener);        //touch the screen to Director
        mStatusForgruondDummyView = (RelativeLayout) mLockscreenView.findViewById(R.id.lockscreen_forground_status_dummy);
        mForgroundLayout = (LinearLayout) mLockscreenView.findViewById(R.id.timeshow_linear);

        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        mDeviceWidth = displayMetrics.widthPixels;
        mDevideDeviceWidth = (mDeviceWidth / 2);

        //Counterdown Value
        time_txtView = (TextView) mLockscreenView.findViewById(R.id.time_textview);
        remain_txtView = (TextView) mLockscreenView.findViewById(R.id.remain_txtView);
        int intTime = Integer.parseInt(Common.fireTime.toString());
        hour = intTime / 60;
        min = intTime % 60;
        sec = 0;
        timer = new Timer();
        MyTimerTask myTimerTask = new MyTimerTask();
        timer.schedule(myTimerTask, new Date(), 1000);

        emergency_bttn = (Button) mLockscreenView.findViewById(R.id.emergency_btn);
        hangup_btn = (Button) mLockscreenView.findViewById(R.id.hangup_btn);
        emergency_bttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWindowManager.removeView(mLockscreenView);
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                callIntent.addFlags(Intent.FLAG_FROM_BACKGROUND);
                callIntent.setData(Uri.parse("tel:" + Common.parentnum));
                if (ActivityCompat.checkSelfPermission(LockscreenViewService.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                startActivity(callIntent);
                mWindowManager.addView(mLockscreenView, mParams);
            }
        });
        hangup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (killCall(mContext)) {
                    Log.d("status", "success");
                } else
                    Log.d("status", "failed");

            }});
        //kitkat
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int val = LockscreenUtil.getInstance(mContext).getStatusBarHeight();
            RelativeLayout.LayoutParams forgroundParam = (RelativeLayout.LayoutParams) mStatusForgruondDummyView.getLayoutParams();
            forgroundParam.height = val;
            mStatusForgruondDummyView.setLayoutParams(forgroundParam);
            AlphaAnimation alpha = new AlphaAnimation(0.5F, 0.5F);
            alpha.setDuration(0); // Make animation instant
            alpha.setFillAfter(true); // Tell it to persist after the animation ends
            mStatusForgruondDummyView.startAnimation(alpha);
        }
    }
    //Hang up function
    public boolean killCall(Context context) {
        try {
            // Get the boring old TelephonyManager
            TelephonyManager telephonyManager =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            // Get the getITelephony() method
            Class classTelephony = Class.forName(telephonyManager.getClass().getName());
            Method methodGetITelephony = classTelephony.getDeclaredMethod("getITelephony");

            // Ignore that the method is supposed to be private
            methodGetITelephony.setAccessible(true);

            // Invoke getITelephony() to get the ITelephony interface
            Object telephonyInterface = methodGetITelephony.invoke(telephonyManager);

            // Get the endCall method from ITelephony
            Class telephonyInterfaceClass =
                    Class.forName(telephonyInterface.getClass().getName());
            Method methodEndCall = telephonyInterfaceClass.getDeclaredMethod("endCall");

            // Invoke endCall()
            methodEndCall.invoke(telephonyInterface);

        } catch (Exception ex) { // Many things can go wrong with reflection calls
            Log.d("Service_call","PhoneStateReceiver **" + ex.toString());
            return false;
        }
        return true;
    }

    private void changeBackGroundLockView(float forgroundX) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (forgroundX < mDeviceWidth) {
                mBackgroundLockImageView.setBackground(getResources().getDrawable(R.drawable.lock));
            } else {
                mBackgroundLockImageView.setBackground(getResources().getDrawable(R.drawable.unlock));
            }
        } else {
            if (forgroundX < mDeviceWidth) {
                mBackgroundLockImageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.lock));
            } else {
                mBackgroundLockImageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.unlock));
            }
        }
    }
    //Call this function in SettingView
    private View.OnTouchListener mViewTouchListener = new View.OnTouchListener() {
        private float firstTouchX = 0;
        private float layoutPrevX = 0;
        private float lastLayoutX = 0;
        private float layoutInPrevX = 0;
        private boolean isLockOpen = false;
        private int touchMoveX = 0;
        private int touchInMoveX = 0;

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN: {// 0
                    firstTouchX = event.getX();
                    layoutPrevX = mForgroundLayout.getX();
                    layoutInPrevX = mBackgroundLockImageView.getX();
                    if (firstTouchX <= LOCK_OPEN_OFFSET_VALUE) {
                        isLockOpen = true;
                    }
                }
                break;
                case MotionEvent.ACTION_MOVE: { // 2
                    if (isLockOpen) {
                        touchMoveX = (int) (event.getRawX() - firstTouchX);
                        if (mForgroundLayout.getX() >= 0) {
                            mForgroundLayout.setX((int) (layoutPrevX + touchMoveX));
                            mBackgroundLockImageView.setX((int) (layoutInPrevX + (touchMoveX / 1.8)));
                            mLastLayoutX = lastLayoutX;
                            mMainHandler.sendEmptyMessage(0);
                            if (mForgroundLayout.getX() < 0) {
                                mForgroundLayout.setX(0);
                            }
                            lastLayoutX = mForgroundLayout.getX();
                        }
                    } else {
                        return false;
                    }
                }
                break;
                case MotionEvent.ACTION_UP: { // 1
                    if (isLockOpen) {
                        mForgroundLayout.setX(lastLayoutX);
                        mForgroundLayout.setY(0);
                        optimizeForground(lastLayoutX);
                    }
                    isLockOpen = false;
                    firstTouchX = 0;
                    layoutPrevX = 0;
                    layoutInPrevX = 0;
                    touchMoveX = 0;
                    lastLayoutX = 0;
                }
                break;
                default:
                    break;
            }

            return true;
        }
    };
    private void optimizeForground(float forgroundX) {
        if (forgroundX < mDevideDeviceWidth) {
            int startPostion = 0;
            for (startPostion = mDevideDeviceWidth; startPostion >= 0; startPostion--) {
                mForgroundLayout.setX(startPostion);
            }
        } else {
            TranslateAnimation animation = new TranslateAnimation(0, mDevideDeviceWidth, 0, 0);
            animation.setDuration(300);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mForgroundLayout.setX(mDevideDeviceWidth);
                    mForgroundLayout.setY(0);
                    dettachLockScreenView();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            mForgroundLayout.startAnimation(animation);
        }
    }
    //Display counterdown-
    class MyTimerTask extends TimerTask {
        @Override
        public void run() {

            if (hour == 0 && min == 0 && sec == 0) {
                btnHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        timer.cancel();
                        timer = null;
                    }
                });
                //destory the freezing screen
                mWindowManager.removeView(mLockscreenView);
                mLockscreenView = null;
                mWindowManager = null;
                stopSelf(mServiceStartId);
            }
            if ((hour > 0 || min >= 0) && sec >= 0) {
                if (sec == 0) {
                    if (min == 0) {
                        hour -= 1;
                        min = 59;
                    } else if (min > 0) {
                        min -= 1;
                    }
                    sec = 59;
                } else if (sec > 0) {
                    sec -= 1;
                }
                if (hour > -1) {
                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            time_txtView.setText(String.format("%02d", hour) + " : " + String.format("%02d", min) + " : " + String.format("%02d", sec));
                            remain_txtView.setText(String.format("%02d", hour) + " : " + String.format("%02d", min) + " : " + String.format("%02d", sec));
                        }
                    });
                }
            }

        }
    }
}
