package com.bupt.myapplication.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkHelper {
    
    /**
     * 检查网络是否连接
     * @param context 上下文
     * @return true表示网络已连接，false表示网络未连接
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = 
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }
    
    /**
     * 检查网络是否可用（不仅连接，还要能访问互联网）
     * @param context 上下文
     * @return true表示网络可用
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = 
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null 
                    && activeNetwork.isConnected() 
                    && activeNetwork.isAvailable();
        }
        return false;
    }
}

