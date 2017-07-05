package com.youredone.youredonekids.Utils;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by RED on 6/27/2016.
 */
public class PowerReceiver extends BroadcastReceiver {
    private static int countPowerOff = 0;

    public PowerReceiver ()
    {

    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
        {
            Toast.makeText(context, "MAIN ACTIVITY IS BEING CALLED(off) ", Toast.LENGTH_LONG).show();
        }
        else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
        {
            Toast.makeText(context, "MAIN ACTIVITY IS BEING CALLED(on) ", Toast.LENGTH_LONG).show();
        }

    }
}
