package com.bupt.myapplication.dialog;

import static java.lang.Thread.sleep;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bbb.bpen.command.BiBiCommand;
import com.bupt.myapplication.MyApp;
import com.bupt.myapplication.fragment.IFragmentCallBack;
import com.bupt.myapplication.R;
import com.bupt.myapplication.recyclerList.BLEScanAdapter;
import com.bupt.myapplication.recyclerList.BLEScanManager;
import com.bupt.myapplication.recyclerList.BLEScanObserver;
import com.bupt.myapplication.view.EditableSequenceView;

import java.util.ArrayList;
import java.util.List;

// 首页的dialog，展示一个宣传图和被试编号、蓝牙笔连接
public class MainDialogFragment extends DialogFragment implements BLEScanObserver {

    private LinearLayout linearLayout1, linearLayout2, linearLayout3;
    private Button button1, button2, button3, button_scan;
    private TextView tvDialogTitle;

    private EditableSequenceView editableSequenceView;
    private String participantId;

    private RecyclerView recyclerView;
    private BLEScanAdapter bleScanAdapter;
    public BLEScanManager bleScanManager;
    private IFragmentCallBack iFragmentCallBack;

    public void setiFragmentCallBack(IFragmentCallBack callback) {
        iFragmentCallBack = callback;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // 创建一个对话框并返回
        Dialog dialog = new Dialog(requireContext(), getTheme());

        dialog.setCanceledOnTouchOutside(false);
        // 设置对话框窗口背景透明，以便显示圆角卡片
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

        // 固定窗口宽度：屏幕宽度的92%，同时不超过600dp
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

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e("MyDialogFragment", "onCreate");
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
        tvDialogTitle = view.findViewById(R.id.tv_dialog_title);

        editableSequenceView = view.findViewById(R.id.editableSequenceView);
        // 设置按钮点击事件
        // 跳转到被试编号填写界面
        button1.setOnClickListener(v -> showFragment2());

        // 被试编号填写，逻辑复杂些，检查ID正确性后才能进行下一步
        // TODO：完成测试后，需要再次进入这个页面，如何实现跳转逻辑
        // TODO：如果ID和本地的重复，需要重复读取一下
        // 开始测试删除操作
        button2.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onClick(View view) {
                participantId = editableSequenceView.getText();
                Log.e("HEAD", "onClick: " + participantId.length() + participantId);

                if (participantId.length() == 22) {
                    MyApp.getInstance().setParticipantID(participantId);

                    // 已经连接好蓝牙笔，跳过连接蓝牙笔步骤
                    if (MyApp.getInstance().isBLEConnected()){
                        finalConfrim();
                    }else {
                        // 没有连接好蓝牙笔，进入连接蓝牙笔步骤
                        showFragment3();
                    }
                } else {
                    // #region agent log
                    String runId = "pre-fix";
                    try {
                        // 预检：定位是否存在缺 layout_width/height 的 tag（常见于 mtrl/m3_alert_dialog_title）
                        com.bupt.myapplication.util.AgentDebugLog.preflightLayout(requireContext(),
                                com.google.android.material.R.layout.mtrl_alert_dialog_title, runId, "H1");
                        com.bupt.myapplication.util.AgentDebugLog.preflightLayout(requireContext(),
                                com.google.android.material.R.layout.m3_alert_dialog_title, runId, "H2");
                        com.bupt.myapplication.util.AgentDebugLog.preflightLayout(requireContext(),
                                com.bupt.myapplication.R.layout.mtrl_alert_dialog, runId, "H3");
                        com.bupt.myapplication.util.AgentDebugLog.preflightLayout(requireContext(),
                                com.bupt.myapplication.R.layout.m3_alert_dialog, runId, "H3");
                    } catch (Exception ignored) {}
                    // #endregion agent log

                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                            .setTitle("编号长度说明")
                            .setMessage("被试编号通常为22位。检测到当前输入长度不符，是否继续？\n\n注意：不规范的编号可能导致后期数据分析困难。")
                            .setPositiveButton("坚持继续", (dialogInterface, i) -> showFragment3())
                            .setNegativeButton("返回修改", null)
                            .show();
                }
                Log.e("ID", participantId);
            }
        });
        button3.setOnClickListener(v -> finalConfrim());

        button_scan.setOnClickListener(v -> secondScanBLEPen());


        bleScanManager = BLEScanManager.getInstance();
        bleScanManager.addObserver(this);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        List<String> data = new ArrayList<>();
        if(MyApp.getInstance().isBLEConnected()){
            String curMacAdress = MyApp.getInstance().getCurMacAddress();
            data.add(curMacAdress);

        }else{
            data.add("未找到蓝牙设备，请打开蓝牙笔");
        }

        bleScanAdapter = new BLEScanAdapter(data);
        recyclerView.setAdapter(bleScanAdapter);

        bleScanAdapter.notifyDataSetChanged();  // 用于刷新recyclerView,也就是每次修改bleScanAdapter中的list，就要调用这个函数刷新一下


        showFragment1();
        scanBLEPen();

        checkNetwork();
        return view;
    }


    private void dismissAll(){
        linearLayout1.setVisibility(View.GONE);
        linearLayout2.setVisibility(View.GONE);
        linearLayout3.setVisibility(View.GONE);
    }

    private void showFragment(LinearLayout linearLayout, String title, Button stepButton) {
        linearLayout1.setVisibility(View.GONE);
        linearLayout2.setVisibility(View.GONE);
        linearLayout3.setVisibility(View.GONE);
        button1.setVisibility(View.GONE);
        button2.setVisibility(View.GONE);
        button3.setVisibility(View.GONE);

        linearLayout.setVisibility(View.VISIBLE);
        if (tvDialogTitle != null) {
            tvDialogTitle.setText(title);
        }
        if (stepButton != null) {
            stepButton.setVisibility(View.VISIBLE);
        }
    }

    private void showFragment1() {
        showFragment(linearLayout1, "数字化量表数据采集系统", button1);
    }

    private void showFragment2() {
        showFragment(linearLayout2, "被试编号填写", button2);
    }

    private void showFragment3() {
        showFragment(linearLayout3, "蓝牙笔连接配置", button3);
    }

    @Override
    public void onBLEScanChanged() {
        Log.e("TAG", "on received data changed");
        List<String> macAddress = bleScanManager.getBLEScanList();
        bleScanAdapter.updateData(macAddress);
    }

    public void secondScanBLEPen() {

        bleScanAdapter.clearData();
        //bleScanAdapter.addData("未找到蓝牙设备，请打开蓝牙笔");
        bleScanManager.resetData();
        bleScanAdapter.notifyDataSetChanged();

        scanBLEPen();
    }


    private void scanBLEPen() {
        // 延时才能生效
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                BiBiCommand.startScanWithQueue(getContext());
                Log.e("BIBIPENSCAN", "start scan");
            }
        }, 1 * 1000);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 可能界面都关闭了，但是这个线程仍在进行
                try {
                    BiBiCommand.stopscan(getContext());
                    Log.e("BIBIPENSCAN", "stop scan");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 10 * 1000); //10s
    }

    public void checkNetwork() {
        if (!isNetworkConnected(getContext())) {
            Toast.makeText(getContext(), "网络未连接，请连接网络后使用", Toast.LENGTH_LONG).show();
        }
    }

    public void finalConfrim() {
        // 提示蓝牙笔有无连接

        if (!MyApp.getInstance().isBLEConnected()) {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle("设备未连接")
                    .setMessage("当前尚未连接蓝牙笔，将无法正常使用系统，是否继续使用")
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setPositiveButton("确认离线使用", (dialogInterface, i) -> dismiss())
                    .setNegativeButton("去连接", null)
                    .show();
        } else if (!isNetworkConnected(getContext())) {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle("网络未连接")
                    .setMessage("当前网络不可用，数据将无法实时上传，确定继续吗？")
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setPositiveButton("确认继续", (dialogInterface, i) -> dismiss())
                    .setNegativeButton("去设置网络", null)
                    .show();
        } else {
            dismiss();
        }
        // 结束全部的扫描
        try {
            BiBiCommand.stopscan(getContext());
        } catch (Exception e) {
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

    public void changeColor(String ads) {
        bleScanAdapter.setBindId(ads);
        bleScanAdapter.notifyDataSetChanged();

    }

}