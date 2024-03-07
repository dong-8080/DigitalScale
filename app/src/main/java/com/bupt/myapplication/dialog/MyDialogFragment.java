package com.bupt.myapplication.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bbb.bpen.command.BiBiCommand;
import com.bupt.myapplication.MainActivity;
import com.bupt.myapplication.R;
import com.bupt.myapplication.recyclerList.BLEScanAdapter;
import com.bupt.myapplication.recyclerList.BLEScanManager;
import com.bupt.myapplication.recyclerList.BLEScanObserver;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class MyDialogFragment extends DialogFragment implements BLEScanObserver{

    private LinearLayout linearLayout1, linearLayout2, linearLayout3;
    private Button button1, button2, button3, button_scan;

    // BLE扫描的mac地址
    private RecyclerView recyclerView;
    private BLEScanAdapter bleScanAdapter;
    public BLEScanManager bleScanManager;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // 创建一个对话框并返回
        Dialog dialog =  new Dialog(requireContext(), getTheme());
        // 设置对话框外部点击无法取消
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 加载布局文件
        View view = inflater.inflate(R.layout.fragment_dialog, container, false);

        // 初始化控件
        linearLayout1 = view.findViewById(R.id.linear_layout1);
        linearLayout2 = view.findViewById(R.id.linear_layout2);
        linearLayout3 = view.findViewById(R.id.linear_layout3);
        button1 = view.findViewById(R.id.buttonStartTest);
        button2 = view.findViewById(R.id.button2);
        button3 = view.findViewById(R.id.button3);
        button_scan = view.findViewById(R.id.button_scan);

        // 设置按钮点击事件
        // 2024/1/28 取消中间的用户信息页面，直接跳转到蓝牙笔连接页面
        button1.setOnClickListener(v -> showFragment3());
//        button2.setOnClickListener(v -> showFragment3());
        button3.setOnClickListener(v -> finalConfrim());

        button_scan.setOnClickListener(v -> secondScanBLEPen());


        bleScanManager = BLEScanManager.getInstance();
        bleScanManager.addObserver(this);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        List<String> data = new ArrayList<>();
        data.add("未找到蓝牙设备，请打开蓝牙笔");

        bleScanAdapter = new BLEScanAdapter(data);
        recyclerView.setAdapter(bleScanAdapter);

        bleScanAdapter.notifyDataSetChanged();

        showFragment1();
        scanBLEPen();

        checkNetwork();
        return view;
    }

    private void showFragment(LinearLayout linearLayout) {
        linearLayout1.setVisibility(View.GONE);
        linearLayout2.setVisibility(View.GONE);
        linearLayout3.setVisibility(View.GONE);
        linearLayout.setVisibility(View.VISIBLE);
    }

    private void showFragment1() {
        showFragment(linearLayout1);
    }

    private void showFragment2() {
        showFragment(linearLayout2);
    }

    private void showFragment3() {
        showFragment(linearLayout3);
    }

    @Override
    public void onBLEScanChanged() {
        Log.e("TAG", "on received data changed");
        List<String> macAddress = bleScanManager.getBLEScanList();
        bleScanAdapter.updateData(macAddress);
    }

    public void secondScanBLEPen(){
        Toast.makeText(getContext(),"重复扫描暂未实现，请重启应用", Toast.LENGTH_SHORT).show();
    }


    private void scanBLEPen(){
        // 延时才能生效
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                BiBiCommand.startScanWithQueue(getContext());
            }
        }, 1*1000);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 可能界面都关闭了，但是这个线程仍在进行
                try {
                    BiBiCommand.stopscan(getContext());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }, 10*1000);
    }

    public void checkNetwork(){
        if (!isNetworkConnected(getContext())){
            Toast.makeText(getContext(), "网络未连接，请连接网络后使用", Toast.LENGTH_LONG).show();
        }
    }

    public void finalConfrim(){
        // 提示蓝牙笔有无连接

        if (!BiBiCommand.isConnect(getContext())) {
            new MaterialAlertDialogBuilder(getContext())
                    .setTitle("提示")
                    .setMessage("当前没有连接蓝牙笔，确认进行下一步吗?")
                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // 用户点击确认按钮的操作
                            dismiss();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        }else {
            dismiss();
        }

        // 结束全部的扫描
        try {
            BiBiCommand.stopscan(getContext());
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }

}