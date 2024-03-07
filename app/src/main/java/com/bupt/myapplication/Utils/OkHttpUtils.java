package com.bupt.myapplication.Utils;

import java.io.IOException;

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
        client = new OkHttpClient();
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
                if (!response.isSuccessful()) {
                    callback.onFailure(new IOException("Unexpected code " + response));
                    return;
                }

                callback.onResponse(response.body().string());
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
                if (!response.isSuccessful()) {
                    callback.onFailure(new IOException("Unexpected code " + response));
                    return;
                }

                callback.onResponse(response.body().string());
            }
        });
    }

    // 回调接口
    public interface Callback {
        void onResponse(String response);
        void onFailure(IOException e);
    }
}