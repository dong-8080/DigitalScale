package com.bupt.myapplication;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bbb.bpen.binder.BiBiBinder;
import com.bbb.bpen.command.BiBiCommand;
import com.bbb.bpen.delegate.BlueDelegate;
import com.bbb.bpen.service.BluetoothLEService;
import com.bupt.myapplication.Utils.OkHttpUtils;
import com.bupt.myapplication.data.PointManager;
import com.bupt.myapplication.data.StrokeManager;
import com.bupt.myapplication.data.StrokePoint;
import com.bupt.myapplication.dialog.MyDialogFragment;
import com.bupt.myapplication.object.PostStrokeObject;
import com.bupt.myapplication.util.HttpUtil;
import com.bupt.myapplication.util.StringUtils;
import com.bupt.myapplication.view.DrawingView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.List;
import java.util.LongSummaryStatistics;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class MainActivity extends AppCompatActivity {


    // import custom drawing view
    public DrawingView dw;

    private static String TAG = "MainActivityClass";

    public Handler BLEConnectHandler;

    public BlueDelegate blueDelegate;
    static {
        System.loadLibrary("bbbdraw");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dw = findViewById(R.id.drawing_view);

        init();

    }

    private void init() {
        // 权限授予
        checkBluetoothPermission();

        // 初始化蓝牙笔连接service
        BLEConnectHandler = new Handler(Looper.getMainLooper());
        Intent intent = new Intent(this, BluetoothLEService.class);
        bindService(intent, coon, Context.BIND_AUTO_CREATE);

        // 处理相关的蓝牙笔连接和笔迹处理
        blueDelegate = new BlueDelegateImpl(dw);

        showMyDialog();
    }

    public void showMyDialog() {
        MyDialogFragment dialogFragment = new MyDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), "dialog");
    }



    Handler stopScanHandler = new Handler();
    Runnable stopScanRunable = new Runnable() {
        @Override
        public void run() {
            BiBiCommand.stopscan(MainActivity.this);
            stopScanHandler.postDelayed(this, 10000); // 10秒后再次执行
        }
    };

    private BluetoothLEService service = null;
    private ServiceConnection coon = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            BiBiBinder myBinder = (BiBiBinder) binder;
            service = myBinder.getService();
            service.setblueDelegate(blueDelegate);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "onServiceDisconnected ");
        }
    };


    public static final int REQUEST_BLUETOOTH_PERMISSION = 310;
    private void checkBluetoothPermission() {
        // 检查蓝牙权限是否已授予
        // 写满权限
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED

        ) {
            // 权限未被授予，需要申请权限
//            Toast.makeText(this, "权限未被授予，需要申请权限", Toast.LENGTH_SHORT);
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.BLUETOOTH,
                    android.Manifest.permission.BLUETOOTH_ADMIN,
                    android.Manifest.permission.BLUETOOTH_CONNECT,
                    android.Manifest.permission.BLUETOOTH_SCAN,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, REQUEST_BLUETOOTH_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {

            // 0表示授权成功，-1失败，全都为0即成功
            int grantResultsSum = 0;
            for (int grantResult : grantResults) {
                grantResultsSum += grantResult;
            }
            if (grantResults.length > 0 && grantResultsSum == PackageManager.PERMISSION_GRANTED) {
                // 权限已被授予，可以进行蓝牙操作
                Log.e(TAG, "权限已被授予，可以进行蓝牙操作");
                for (int i = 0; i < grantResults.length; i++) {
                    Log.e(TAG, "permission " + permissions[i] + ":" + grantResults[i]);
                }
//                showPenBindingDialog();

            } else {
                // 权限被拒绝，无法执行蓝牙操作
                Log.e(TAG, "权限被拒绝，无法执行蓝牙操作");
                Toast.makeText(MainActivity.this, "权限被拒绝，无法执行蓝牙操作",
                        Toast.LENGTH_LONG).show();
                // 可以根据需要进行处理，例如显示一个提示信息或关闭应用程序
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.submit) {
            // TODO： fix 处理点击事件
//            showUploadConfirmationDialog();

            // 数据上传操作
            List<List<StrokePoint>> strokes_list = StrokeManager.getInstance().getALL();

            String url = "http://ibrain.headdb.cn/api/scale/insertscale";
            PostStrokeObject object = new PostStrokeObject();
            object.setJson(strokes_list);
            Gson gson = new Gson();
            String json_str = gson.toJson(object);

//            String example_str = StringUtils.readRawJsonFile(MainActivity.this, R.raw.example_strokes);
//            Log.e("example", example_str+"");
            OkHttpUtils.getInstance().postAsync(url, json_str, new OkHttpUtils.Callback() {
                @Override
                public void onResponse(String response) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response.contains("200")){
                                // 提交成功重新开始
                                // 清除存储的待上传笔迹、页面存储笔迹以及初始化笔迹
                                StrokeManager.getInstance().clearAll();
                                MyApp.getInstance().setPaperid(null);
                                PointManager.getInstance().clear();
                                dw.initDraw();
                                Toast.makeText(MainActivity.this, "数据上传成功，请准备下次作答", Toast.LENGTH_LONG).show();
                                Log.e("Response", response);
                            }else {
                                Log.e("HTTP", response+"");
                                Toast.makeText(MainActivity.this, "数据上传失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                @Override
                public void onFailure(IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "数据上传失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
            return true;
        } else if (id==R.id.clear) {
            new MaterialAlertDialogBuilder(MainActivity.this)
                .setTitle("笔迹清除")
                .setMessage("确定要删除所有笔迹吗？该操作不可撤回")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        // 清除存储的待上传笔迹、页面存储笔迹以及初始化笔迹
                        StrokeManager.getInstance().clearAll();
                        MyApp.getInstance().setPaperid(null);
                        PointManager.getInstance().clear();
                        dw.initDraw();
                        // 用户点击确认按钮的操作
                        Snackbar.make(findViewById(R.id.container), "数据已删除", Snackbar.LENGTH_SHORT)
                                .setAction("确认", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        // none
                                    }
                                })
                                .show();
                    }
                })
                .setNegativeButton("取消", /* 监听器 */ null)
                .show();
        }

        return super.onOptionsItemSelected(item);
    }

//
//    // 提交数据的确认按钮
//    private void showUploadConfirmationDialog() {
//
//        new MaterialAlertDialogBuilder(MainActivity.this)
//                .setTitle("数据上传")
//                .setMessage("请确保用户已完成所有测试，进行数据上传操作")
//                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        // 用户点击确认按钮的操作
//                        Snackbar.make(findViewById(R.id.container), "模拟数据上传", Snackbar.LENGTH_SHORT)
//                                .setAction("确认", new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View view) {
//                                        // none
//                                    }
//                                })
//                                .show();
//                    }
//                })
//                .setNegativeButton("取消", /* 监听器 */ null)
//                .show();
//    }


}