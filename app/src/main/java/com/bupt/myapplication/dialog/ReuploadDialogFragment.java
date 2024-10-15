package com.bupt.myapplication.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bupt.myapplication.MainActivity;
import com.bupt.myapplication.R;
import com.bupt.myapplication.ReuploadCallback;
import com.bupt.myapplication.recyclerList.BLEScanAdapter;
import com.bupt.myapplication.recyclerList.ReuploadAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReuploadDialogFragment extends DialogFragment {
    private LinearLayout linearLayout1;
    private Button button1;
    private static RecyclerView recyclerView;
    private static ReuploadAdapter reuploadAdapter;
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // 创建一个对话框并返回
        Dialog dialog =  new Dialog(requireContext(), getTheme());
        return dialog;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e("ReuploadDialogFragment", "onCreate");
        // 加载布局文件
        View view = inflater.inflate(R.layout.reupload, container, false);
        linearLayout1 = view.findViewById(R.id.linear_layout);
        button1=view.findViewById(R.id.upload_all);

        //refresh.setOnClickListener(v->Refresh());
        button1.setOnClickListener(v -> {
            button1.setEnabled(false);
            submit();
        });

        recyclerView = view.findViewById(R.id.recycler_view_reupload);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<File> data = getJsonFiles();
        reuploadAdapter = new ReuploadAdapter(data);
        recyclerView.setAdapter(reuploadAdapter);

        reuploadAdapter.notifyDataSetChanged();
        showFragment();

        return view;
    }

    private void submit() {
        activity.Reupload(new ReuploadCallback() {
            @Override
            public void onUploadComplete() {
                button1.setEnabled(true);
            }

            @Override
            public void onUploadFailed() {
                button1.setEnabled(true);
            }
        });
    }

    public static void Refresh() {
        List<File> data = getJsonFiles();
        reuploadAdapter = new ReuploadAdapter(data);
        recyclerView.setAdapter(reuploadAdapter);

        reuploadAdapter.notifyDataSetChanged();
    }

    private void showFragment() {
        linearLayout1.setVisibility(View.VISIBLE);
    }

    private static MainActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }

    //得到本地存储的所有json文件
    private static List<File> getJsonFiles() {
        File externalFilesDir = activity.getExternalFilesDir(null);
        File[] files = externalFilesDir.listFiles();
        List<File> jsonFiles = new ArrayList<>();
        for (File file: files) {
            if (file.getName().endsWith(".json")) {
                jsonFiles.add(file);
            }
        }
        return jsonFiles;
    }


}
