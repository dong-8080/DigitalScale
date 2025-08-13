package com.bupt.myapplication.util;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpUtils {

    private static OkHttpUtils instance = null;
    private OkHttpClient client;
    // 私有构造函数
    private OkHttpUtils() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)   // 连接超时：30s
                .readTimeout(120, TimeUnit.SECONDS)     // 读取响应超时：120s
                .writeTimeout(120, TimeUnit.SECONDS)    // 写入请求体超时：120s
                .build();
    }

    // 公开获取实例的方法
    public static synchronized OkHttpUtils getInstance() {
        if (instance == null) {
            instance = new OkHttpUtils();
        }
        return instance;
    }

    // 异步GET请求
    public void getAsync(String url, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
//                if (!response.isSuccessful()) {
//                    callback.onFailure(new IOException("Unexpected code " + response));
//                    return;
//                }

                callback.onResponse(response);
            }
        });
    }

    // 异步POST请求
    public void postAsync(String url, String json, Callback callback) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json, JSON);


        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
//                if (!response.isSuccessful()) {
//                    callback.onFailure(new IOException("Unexpected code " + response));
//                    return;
//                }

                callback.onResponse(response);
            }
        });
    }

    // 回调接口
    public interface Callback {
        void onResponse(Response response);
        void onFailure(IOException e);
    }
}