package com.bupt.myapplication.util;

import static com.bupt.myapplication.util.JsonUtil.deleteLocalFile;
import static com.bupt.myapplication.util.JsonUtil.getDataFromLocal;
import static com.bupt.myapplication.util.JsonUtil.saveDataToLocal;

import android.app.Activity;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.bupt.myapplication.MyApp;
import com.bupt.myapplication.ReuploadCallback;
import com.bupt.myapplication.data.PointManager;
import com.bupt.myapplication.data.StrokeManager;
import com.bupt.myapplication.data.StrokePoint;
import com.bupt.myapplication.dialog.ReuploadDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.bupt.myapplication.object.UploadStrokeObject;
import com.bupt.myapplication.view.DrawingView;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Response;

public class StrokeUploadManager {
    private static final String UPLOAD_URL = "https://ibrain.bupt.edu.cn/scaleBackend/scalesSetRecords/androidUpload";
    
    private final Activity activity;
    private final DrawingView drawingView;
    private final Runnable onUploadSuccessCallback;

    public StrokeUploadManager(Activity activity, DrawingView drawingView, Runnable onUploadSuccessCallback) {
        this.activity = activity;
        this.drawingView = drawingView;
        this.onUploadSuccessCallback = onUploadSuccessCallback;
    }

    /**
     * 提交笔迹数据，上传前检查网络状态
     */
    public void submitStrokes() {
        submitStrokes(null);
    }

    /**
     * 提交笔迹数据（带完成回调）
     * onFinished：当网络请求结束并准备弹出结果提示时回调，用于关闭UI层loading
     */
    public void submitStrokes(Runnable onFinished) {
        // 先检查网络状态
        if (!NetworkHelper.isNetworkConnected(activity)) {
            // 网络未连接，直接保存到本地并提示用户
            if (onFinished != null) onFinished.run();
            handleNetworkUnavailable();
            return;
        }

        // 网络正常，准备数据并提示用户确认上传
        List<List<StrokePoint>> strokesList = StrokeManager.getInstance().getALL();
        if (strokesList == null || strokesList.isEmpty()) {
            if (onFinished != null) onFinished.run();
            Toast.makeText(activity, "没有数据需要上传", Toast.LENGTH_SHORT).show();
            return;
        }
        UploadStrokeObject uploadStroke = createUploadObject(strokesList);
        
        Gson gson = new Gson();
        List<UploadStrokeObject> uploadList = new ArrayList<>();
        uploadList.add(uploadStroke);
        String jsonStr = gson.toJson(uploadList);

        // 执行实际上传
        performUpload(jsonStr, uploadStroke, onFinished);
    }

    /**
     * 执行实际上传操作
     */
    private void performUpload(String jsonStr, UploadStrokeObject uploadStroke, Runnable onFinished) {
        OkHttpUtils.getInstance().postAsync(UPLOAD_URL, jsonStr, new OkHttpUtils.Callback() {
            @Override
            public void onResponse(Response response) {
                activity.runOnUiThread(() -> {
                    if (onFinished != null) onFinished.run();
                    if (response.code() == 200) {
                        showUploadSuccessDialog();
                    } else {
                        // 上传失败，保存到本地
                        handleUploadFailed(jsonStr, uploadStroke.getUploadTime());
                    }
                });
            }

            @Override
            public void onFailure(IOException e) {
                activity.runOnUiThread(() -> {
                    if (onFinished != null) onFinished.run();
                    // 网络异常，保存到本地
                    handleUploadFailed(jsonStr, uploadStroke.getUploadTime());
                });
            }
        });
    }

