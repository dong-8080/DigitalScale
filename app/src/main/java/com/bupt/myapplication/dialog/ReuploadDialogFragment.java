package com.bupt.myapplication.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bupt.myapplication.MainActivity;
import com.bupt.myapplication.R;
import com.bupt.myapplication.ReuploadCallback;
import com.bupt.myapplication.recyclerList.ReuploadAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ReuploadDialogFragment extends DialogFragment {
    private View rootView;
    private Button uploadAllButton;
    private RecyclerView recyclerView;
    private TextView emptyState;
    private ReuploadAdapter reuploadAdapter;
    private AlertDialog uploadingDialog;

    private MainActivity activity;
    private static WeakReference<ReuploadDialogFragment> sCurrent = new WeakReference<>(null);
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // 创建一个对话框并返回
        Dialog dialog =  new Dialog(requireContext(), getTheme());
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog == null) return;
        Window window = dialog.getWindow();
        if (window == null) return;

        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = window.getWindowManager();
        if (wm != null) {
            wm.getDefaultDisplay().getMetrics(dm);
        } else {
            dm = getResources().getDisplayMetrics();
        }

        int maxWidthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 600, getResources().getDisplayMetrics());
        int targetWidthPx = (int) (dm.widthPixels * 0.92f);
        int finalWidthPx = Math.min(targetWidthPx, maxWidthPx);
        window.setLayout(finalWidthPx, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e("ReuploadDialogFragment", "onCreate");
        // 加载布局文件
        rootView = inflater.inflate(R.layout.reupload, container, false);
        sCurrent = new WeakReference<>(this);

        MaterialToolbar toolbar = rootView.findViewById(R.id.toolbar_reupload);
        toolbar.setNavigationOnClickListener(v -> dismiss());

        uploadAllButton = rootView.findViewById(R.id.upload_all);
        emptyState = rootView.findViewById(R.id.tv_empty_state);

        uploadAllButton.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("全部上传")
                    .setMessage("确定上传全部待上传数据吗？\n\n上传过程中请勿重复点击。")
                    .setPositiveButton("开始上传", (d, w) -> {
                        showUploadingDialog("正在上传全部数据，请稍候…");
                        setUiEnabled(false);
                        submitAll();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });

        recyclerView = rootView.findViewById(R.id.recycler_view_reupload);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        reuploadAdapter = new ReuploadAdapter(getJsonFiles(), this::confirmUploadSingle);
        recyclerView.setAdapter(reuploadAdapter);
        updateEmptyState();

        return rootView;
    }

    private void submitAll() {
        activity.Reupload(new ReuploadCallback() {
            @Override
            public void onUploadComplete() {
                hideUploadingDialog();
                setUiEnabled(true);
            }

            @Override
            public void onUploadFailed() {
                hideUploadingDialog();
                setUiEnabled(true);
            }
        });
    }

    public static void Refresh() {
        ReuploadDialogFragment fragment = sCurrent.get();
        if (fragment == null || fragment.reuploadAdapter == null) return;
        fragment.reuploadAdapter.setData(fragment.getJsonFiles());
        fragment.updateEmptyState();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        hideUploadingDialog();
        ReuploadDialogFragment cur = sCurrent.get();
        if (cur == this) {
            sCurrent = new WeakReference<>(null);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }

    //得到本地存储的所有json文件
    private List<File> getJsonFiles() {
        if (activity == null) return new ArrayList<>();
        File externalFilesDir = activity.getExternalFilesDir(null);
        if (externalFilesDir == null) return new ArrayList<>();
        File[] files = externalFilesDir.listFiles();
        List<File> jsonFiles = new ArrayList<>();
        if (files != null) {
            for (File file: files) {
                if (file.getName().endsWith(".json")) {
                    jsonFiles.add(file);
                }
            }
        }
        return jsonFiles;
    }

    private void confirmUploadSingle(File file) {
        if (file == null) return;
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("上传确认")
                .setMessage("是否上传该条数据？\n\n" + file.getName())
                .setPositiveButton("上传", (d, w) -> {
                    showUploadingDialog("正在上传该条数据，请稍候…");
                    setUiEnabled(false);
                    activity.ReuploadSingle(file.getName(), new ReuploadCallback() {
                        @Override
                        public void onUploadComplete() {
                            hideUploadingDialog();
                            setUiEnabled(true);
                        }

                        @Override
                        public void onUploadFailed() {
                            hideUploadingDialog();
                            setUiEnabled(true);
                        }
                    });
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void setUiEnabled(boolean enabled) {
        if (uploadAllButton != null) uploadAllButton.setEnabled(enabled);
        if (recyclerView != null) recyclerView.setEnabled(enabled);
    }

    private void updateEmptyState() {
        if (emptyState == null || recyclerView == null || reuploadAdapter == null) return;
        boolean isEmpty = reuploadAdapter.getItemCount() == 0;
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void showUploadingDialog(String message) {
        if (uploadingDialog != null && uploadingDialog.isShowing()) return;
        View v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_uploading, null, false);
        TextView tv = v.findViewById(R.id.tv_uploading);
        if (tv != null) tv.setText(message);

        uploadingDialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(v)
                .setCancelable(false)
                .create();
        uploadingDialog.show();
    }

    private void hideUploadingDialog() {
        if (uploadingDialog != null) {
            try {
                uploadingDialog.dismiss();
            } catch (Exception ignored) {}
            uploadingDialog = null;
        }
    }

}
