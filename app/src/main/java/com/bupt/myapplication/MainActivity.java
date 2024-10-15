package com.bupt.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;

import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.bbb.bpen.binder.BiBiBinder;
import com.bbb.bpen.delegate.BlueDelegate;
import com.bbb.bpen.service.BluetoothLEService;
import com.bupt.myapplication.bbbdraw.BlueDelegateImpl;
import com.bupt.myapplication.fragment.ContactFragment;
import com.bupt.myapplication.fragment.IFragmentCallBack;
import com.bupt.myapplication.fragment.IntroductionFragment;
import com.bupt.myapplication.fragment.MyAccumulator;
import com.bupt.myapplication.fragment.MyTimer;
import com.bupt.myapplication.fragment.mmseFragment;
import com.bupt.myapplication.util.CSVReaderUtil;
import com.bupt.myapplication.util.OkHttpUtils;
import com.bupt.myapplication.data.PointManager;
import com.bupt.myapplication.data.StrokeManager;
import com.bupt.myapplication.data.StrokePoint;
import com.bupt.myapplication.dialog.MyDialogFragment;
import com.bupt.myapplication.dialog.ReuploadDialogFragment;
import com.bupt.myapplication.object.PostStrokeObject;
import com.bupt.myapplication.view.DrawingView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    // import custom drawing view
    private static final int PERMISSION_REQUEST_CODE = 200;

    private MyDialogFragment dialogFragment;
    private ReuploadDialogFragment uploadDialogFragment;
    private FrameLayout timerFrameLayout;
    private FrameLayout recorderFrameLayout;
    private FrameLayout accumulatorFrameLayout;
    private FrameLayout contactFragmentLayout;
    private FrameLayout introductionFragmentLayout;
    private FrameLayout mmsepictureFragmentLayout;

    private ConstraintLayout contactUsContainer;
    public DrawingView dw;
    private ContactFragment contactFragment;
    private IntroductionFragment introductionFragment;
    private com.bupt.myapplication.fragment.mmseFragment mmseFragment;
    private String timeStamp;

    private static String TAG = "MainActivityClass";

    public Handler BLEConnectHandler;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private float dX, dY;
    private float dX1, dY1;


    public BlueDelegate blueDelegate;

    static {
        System.loadLibrary("bbbdraw");
    }

    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        //设置 Activity 的布局文件为 activity_main.xml
        //@SuppressLint("ResourceType") View toolbarView = LayoutInflater.from(this).inflate(R.menu.toolbar_item, null); // change
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //顶部导航条
        dw = findViewById(R.id.drawing_view);

        timerFrameLayout = findViewById(R.id.timer_fragment);
        timerFrameLayout.setVisibility(View.GONE);
        recorderFrameLayout = findViewById(R.id.record_fragment);
        recorderFrameLayout.setVisibility(View.GONE);
        accumulatorFrameLayout = findViewById(R.id.accumulator_fragment);
        accumulatorFrameLayout.setVisibility(View.GONE);
        contactFragmentLayout = findViewById(R.id.contact_us_fragment);
        contactFragmentLayout.setVisibility(View.GONE);
        introductionFragmentLayout = findViewById(R.id.fragment_container2);
        introductionFragmentLayout.setVisibility(View.GONE);
        mmsepictureFragmentLayout = findViewById(R.id.mmse_picture_fragment);
        mmsepictureFragmentLayout.setVisibility(View.GONE);

        contactUsContainer = findViewById(R.id.fragment_container);
        checkAndRequestPermissions();
        contactUsContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    contactFragmentLayout.setVisibility(View.GONE);
                    destroyFragment(contactFragment);
                } catch (Exception e) {

                }
                try {
                    introductionFragmentLayout.setVisibility(View.GONE);
                    destroyFragment(introductionFragment);
                } catch (Exception e) {

                }
                try {
                    mmsepictureFragmentLayout.setVisibility(View.GONE);
                    destroyFragment(mmseFragment);
                } catch (Exception e) {

                }
            }
        });
        timerFrameLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 记录触摸点与TextView左上角的距离
                        dX = view.getX() - event.getRawX();
                        dY = view.getY() - event.getRawY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        // 更新TextView的位置
                        view.animate()
                                .x(event.getRawX() + dX)
                                .y(event.getRawY() + dY)
                                .setDuration(0)
                                .start();
                        break;

                    case MotionEvent.ACTION_UP:
                        // 可以在这里添加你想要的任何代码，比如更新布局参数等
                        break;

                    default:
                        return false;
                }
                return true;
            }
        });
        recorderFrameLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 记录触摸点与TextView左上角的距离
                        dX1 = view.getX() - event.getRawX();
                        dY1 = view.getY() - event.getRawY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        // 更新TextView的位置
                        view.animate()
                                .x(event.getRawX() + dX1)
                                .y(event.getRawY() + dY1)
                                .setDuration(0)
                                .start();
                        break;

                    case MotionEvent.ACTION_UP:
                        // 可以在这里添加你想要的任何代码，比如更新布局参数等
                        break;

                    default:
                        return false;
                }
                return true;
            }
        });
        accumulatorFrameLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 记录触摸点与TextView左上角的距离
                        dX1 = view.getX() - event.getRawX();
                        dY1 = view.getY() - event.getRawY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        // 更新TextView的位置
                        view.animate()
                                .x(event.getRawX() + dX1)
                                .y(event.getRawY() + dY1)
                                .setDuration(0)
                                .start();
                        break;

                    case MotionEvent.ACTION_UP:
                        // 可以在这里添加你想要的任何代码，比如更新布局参数等
                        break;

                    default:
                        return false;
                }
                return true;
            }
        });
        //都在activity_main里面改
        drawerLayout = findViewById(R.id.drawer_layout);

        // Set up the toggle to open and close the drawer
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView = findViewById(R.id.nav_view);
        toolbox();

        // Set up the navigation view listener
        navigationView.setNavigationItemSelectedListener(item -> {
            // Handle navigation view item clicks here.
            int id = item.getItemId();

//            if (id == R.id.instruction) {
//                introductionFragmentLayout.setVisibility(View.VISIBLE);
//                introductionFragment = new IntroductionFragment();
//                replaceFragment3(introductionFragment);
//            }
            if (id == R.id.to_be_upload) {
                uploadDialogFragment = new ReuploadDialogFragment();
                uploadDialogFragment.show(getSupportFragmentManager(), "dialog");
            } else if (id == R.id.history) {
                Toast.makeText(this, "功能暂未实现", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.bluetooth_connect) {
                dialogFragment.show(getSupportFragmentManager(), "dialog");
            } else if (id == R.id.toolbox) {
                toolbox();
                return true;
            } else if (id == R.id.contact_us) {
                contactFragmentLayout.setVisibility(View.VISIBLE);
                contactFragment = new ContactFragment();
                replaceFragment2(contactFragment);
            } else if (id == R.id.timer) {
                timerFrameLayout.setVisibility(View.VISIBLE);
                MyTimer myTimer = new MyTimer();
                myTimer.setiFragmentCallBack(new IFragmentCallBack() {
                    @Override
                    public void send2main(String msg) {
                        if (msg.equals("close")) {
                            destroyFragment(myTimer);
                            timerFrameLayout.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public String getFromMain(String msg) {
                        return null;
                    }
                });
                replaceFragment(myTimer);
            } else if (id == R.id.accumulator) {
                accumulatorFrameLayout.setVisibility(View.VISIBLE);
                MyAccumulator myAccumulator = new MyAccumulator();
                myAccumulator.setiFragmentCallBack(new IFragmentCallBack() {
                    @Override
                    public void send2main(String msg) {
                        if (msg.equals("close")) {
                            destroyFragment(myAccumulator);
                            accumulatorFrameLayout.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public String getFromMain(String msg) {
                        return null;
                    }
                });
                replaceFragment4(myAccumulator);
            } else if (id == R.id.picture) {
                mmsepictureFragmentLayout.setVisibility(View.VISIBLE);
                mmseFragment = new mmseFragment();
                replaceFragment5(mmseFragment);
            }
            // Close the drawer
            drawerLayout.closeDrawer(GravityCompat.START);
            navigationView.getMenu().clear(); // 清除侧边栏菜单
            navigationView.inflateMenu(R.menu.drawer_view);
            toolbox();
            return true;
        });


        init();
    }

    public void closeFragment(View view) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (fragment != null) {
            fragmentManager.beginTransaction().remove(fragment).commit();
        }
    }


    private void toolbox() {
        Menu menu = navigationView.getMenu();
        MenuItem subMenu1 = menu.findItem(R.id.timer);
        MenuItem subMenu2 = menu.findItem(R.id.record);
        MenuItem subMenu3 = menu.findItem(R.id.accumulator);
        MenuItem subMenu4 = menu.findItem(R.id.picture);

        subMenu1.setVisible(true);
        subMenu2.setVisible(true);
        subMenu3.setVisible(true);
        subMenu4.setVisible(true);

    }

    private void init() {
        // 权限授予
        checkBluetoothPermission();
        Log.d("test", "checkBluetoothPermission ok");

        // 初始化蓝牙笔连接service
        BLEConnectHandler = new Handler(Looper.getMainLooper());
        Intent intent = new Intent(this, BluetoothLEService.class);
        bindService(intent, coon, Context.BIND_AUTO_CREATE);


        // 处理相关的蓝牙笔连接和笔迹处理
        dialogFragment = new MyDialogFragment();
        blueDelegate = new BlueDelegateImpl(dw, dialogFragment);
        dialogFragment.show(getSupportFragmentManager(), "dialog");

        // showMyDialog();

        // 检测屏幕方向
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 如果是横屏，提示一下
            showOrientationPrompt();
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


    }

    private void showOrientationPrompt() {
        // 强制转成竖屏会更好
        Toast.makeText(MainActivity.this, "为了更好的使用体验，请切换至竖屏使用!", Toast.LENGTH_LONG).show();

    }

    //创建菜单栏
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_item, menu);
        return true;
    }



    //间隔十秒扫描一次

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


    public boolean confirm_submit_old(){
        // 数据上传操作
        String message = "个人信息未填写完整，请检查个人信息后再次上传";
        List<List<StrokePoint>> strokes_list = StrokeManager.getInstance().getALL();//获取所有笔迹数据

        message = "";
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("上传提示");

        //上传前确认每一页做答情况
        // TODO： 这里做了量表是否完整的点击事件，v7量表触发个bug，需要修改StrokeManager，暂时套了层try catch
        try {
            boolean[] IsWrite = StrokeManager.getStrokePages(strokes_list);
            if (!IsWrite[14]) {
                builder.setMessage(message);
                builder.setPositiveButton("关闭", null);
                builder.show();
                return false;
            }
            boolean unfinished = false;
            for (int i = 16; i <= 55; i++) {
                if (i == 26) continue;
                if (!IsWrite[i]) {
                    unfinished = true;
                    break;
                }
            }

            // 如果存在未填写页码
            if (unfinished) {
                builder.setTitle("以下问卷未作答，是否继续上传？");
                // 构建提示信息
                if (!IsWrite[16] || !IsWrite[17]) message += "主观认知衰退量表(SCDS)\n";
                if (!IsWrite[18] || !IsWrite[19]) message += "简易智力状态检测表(MMSE)\n";
                if (!IsWrite[20] || !IsWrite[21] || !IsWrite[22] || !IsWrite[23])
                    message += "蒙特利尔认知评估(MOCA)\n";
                if (!IsWrite[24]) message += "画钟测验(CDT)\n";
                if (!IsWrite[25]) message += "听觉词语学习测验(AVLT)\n";
                if (!IsWrite[27] || !IsWrite[28]) message += "连线测验(TMT) 测试1\n";
                if (!IsWrite[29] || !IsWrite[30]) message += "连线测验(TMT) 测试2\n";
                if (!IsWrite[31]) message += "数字广度测验(DST)\n";
                if (!IsWrite[32]) message += "词语流畅度测验(VFT)-A\n";
                if (!IsWrite[33]) message += "词语流畅度测验(VFT)-B\n";
                if (!IsWrite[34]) message += "词语流畅度测验(VFT)-C\n";
                if (!IsWrite[35] || !IsWrite[36] || !IsWrite[37])
                    message += "汉密尔顿抑郁量表(HAMD)\n";
                if (!IsWrite[38]) message += "汉密尔顿抑郁量表(HAMA)\n";
                if (!IsWrite[39]) message += "老年人生活能力(ADL)\n";
                if (!IsWrite[40] || !IsWrite[41]) message += "匹兹堡睡眠质量指数(PSQI)\n";
                if (!IsWrite[42] || !IsWrite[43]) message += "神经精神科问卷(NPI)\n";
                if (!IsWrite[44]) message += "痴呆病感缺失问卷—被试版(AQD)\n";
                if (!IsWrite[45]) message += "痴呆病感缺失问卷—知情者版(AQD)\n";
                if (!IsWrite[46]) message += "Hachinski缺血指数量表(HIS)\n";
                if (!IsWrite[47] || !IsWrite[48] || !IsWrite[50]) message += "复杂图形测验(CFT)\n";
                if (!IsWrite[51] || !IsWrite[52] || !IsWrite[53] || !IsWrite[54] || !IsWrite[55])
                    message += "临床痴呆指标(CDR)\n";
                if (!IsWrite[49]) message += "老年抑郁量表(GDS简化版)\n";
            } else {
                builder.setTitle("上传提示");
                message += "您已完成全部作答，是否继续上传";
            }
        }catch (Exception e){
            // Todo: Fix above bug
            builder.setTitle("上传提示");
            message += "您已完成全部作答，是否继续上传";
        }
        // 弹出确认对话框
        builder.setMessage(message)
                .setPositiveButton("继续上传", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton("取消", null);

        // 设置对话框的最大高度
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setLayout(1320, 600);
        return false;
    }

    // 检查量表是否做完，弹出弹框展示是否需要上传数据
    public void confirm_submit(){
        // 判定是否允许提交
        Map<String, String> uncompleted_pages = getUnCompletedPages();
        String msg = "";
        boolean profile_completed = true;
        for (Map.Entry<String, String> entry : uncompleted_pages.entrySet()) {
            String short_name = entry.getKey();
            String full_name = entry.getValue();
            msg = msg + full_name + " ";
            if (short_name.equals("PROFILE")){
                profile_completed = false;
            }
        }

        // 如果个人信息页没有填写不允许提交
        if (!profile_completed){
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
            builder.setTitle("上传提示");
            builder.setMessage("个人信息页没有填写，请填写后提交");
            builder.setPositiveButton("关闭", null);
            builder.show();
        }
        else{
            // 弹窗确认
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
            builder.setTitle("提交确认");
            String alertMsg = "以下部分未填写，是否确认提交？\n"+msg;
            builder.setMessage(alertMsg);
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    submit_strokes();
                }
            });
            builder.setNegativeButton("关闭", null);

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    // 向后端提交全部数据，并初始化
    public void submit_strokes(){
        List<List<StrokePoint>> strokes_list = StrokeManager.getInstance().getALL();//获取所有笔迹数据

        String url = "http://ibrain.bupt.edu.cn/api/scale/insertscale";
        PostStrokeObject object = new PostStrokeObject();
        object.setJson(strokes_list);
        LocalDateTime now = LocalDateTime.now();
        String timString = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        object.setTim(timString);
        object.setPenId(GlobalVars.getInstance().getGlobalAddr());
        timeStamp = String.valueOf(System.currentTimeMillis());
        object.setTimeStamp(timeStamp);
        Gson gson = new Gson();
        String json_str = gson.toJson(object);
        //转换为json字符串储存
//            String example_str = StringUtils.readRawJsonFile(MainActivity.this, R.raw.example_strokes);
//            Log.e("example", example_str+"");
        OkHttpUtils.getInstance().postAsync(url, json_str, new OkHttpUtils.Callback() {
            @Override
            public void onResponse(String response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.contains("200")) {
                            // 提交成功重新开始
                            // 清除存储的待上传笔迹、页面存储笔迹以及初始化笔迹
                            StrokeManager.getInstance().clearAll();
                            MyApp.getInstance().setPaperid(null);
                            PointManager.getInstance().clear();
                            dw.initDraw();
                            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_upload_success, null);
                            Toast toast = new Toast(MainActivity.this);
                            toast.setView(view);
                            toast.setDuration(Toast.LENGTH_SHORT);
                            toast.show();

                            Log.e("Response", response);

                        } else {
                            Log.e("HTTP", response + "");
                        }
                    }
                });
            }

            @Override
            public void onFailure(IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_upload_failed, null);
                        Toast toast = new Toast(MainActivity.this);
                        toast.setView(view);
                        toast.setDuration(Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
//                                            UndoButtonUnEnabled();
                        saveDataToLocal(json_str, timString);//本地存储
                        //Log.e("debug", "ok");

                        // 清除存储的待上传笔迹、页面存储笔迹以及初始化笔迹
                        StrokeManager.getInstance().clearAll();
                        MyApp.getInstance().setPaperid(null);
                        PointManager.getInstance().clear();
                        dw.initDraw();
                    }
                });
            }
        });
    }

    public Map<String, String> getUnCompletedPages(){
        // 获取当前存储的全部ID,已经做完的
        List<Long> pageIds =  StrokeManager.getInstance().getStrokePageIDs();

        for(Long id:pageIds){
            Log.e("PageID", id.toString());
        }
        // 判断量表的版本，判定页码数在v1的55240-55287之间的多，还是在V2的69135-69179的页码数量
        int scale_v1_page_count = 0;
        int scale_v2_page_count = 0;
        for (long page_id: pageIds){
            if (page_id<=55287 && page_id>=55240)
                scale_v1_page_count++;
            else if (page_id<=69179 && page_id>=69135)
                scale_v2_page_count++;
        }
        String scale_name_path = "";
        if (scale_v1_page_count>scale_v2_page_count)
            scale_name_path = "scale_v1.csv";
        else
            scale_name_path = "scale_v2.csv";


        // 读取坐标和名称对应的csv，第一个元素是表头，直接去除掉
        List<CSVReaderUtil.PageMap> pages = CSVReaderUtil.readCSVFile(this, scale_name_path);
        pages.remove(0);
        for (CSVReaderUtil.PageMap page : pages) {
            // 处理你的数据，例如显示在UI上
            Long pageID = Long.parseLong(page.pageId);
            if(pageIds.contains(pageID)){
                page.completed=true;
            }
            String message = "Page ID: " + page.pageId + ", Short Name: " + page.shortName + ", Full Name: " + page.fullName +", completed: " + page.completed;
            Log.e("pagemap", message);
        }

        // 获取未完成页面的信息
        List<String> completed_short_name = new ArrayList<>();
        for (CSVReaderUtil.PageMap page : pages){
            if (page.completed) {
                completed_short_name.add(page.shortName);
            }
        }
        Map<String, String> uncompleted_pages = new HashMap<>();
        for (CSVReaderUtil.PageMap page: pages){
            if (!page.completed && !completed_short_name.contains(page.shortName)
                    && !page.shortName.equals("EMPTY")
                    && !page.shortName.equals("OTHER")){
                uncompleted_pages.put(page.shortName, page.fullName);
            }
        }

        return uncompleted_pages;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.submit) {//提交按钮点击事件处理
            confirm_submit();
        } else if (id == R.id.clear) {//清空按钮点击事件处理
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("笔迹清除")
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
                            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_clear_success, null);
                            Toast toast = new Toast(MainActivity.this);
                            toast.setView(view);
                            toast.setDuration(Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
//                            UndoButtonUnEnabled();
                        }
                    })
                    .setNegativeButton("取消", /* 监听器 */ null)
                    .show();
        }

        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        toggle.syncState();
    }

    //保存
    public void saveDataToLocal(String json, String fileName) {
        try {
            File file = new File(getExternalFilesDir(null), fileName + ".json");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(json.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //获取
    public String getDataFromLocal(String fileName) {
        try {
            File file = new File(getExternalFilesDir(null), fileName);
            FileInputStream fis = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            fis.read(bytes);
            fis.close();
            return new String(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    //删除
    public boolean deleteLocalFile(String fileName) {
        try {
            File file = new File(getExternalFilesDir(null), fileName);
            return file.delete();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public int fail_num = 0;
    public int num_of_files = 0;

    public void Reupload(ReuploadCallback callback) {
        File externalFilesDir = getExternalFilesDir(null);
        File[] files = externalFilesDir.listFiles();
        List<String> jsonFiles = new ArrayList<>();
        num_of_files = 0;
        for (File file : files) {
            if (file.getName().endsWith(".json")) {
                jsonFiles.add(file.getName());
                num_of_files++;
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        String message = "发现" + num_of_files + "份未上传记录，是否重新上传";
        builder.setTitle("重新上传提示")
                .setMessage(message)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (num_of_files == 0) {
                            callback.onUploadFailed();
                        }
                        String url = "http://ibrain.bupt.edu.cn/api/scale/insertscale";
                        for (String filename : jsonFiles) {
                            OkHttpUtils.getInstance().postAsync(url, getDataFromLocal(filename), new OkHttpUtils.Callback() {
                                @Override
                                public void onResponse(String response) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (response.contains("200")) {
                                                deleteLocalFile(filename);
                                                Log.e("Response", response);
                                                ReuploadDialogFragment.Refresh();
                                            } else {
                                                Log.e("HTTP", response + "");
                                            }
                                            num_of_files--;
                                            if (num_of_files == 0) {
                                                if (fail_num != 0) {
                                                    Toast.makeText(MainActivity.this, fail_num + "条信息上传失败!", Toast.LENGTH_LONG).show();
                                                    fail_num = 0;
                                                    callback.onUploadFailed();
                                                } else {
                                                    Toast.makeText(MainActivity.this, "本地存储笔迹已全部上传成功", Toast.LENGTH_LONG).show();
                                                    callback.onUploadComplete();
                                                }
                                            }
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(IOException e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            fail_num++;
                                            num_of_files--;
                                            if (num_of_files == 0) {
                                                if (fail_num != 0) {
                                                    Toast.makeText(MainActivity.this, fail_num + "条信息上传失败!", Toast.LENGTH_LONG).show();
                                                    fail_num = 0;
                                                    callback.onUploadFailed();
                                                } else {
                                                    Toast.makeText(MainActivity.this, "本地存储笔迹已全部上传成功", Toast.LENGTH_LONG).show();
                                                    callback.onUploadComplete();
                                                }
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        callback.onUploadFailed();
                    }
                })
                .show();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.timer_fragment, fragment);
        transaction.commit();
    }

    private void replaceFragment1(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.record_fragment, fragment);
        transaction.commit();
    }

    private void replaceFragment2(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.contact_us_fragment, fragment);
        transaction.commit();
    }

    private void replaceFragment3(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container2, fragment);
        transaction.commit();
    }

    private void replaceFragment4(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.accumulator_fragment, fragment);
        transaction.commit();
    }

    private void replaceFragment5(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.mmse_picture_fragment, fragment);
        transaction.commit();
    }

    private void destroyFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.remove(fragment);
        transaction.commit();
    }




    private void checkAndRequestPermissions() {
        String[] permissions = new String[]{
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_PRIVILEGED,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.RECORD_AUDIO
        };

        if (!hasPermissions(permissions)) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    private boolean hasPermissions(String... permissions) {
        if (permissions != null) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}