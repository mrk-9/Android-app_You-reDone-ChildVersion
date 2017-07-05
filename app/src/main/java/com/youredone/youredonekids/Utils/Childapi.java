package com.youredone.youredonekids.Utils;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
/**
 * Created by 1030 on 4/7/2016.
 */
public class Childapi {

    private String url;
    private RequestParams params;

    public interface ApiSyncHandler {
        public void success(String response);
        public void failed(String response, Throwable throwable);
    }

    public ApiSyncHandler handler = null;

    public Childapi() {}

    public Childapi(String mUrl, RequestParams mParams, ApiSyncHandler mHandler) {

        this.url = mUrl;
        this.params = mParams;
        this.handler = mHandler;
    }

    public void syncObject()    {

        AsyncHttpClient client = new AsyncHttpClient();

        client.post(this.url, this.params, new TextHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {
                        handler.failed(responseString, throwable);
                    }

                    @Override
                    public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                        handler.success(responseString);
                    }
                }
        );
    }
}
