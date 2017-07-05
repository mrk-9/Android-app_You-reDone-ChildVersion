package com.youredone.youredonekids.common;

/**
 * Created by 1030 on 4/18/2016.
 */
public class Application {

    public static Application instance = null;

    public static String locktime;
    public static long constant;

    public static Application getSharedInstance()
    {
        if(instance == null)
        {
            instance = new Application();
        }
        return instance;
    }
    public Application()
    {

    }
}
