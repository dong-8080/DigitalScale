package com.bupt.myapplication.util;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// 封装OkHttp作为与后端交互的工具类
public class HttpUtil {
    // "https://bluepen.66nao.com/api/penUser/";
    // "http://10.110.147.81:9010/penUser/";
    public static final String API = "https://bluepen.66nao.com/api/penUser/";
    private static HttpUtil instance;
    private OkHttpClient client;

    private HttpUtil() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // 连接超时时间为10秒
                .readTimeout(30, TimeUnit.SECONDS)    // 读取超时时间为30秒
                .writeTimeout(30, TimeUnit.SECONDS)   // 写入超时时间为30秒
                .build();

    }

    public static synchronized HttpUtil getInstance() {
        if (instance == null) {
            instance = new HttpUtil();
        }
        return instance;
    }

    public void getRequest(String url, final OkHttpCallback callback) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                if (callback != null) {
                    callback.onResponse(responseData);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                if (callback != null) {
                    callback.onFailure(e);
                }
            }
        });
    }

    public void getRequest(String url, Map<String, String> queryParams, final OkHttpCallback callback) {
        // 构建带参数的 URL
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }
        String finalUrl = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(finalUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                if (callback != null) {
                    callback.onResponse(responseData);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                if (callback != null) {
                    callback.onFailure(e);
                }
            }
        });
    }

    public void postRequest(String url, RequestBody requestBody, final OkHttpCallback callback) {
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                if (callback != null) {
                    callback.onResponse(responseData);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                if (callback != null) {
                    callback.onFailure(e);
                }
            }
        });
    }

    public interface OkHttpCallback {
        String onResponse(String response);

        void onFailure(IOException e);
    }
}
