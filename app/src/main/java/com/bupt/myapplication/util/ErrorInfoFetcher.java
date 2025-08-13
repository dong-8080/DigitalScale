package com.bupt.myapplication.util;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ErrorInfoFetcher {

    private static final OkHttpClient client = new OkHttpClient();

    /** 后端错误返回的结构 */
    private static class ErrorResp {
        @SerializedName("message")
        String message;

        @SerializedName("data")
        String data;   // 可能是 txt 的 url，也可能以后直接是内容
    }

    /** 回调接口 */
    public interface Callback {
        void onResult(String fullErrorText);   // 成功时返回完整错误文本
        void onFailed(String reason);          // 网络/解析失败时的提示
    }

    /**
     * 任何非 200 的 response 都调用这个方法
     * 它会自动解析 { "message":..., "data":"https://.../xxx.txt" }
     * 再去把 txt 下载下来，拼成完整的错误信息返回
     */
    public static void fetch(Response response, Callback callback) {
        if (response == null || callback == null) {
            callback.onFailed("response 为 null");
            return;
        }

        // 1. 先把 body 读出来（只能读一次）
        String body;
        try {
            body = response.body() != null ? response.body().string() : "";
        } catch (Exception e) {
            callback.onFailed("读取响应体失败");
            response.close();
            return;
        } finally {
            response.close();   // 一定要关
        }

        // 2. 解析 JSON
        ErrorResp err;
        try {
            err = new com.google.gson.Gson().fromJson(body, ErrorResp.class);
        } catch (Exception e) {
            callback.onFailed("JSON 解析失败");
            return;
        }

        String baseMsg = (err.message != null && !err.message.trim().isEmpty())
                ? err.message.trim() : "服务器处理失败";

        // 3. 如果没有 data，直接返回 message
        if (err.data == null || err.data.trim().isEmpty()) {
            callback.onResult(baseMsg);
            return;
        }

        String data = err.data.trim();

        // 4. data 是 http(s) 链接 → 去下载 txt
        if (data.startsWith("http://") || data.startsWith("https://")) {
            Request req = new Request.Builder().url(data).build();
            client.newCall(req).enqueue(new okhttp3.Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    callback.onFailed("获取错误详情失败");
                }

                @Override public void onResponse(Call call, Response r) throws IOException {
                    String txt = r.body() != null ? r.body().string() : "";
                    r.close();

                    // 这里把你想要的几行信息拼成完整字符串返回
                    String finalText = baseMsg + "\n\n" + txt.trim();
                    callback.onResult(finalText);
                }
            });
        } else {
            // data 本身就是文字（兼容以后直接返回内容的情况）
            callback.onResult(baseMsg + "\n\n" + data);
        }
    }

}