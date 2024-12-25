package com.bupt.myapplication.dialog;

import static java.lang.Thread.sleep;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bbb.bpen.command.BiBiCommand;
import com.bupt.myapplication.GlobalVars;
import com.bupt.myapplication.MyApp;
import com.bupt.myapplication.fragment.IFragmentCallBack;
import com.bupt.myapplication.R;
import com.bupt.myapplication.recyclerList.BLEScanAdapter;
import com.bupt.myapplication.recyclerList.BLEScanManager;
import com.bupt.myapplication.recyclerList.BLEScanObserver;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

// 首页的dialog，展示一个宣传图和被试编号、蓝牙笔连接
public class MainDialogFragment extends DialogFragment implements BLEScanObserver {

    private LinearLayout linearLayout1, linearLayout2, linearLayout3;
    private Button button1, button2, button3, button_scan;

    private EditText editTextS1, editTextS2;
    private EditText editTextX1, editTextX2, editTextX3, editTextX4;
    private EditText editTextYear1, editTextYear2;
    private EditText editTextMonth1, editTextMonth2;

    private String participantId;
    private boolean isConnected = false;

    // BLE扫描的mac地址
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
        // 设置对话框外部点击无法取消
//        if(GlobalVars.getInstance().getOpened() == false){
//            dialog.setCanceledOnTouchOutside(false);
//        }
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

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

        // 设置按钮点击事件
        // 跳转到被试编号填写界面
        // TODO: For Jingrixing demo
        button1.setOnClickListener(v -> showFragment2());

        // 被试编号填写，逻辑复杂些，检查ID正确性后才能进行下一步
        // TODO：完成测试后，需要再次进入这个页面，如何实现跳转逻辑
        // TODO：如果ID和本地的重复，需要重复读取一下
        // 开始测试删除操作
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                participantId = "HEAD_S" + editTextS1.getText() + editTextS2.getText() + "_00" +
                        editTextX1.getText() + editTextX2.getText() + editTextX3.getText() + editTextX4.getText() +
                        "_20" + editTextYear1.getText() + editTextYear2.getText() + editTextMonth1.getText() +
                        editTextMonth2.getText();
                Log.e("HEAD", "onClick: " + participantId.length() + participantId);

                if (participantId.length() == 22) {
                    MyApp.getInstance().setParticipantID(participantId);
                    // TODO：可以将toolbar设置成被试的ID
                    showFragment3();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("警告")
                            .setMessage("检测到被试编号未填写，这会导致无法上传数据，确定执行下一步吗?")
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // 用户点击确认按钮的操作
                                    showFragment3();
                                }
                            })
                            .setNegativeButton("取消", null)
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
        data.add("未找到蓝牙设备，请打开蓝牙笔");

        bleScanAdapter = new BLEScanAdapter(data);
        recyclerView.setAdapter(bleScanAdapter);

        editTextS1 = view.findViewById(R.id.editTextS1);
        editTextS2 = view.findViewById(R.id.editTextS2);
        editTextX1 = view.findViewById(R.id.editTextX1);
        editTextX2 = view.findViewById(R.id.editTextX2);
        editTextX3 = view.findViewById(R.id.editTextX3);
        editTextX4 = view.findViewById(R.id.editTextX4);
        editTextYear1 = view.findViewById(R.id.editTextYear1);
        editTextYear2 = view.findViewById(R.id.editTextYear2);
        editTextMonth1 = view.findViewById(R.id.editTextMonth1);
        editTextMonth2 = view.findViewById(R.id.editTextMonth2);

        // 设置默认值
//        Calendar calendar = Calendar.getInstance();
//        int year = calendar.get(Calendar.YEAR);
//        int month = calendar.get(Calendar.MONTH) + 1; // 月份是从0开始的，所以需要加1
//        editTextYear1.setText(String.valueOf((year - 2000) / 10));
//        editTextYear2.setText(String.valueOf(year - 2020));
//        editTextMonth1.setText(String.valueOf(month / 10));
//        editTextMonth2.setText(String.valueOf(month % 10));

        setupEditText(editTextS1, editTextS2, null);
        setupEditText(editTextS2, editTextX1, editTextS1);
        setupEditText(editTextX1, editTextX2, editTextS2);
        setupEditText(editTextX2, editTextX3, editTextX1);
        setupEditText(editTextX3, editTextX4, editTextX2);
        setupEditText(editTextX4, editTextYear1, editTextX3);
        setupEditText(editTextYear1, editTextYear2, editTextX4);
        setupEditText(editTextYear2, editTextMonth1, editTextYear1);
        setupEditText(editTextMonth1, editTextMonth2, editTextYear2);
        setupEditText(editTextMonth2, null, editTextMonth1);


        bleScanAdapter.notifyDataSetChanged();  // 用于刷新recyclerView,也就是每次修改bleScanAdapter中的list，就要调用这个函数刷新一下

        if (GlobalVars.getInstance().getOpened() == true) {
            showFragment3();
        } else {
            showFragment1();
        }
        scanBLEPen();

        checkNetwork();
        return view;
    }

    private void setupEditText(final EditText current, final EditText next, final EditText prev) {
        current.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1 && next != null) {
                    next.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        current.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if (current.getText().length() == 1 && prev != null) {
                        prev.requestFocus();
                        current.setText("");
                        return true;
                    } else if (current.getText().length() == 0 && prev == null) {
                        return true; // For the first EditText, do nothing on backspace
                    }
                }
                return false;
            }
        });

        // Handle the case where the next EditText is not empty
        final EditText finalNext = next;
        current.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && finalNext != null && finalNext.getText().toString().trim().length() > 0) {
                    // If the next EditText is not empty, we clear it to prepare for the new input
                    finalNext.setText("");
                }
            }
        });
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

        if (!isConnected) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("蓝牙笔连接提示")
                    .setMessage("当前没有连接蓝牙笔，确认进行下一步吗?")
                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // 用户点击确认按钮的操作
                            dismiss();
                            GlobalVars.getInstance().setOpened(true);
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
            //Toast.makeText(requireContext(), "蓝牙未连接，不能进行下一步", Toast.LENGTH_SHORT).show();
        } else if (!isNetworkConnected(getContext())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("提示")
                    .setMessage("当前没有连接网络，确认进行下一步吗?")
                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // 用户点击确认按钮的操作
                            dismiss();
                            GlobalVars.getInstance().setOpened(true);
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        } else {
            dismiss();
            GlobalVars.getInstance().setOpened(true);
        }
        GlobalVars.getInstance().setOpened(true);
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
        isConnected = true;
    }

}