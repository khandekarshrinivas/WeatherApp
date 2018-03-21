package com.shrinivas.weatherapp;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/** @author Shrinivas Khandekar
 * Created by Shrinivas Khandekar on 2018-03-06.
 */

public class RequestQueueSingleton {

    private static RequestQueueSingleton mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;

    public RequestQueueSingleton(Context context) {
        mCtx = context.getApplicationContext();
        mRequestQueue = getRequestQueue();
    }

    public static synchronized  RequestQueueSingleton getInstance(Context context){
        if(mInstance == null){
            mInstance = new RequestQueueSingleton(context.getApplicationContext());
        }
        return mInstance;
    }
    public RequestQueue getRequestQueue() {
        if(mRequestQueue == null){
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
