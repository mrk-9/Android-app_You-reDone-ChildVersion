package com.youredone.youredonekids.Utils;

/**
 * Created by 1030 on 4/7/2016.
 */
public interface HttpListener {

    public void onSuccess(String jsonResult);
    public void onFailed(String error);
}