    /**
     * 处理网络不可用的情况
     */
    private void handleNetworkUnavailable() {
        List<List<StrokePoint>> strokesList = StrokeManager.getInstance().getALL();
        if (strokesList == null || strokesList.isEmpty()) {
            Toast.makeText(activity, "没有数据需要上传", Toast.LENGTH_SHORT).show();
            return;
        }

        UploadStrokeObject uploadStroke = createUploadObject(strokesList);
        Gson gson = new Gson();
        List<UploadStrokeObject> uploadList = new ArrayList<>();
        uploadList.add(uploadStroke);
        String jsonStr = gson.toJson(uploadList);

        // 直接保存到本地并提示用户
        new AlertDialog.Builder(activity)
                .setTitle("网络不可用")
                .setMessage("当前网络连接不可用，数据已自动保存到本地。\n\n您可以在网络恢复后，通过\"待上传\"功能重新上传数据。")
                .setPositiveButton("知道了", (dialogInterface, i) -> {
                    String fileName = MyApp.getInstance().getParticipantID() + "_" + uploadStroke.getUploadTime();
                    saveDataToLocal(jsonStr, fileName, activity);
                    resetData();
                    if (onUploadSuccessCallback != null) {
                        onUploadSuccessCallback.run();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private UploadStrokeObject createUploadObject(List<List<StrokePoint>> strokesList) {
        UploadStrokeObject uploadStroke = new UploadStrokeObject();
        uploadStroke.setScalesSetRecordId(MyApp.getInstance().getParticipantID());
        uploadStroke.setPenMac(MyApp.getInstance().getCurMacAddress());
        LocalDateTime now = LocalDateTime.now();
        String timeString = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        uploadStroke.setUploadTime(timeString);
        uploadStroke.setStrokesList(strokesList);
        return uploadStroke;
    }

    private void showUploadSuccessDialog() {
        new AlertDialog.Builder(activity)
                .setTitle("提交成功")
                .setMessage("数据已成功提交！现在开始新的评测。")
                .setPositiveButton("好的", (dialogInterface, i) -> {
                    resetData();
                    if (onUploadSuccessCallback != null) {
                        onUploadSuccessCallback.run();
                    }
                })
                .setCancelable(false)
                .show();
    }

    /**
     * 处理上传失败的情况（网络问题或服务器错误）
     */
    private void handleUploadFailed(String jsonStr, String timeString) {
        // 检查是否是因为网络问题导致的失败
        boolean isNetworkIssue = !NetworkHelper.isNetworkConnected(activity);
        String message = isNetworkIssue 
                ? "网络连接异常，数据已自动保存到本地。您可以在网络恢复后，通过\"待上传\"功能重新上传。"
                : "数据上传失败，已自动缓存在本地。您稍后可以在\"待上传\"中重试。";

        new AlertDialog.Builder(activity)
                .setTitle("上传失败")
                .setMessage(message)
                .setPositiveButton("知道了", (dialogInterface, i) -> {
                    String fileName = MyApp.getInstance().getParticipantID() + "_" + timeString;
                    saveDataToLocal(jsonStr, fileName, activity);
                    resetData();
                    if (onUploadSuccessCallback != null) {
                        onUploadSuccessCallback.run();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void resetData() {
        StrokeManager.getInstance().clearAll();
        MyApp.getInstance().setPaperid(null);
        MyApp.getInstance().setParticipantID(null);
        PointManager.getInstance().clear();
        drawingView.initDraw();
    }

    /**
     * 重新上传本地暂存的数据
     */
    public void reupload(ReuploadCallback callback) {
        // 先检查网络状态
        if (!NetworkHelper.isNetworkConnected(activity)) {
            new AlertDialog.Builder(activity)
                    .setTitle("网络不可用")
                    .setMessage("当前网络连接不可用，无法上传数据。\n\n请检查网络连接后重试。")
                    .setPositiveButton("知道了", null)
                    .setCancelable(false)
                    .show();
            callback.onUploadFailed();
            return;
        }

        File externalFilesDir = activity.getExternalFilesDir(null);
        if (externalFilesDir == null) {
            Toast.makeText(activity, "存储目录不可用", Toast.LENGTH_SHORT).show();
            callback.onUploadFailed();
            return;
        }

        File[] files = externalFilesDir.listFiles();
        List<String> filenames = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".json")) {
                    filenames.add(file.getName());
                }
            }
        }

        if (filenames.isEmpty()) {
            Toast.makeText(activity, "当前没有数据可以上传", Toast.LENGTH_SHORT).show();
            return;
        }

        // 网络正常，直接开始批量上传（确认交互由UI层处理）
        uploadNextFile(filenames, 0, callback, true);
    }

    /**
     * 重新上传指定的本地暂存数据（单条）
     * 逻辑复用批量上传的 uploadNextFile
     */
    public void reuploadSingle(String filename, ReuploadCallback callback) {
        if (callback == null) return;

        if (filename == null || filename.trim().isEmpty()) {
            Toast.makeText(activity, "文件名无效", Toast.LENGTH_SHORT).show();
            callback.onUploadFailed();
            return;
        }

        // 先检查网络状态
        if (!NetworkHelper.isNetworkConnected(activity)) {
            new MaterialAlertDialogBuilder(activity)
                    .setTitle("网络不可用")
                    .setMessage("当前网络连接不可用，无法上传数据。\n\n请检查网络连接后重试。")
                    .setPositiveButton("知道了", null)
                    .setCancelable(false)
                    .show();
            callback.onUploadFailed();
            return;
        }

        File externalFilesDir = activity.getExternalFilesDir(null);
        if (externalFilesDir == null) {
            Toast.makeText(activity, "存储目录不可用", Toast.LENGTH_SHORT).show();
            callback.onUploadFailed();
            return;
        }

        File target = new File(externalFilesDir, filename);
        if (!target.exists() || !target.getName().endsWith(".json")) {
            Toast.makeText(activity, "本地文件不存在或格式不正确：" + filename, Toast.LENGTH_SHORT).show();
            callback.onUploadFailed();
            ReuploadDialogFragment.Refresh();
            return;
        }

        // 直接开始单条上传（确认交互由UI层处理）
        uploadNextFile(java.util.Collections.singletonList(filename), 0, callback, false);
    }

    private void uploadNextFile(List<String> filenames, int index, ReuploadCallback callback, boolean isBatch) {
        if (index >= filenames.size()) {
            String title = isBatch ? "批量上传结束" : "上传完成";
            String message = isBatch ? "所有本地暂存数据已处理完成。" : "该条本地暂存数据已处理完成。";
            new AlertDialog.Builder(activity)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("好的", (dialog, which) -> callback.onUploadComplete())
                    .setCancelable(false)
                    .show();
            ReuploadDialogFragment.Refresh();
            return;
        }

        String filename = filenames.get(index);
        String jsonStr = getDataFromLocal(filename, activity);

        String bodyToSend;
        try {
            JSONArray arr = new JSONArray(jsonStr);
            if (arr.length() == 0) {
                deleteLocalFile(filename, activity);
                Toast.makeText(activity, filename + " 为空，已跳过并删除", Toast.LENGTH_SHORT).show();
                ReuploadDialogFragment.Refresh();
                uploadNextFile(filenames, index + 1, callback, isBatch);
                return;
            }

            JSONObject first = arr.getJSONObject(0);
            JSONArray uploadArr = new JSONArray();
            uploadArr.put(first);
            bodyToSend = uploadArr.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            new AlertDialog.Builder(activity)
                    .setTitle("本地数据格式错误")
                    .setMessage("文件 " + filename + " 内容解析失败。")
                    .setPositiveButton(isBatch ? "跳过并继续下一条" : "关闭", (d, w) -> {
                        if (isBatch) {
                            uploadNextFile(filenames, index + 1, callback, true);
                        } else {
                            callback.onUploadFailed();
                        }
                    })
                    .setCancelable(false)
                    .show();
            return;
        }

        OkHttpUtils.getInstance().postAsync(UPLOAD_URL, bodyToSend, new OkHttpUtils.Callback() {
            @Override
            public void onResponse(Response response) {
                activity.runOnUiThread(() -> {
                    if (response.code() == 200) {
                        deleteLocalFile(filename, activity);
                        ReuploadDialogFragment.Refresh();
                        String toastMsg = isBatch
                                ? (filename + " 上传成功并已删除本地数据")
                                : ("上传成功，已删除本地数据\n" + filename);
                        Toast.makeText(activity, toastMsg, Toast.LENGTH_SHORT).show();
                        uploadNextFile(filenames, index + 1, callback, isBatch);
                    } else {
                        ErrorInfoFetcher.fetch(response, new ErrorInfoFetcher.Callback() {
                            @Override
                            public void onResult(String fullErrorText) {
                                activity.runOnUiThread(() -> {
                                    ReuploadDialogFragment.Refresh();
                                    if (isDuplicateSubmitError(fullErrorText)) {
                                        showDuplicateSubmitDialog(filename, filenames, index, callback, fullErrorText, isBatch);
                                    } else {
                                        showUploadErrorDialog(fullErrorText, filenames, index, callback, isBatch);
                                    }
                                });
                            }

                            @Override
                            public void onFailed(String reason) {
                                activity.runOnUiThread(() -> {
                                    showUploadErrorDialog("获取错误详情失败：" + reason, filenames, index, callback, isBatch);
                                });
                            }
                        });
                    }
                });
            }

            @Override
            public void onFailure(IOException e) {
                e.printStackTrace();
                activity.runOnUiThread(() -> {
                    showUploadErrorDialog("网络/服务器异常：" + e.getMessage(), filenames, index, callback, isBatch);
                });
            }
        });
    }

    private void showDuplicateSubmitDialog(
            String filename,
            List<String> filenames,
            int index,
            ReuploadCallback callback,
            String errorText,
            boolean isBatch
    ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle("检测到重复提交")
                .setMessage(errorText + "\n\n是否删除当前这条本地数据？")
                .setCancelable(false);

        if (isBatch) {
            builder.setPositiveButton("删除并继续下一条", (d, w) -> {
                        deleteLocalFile(filename, activity);
                        ReuploadDialogFragment.Refresh();
                        Toast.makeText(activity, filename + " 为重复提交，已删除本地数据", Toast.LENGTH_SHORT).show();
                        uploadNextFile(filenames, index + 1, callback, true);
                    })
                    .setNegativeButton("保留并继续下一条", (d, w) -> uploadNextFile(filenames, index + 1, callback, true));
        } else {
            builder.setPositiveButton("删除本地数据", (d, w) -> {
                        deleteLocalFile(filename, activity);
                        ReuploadDialogFragment.Refresh();
                        Toast.makeText(activity, "已删除本地数据：" + filename, Toast.LENGTH_SHORT).show();
                        callback.onUploadComplete();
                    })
                    .setNegativeButton("保留本地数据", (d, w) -> callback.onUploadFailed());
        }

        builder.show();
    }

    private void showUploadErrorDialog(
            String errorMessage,
            List<String> filenames,
            int index,
            ReuploadCallback callback,
            boolean isBatch
    ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle("上传失败")
                .setMessage(errorMessage)
                .setCancelable(false);

        if (isBatch) {
            builder.setPositiveButton("继续上传下一条", (d, w) -> uploadNextFile(filenames, index + 1, callback, true))
                   .show();
        } else {
            builder.setPositiveButton("重试", (d, w) -> uploadNextFile(filenames, index, callback, false))
                   .setNegativeButton("关闭", (d, w) -> callback.onUploadFailed())
                   .show();
        }
    }

    private boolean isDuplicateSubmitError(String msg) {
        if (msg == null) return false;
        return msg.contains("此份文件设置的上传时间已存在于系统")
                || msg.contains("重复提交了相同的笔迹文件")
                || msg.contains("重复提交");
    }
}

