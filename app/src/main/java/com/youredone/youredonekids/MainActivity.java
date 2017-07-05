package com.youredone.youredonekids;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.github.dubu.lockscreenusingservice.Lockscreen;
import com.github.dubu.lockscreenusingservice.SharedPreferencesUtil;
import com.github.dubu.lockscreenusingservice.service.Common;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.loopj.android.http.RequestParams;
import com.youredone.youredonekids.Utils.Childapi;
import com.youredone.youredonekids.Utils.Constants;
import com.youredone.youredonekids.Utils.PowerReceiver;
import com.youredone.youredonekids.common.Application;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends Activity implements Childapi.ApiSyncHandler {

    private RelativeLayout hidden1_liner;
    private RelativeLayout timeshow_liner;
    private Button okButton;
    private Button cancelButton;
    private EditText name_edittxt;
    private EditText passcode_edittxt;
    private EditText phonenum_edittxt;
    private static final String SENDER_ID = "990767929810";
    private GoogleCloudMessaging _gcm;
    private String _regId = "";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    ProgressDialog progress;
    private Context mContext = null;
    Application instance;

    public static int OVERLAY_PERMISSION_REQ_CODE = 1234;
    public static int REQUEST_CALL_PHONE_CODE = 1235;
    public static int REQUEST_RECEIVE_BOOT_COMPLETED = 1236;
    public static int REQUEST_SYSTEM_ALERT_WINDOW = 1237;

    //android OS 6.0 permerssion(SYSTEMALERTWINDOW permission)
    public void someMethod() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)   {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = Application.getSharedInstance();

          //identify the power button
//        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
//        filter.addAction(Intent.ACTION_SCREEN_OFF);
//        filter.addAction(Intent.ACTION_USER_PRESENT);
//        PowerReceiver myReceiver = new PowerReceiver();
//        registerReceiver(myReceiver, filter);

        someMethod();

        //Call permission automatically
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                        Manifest.permission.CALL_PHONE)) {

                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CALL_PHONE},
                            REQUEST_CALL_PHONE_CODE);
                }
            }
        }

        mContext = this;
        setContentView(R.layout.activity_main);

        timeshow_liner = (RelativeLayout)findViewById(R.id.timeshow_linear);
        timeshow_liner.setVisibility(LinearLayout.GONE);
        okButton = (Button)findViewById(R.id.ok_btn);
        cancelButton = (Button)findViewById(R.id.cancel_btn);
        hidden1_liner = (RelativeLayout) findViewById(R.id.hidden1_linear);

        //Login page is only showed once
        SharedPreferencesUtil.init(this);
        boolean isLogged = SharedPreferencesUtil.get(Lockscreen.ISLOGGED);
        if (isLogged)   {
            hidden1_liner.setVisibility(View.GONE);
        }
        //get seconds from localmemory using sharedPreferences
        SharedPreferences settings = this.getSharedPreferences("Kids phone", 0);
        long end_time = settings.getLong("end_time", 0);

        if (end_time > 0)   {

            Long seconds = System.currentTimeMillis()/1000;
            if((end_time - seconds) > 0) {
                Lockscreen.getInstance(mContext).startLockscreenService(String.valueOf((end_time - seconds)/60));
            }
            else
            {
                SharedPreferences.Editor editor = settings.edit();
                editor.putLong("end_time", 0);
                editor.commit();
            }
        }

        //Get Device_Token
        if (checkPlayServices()) {
            _gcm = GoogleCloudMessaging.getInstance(this);
            _regId = getDevice_token();
            Log.d("Device token", _regId);
            if (_regId.equals(""))
            {
                registerInBackground();
                progress = ProgressDialog.show(MainActivity.this,"Loading","Please wait",true);
                okButton.setEnabled(false);
            }
        } else {

        }

        cancelButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String check_device_token = getDevice_token();
                if(check_device_token.equals("")) {
                    showFailedAlert("Sorry.You are not able to use this application.Please check your network connection first");
                }
                else {

                    name_edittxt = (EditText) findViewById(R.id.name_edittxt);
                    passcode_edittxt = (EditText) findViewById(R.id.passcode_edittxt);
                    phonenum_edittxt = (EditText) findViewById(R.id.phonenum_edittxt);
                    if (name_edittxt.getText().length() <= 0) {
                        showFailedAlert("Please enter valid name");
                    } else if (passcode_edittxt.getText().length() <= 0)
                        showFailedAlert("Please enter valid passcode");
                    else if (phonenum_edittxt.getText().length() <= 0) {
                        showFailedAlert("Please enter valid parent phone number");
                    } else {
                        Common.parentnum = phonenum_edittxt.getText().toString();
                        // Save the phone number into localmemory
                        SharedPreferences phonenum = MainActivity.this.getSharedPreferences("Kids phone", 0);
                        SharedPreferences.Editor editor = phonenum.edit();
                        editor.putString("kid's num", phonenum_edittxt.getText().toString());
                        editor.commit();

                        Log.d("phoneNumber", Common.parentnum);
                        progress = ProgressDialog.show(MainActivity.this, "Connecting to the server", "Please wait", true);
                        //Api connection
                        String url = Constants.HOST_URL + Constants.CHILD_API_URL;
                        RequestParams params = new RequestParams();
                        params.put("name", name_edittxt.getText().toString());
                        params.put("passcode", passcode_edittxt.getText().toString());
                        params.put("token", _regId);
                        params.put("platform", "2");
                        Childapi api = new Childapi(url, params, MainActivity.this);
                        api.syncObject();
                    }
                }
            }
        });
        Button emergency_btn = (Button)findViewById(R.id.emergency_btn);
        emergency_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                callIntent.setData(Uri.parse("tel:" + phonenum_edittxt.getText().toString()));
                startActivity(callIntent);
            }
        });

        //To get phone number, call localmemory
        SharedPreferences phonenum = this.getSharedPreferences("Kids phone", 0);
        Common.parentnum = phonenum.getString("kid's num", "");
        if(Common.parentnum.length() < 0)
            showFailedAlert("Please enter a phone number");
    }

    private void showSuccessAlert(String message)  {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Help");
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
    private void showFailedAlert(String message)  {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Error");
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    //Device Token Function
    public String getDevice_token(){
        SharedPreferences settings = this.getSharedPreferences("Kids phone", 0);
        return settings.getString("device_token", "");
    }
    public void setDevice_token(String token){
        SharedPreferences settings = this.getSharedPreferences("Kids phone", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("device_token", token);
        editor.commit();
    }
    // google play service
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                finish();
            }
            return false;
        }
        return true;
    }
    //Connection to the Google Cloud Messaging
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>()
        {
            @Override
            protected String doInBackground(Void... params)
            {
                String msg = "";
                try
                {
                    if (_gcm == null)
                    {
                        _gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    _regId = _gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + _regId;
                }
                catch (IOException ex)
                {
                    msg = "Error :" + ex.getMessage();

                }
                return _regId;
            }

            //When get the device_token
            @Override
            protected void onPostExecute(String msg)
            {
                setDevice_token(msg);
                progress.dismiss();
                if(msg.length() == 0) {
                    showFailedAlert("Network connection Failed");
                }
                okButton.setEnabled(true);
            }
        }.execute(null, null, null);
    }

    //if result with connecting Api is success
    @Override
    public void success(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            if(jsonObject.getString("status").equals("OK"))
            {
                showSuccessAlert("The user has been registered on the server. You can lock this phone from parent phone");
                //Hidden LinearLayout
                hidden1_liner.setVisibility(LinearLayout.GONE);
                //To show Login page once, set SharedPreferenceUtil value = true
                SharedPreferencesUtil.setBoolean(Lockscreen.ISLOGGED, true);
                progress.dismiss();
            }else
            {
                showFailedAlert("The user already exists");
                progress.dismiss();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //if result with connecting APi is failed
    @Override
    public void failed(String response, Throwable throwable) {

        showFailedAlert("Sorry.Faild. Please confirm the network status");
        progress.dismiss();
    }

    public boolean hexChecker(char c) {
        String string = "0123456789abcdefABCDEF";
        return string.indexOf(c) > -1;
    }
//    //Disable back button
//    @Override
//    public void onBackPressed() {
//    }
//    //Disable back button
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        return false;
//    }
}
