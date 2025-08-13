package com.bupt.myapplication;


import static com.bupt.myapplication.util.JsonUtil.deleteLocalFile;
import static com.bupt.myapplication.util.JsonUtil.getDataFromLocal;
import static com.bupt.myapplication.util.JsonUtil.saveDataToLocal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;

import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
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
import com.bupt.myapplication.fragment.AboutUsFragment; // Changed from ContactFragment
import com.bupt.myapplication.fragment.IFragmentCallBack;
import com.bupt.myapplication.fragment.IntroductionFragment;
import com.bupt.myapplication.fragment.MyAccumulator;
import com.bupt.myapplication.fragment.MyTimer;
import com.bupt.myapplication.fragment.mmseFragment;
import com.bupt.myapplication.object.UploadStrokeObject;
import com.bupt.myapplication.util.CSVReaderUtil;
import com.bupt.myapplication.util.ErrorInfoFetcher;
import com.bupt.myapplication.util.OkHttpUtils;
import com.bupt.myapplication.data.PointManager;
import com.bupt.myapplication.data.StrokeManager;
import com.bupt.myapplication.data.StrokePoint;
import com.bupt.myapplication.dialog.MainDialogFragment;
import com.bupt.myapplication.dialog.ReuploadDialogFragment;

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
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    // import custom drawing view
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final String CHANNEL_ID = "global_exceptions";

    private MainDialogFragment dialogFragment;
    private ReuploadDialogFragment uploadDialogFragment;
    private FrameLayout timerFrameLayout;
    private FrameLayout recorderFrameLayout;
    private FrameLayout accumulatorFrameLayout;
    private FrameLayout aboutUsFragmentLayout; // Renamed from contactFragmentLayout
    private FrameLayout introductionFragmentLayout;
    private FrameLayout mmsepictureFragmentLayout;

    private String TAG = "MainActivity.class";

    private ConstraintLayout contactUsContainer; // This is the background container R.id.fragment_container
    public DrawingView dw;
    private AboutUsFragment aboutUsFragment; // Renamed and type changed
    private IntroductionFragment introductionFragment;
    private com.bupt.myapplication.fragment.mmseFragment mmseFragment;


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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        dw = findViewById(R.id.drawing_view);

        timerFrameLayout = findViewById(R.id.timer_fragment);
        timerFrameLayout.setVisibility(View.GONE);
        recorderFrameLayout = findViewById(R.id.record_fragment);
        recorderFrameLayout.setVisibility(View.GONE);
        accumulatorFrameLayout = findViewById(R.id.accumulator_fragment);
        accumulatorFrameLayout.setVisibility(View.GONE);
        // IMPORTANT: Ensure R.id.about_us_fragment_container exists in activity_main.xml
        aboutUsFragmentLayout = findViewById(R.id.contact_us_fragment);
        aboutUsFragmentLayout.setVisibility(View.GONE);
        introductionFragmentLayout = findViewById(R.id.fragment_container2);
        introductionFragmentLayout.setVisibility(View.GONE);
        mmsepictureFragmentLayout = findViewById(R.id.mmse_picture_fragment);
        mmsepictureFragmentLayout.setVisibility(View.GONE);

        contactUsContainer = findViewById(R.id.fragment_container); // This is the background R.id.fragment_container
        checkAndRequestPermissions();
        contactUsContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (aboutUsFragment != null && aboutUsFragmentLayout.getVisibility() == View.VISIBLE) {
                        aboutUsFragmentLayout.setVisibility(View.GONE);
                        destroyFragment(aboutUsFragment);
                        aboutUsFragment = null;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "onClick: Error closing AboutUsFragment", e);
                }
                try {
                    if (introductionFragment != null && introductionFragmentLayout.getVisibility() == View.VISIBLE) {
                        introductionFragmentLayout.setVisibility(View.GONE);
                        destroyFragment(introductionFragment);
                        introductionFragment = null;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "onClick: Error closing IntroductionFragment", e);
                }
                try {
                    if (mmseFragment != null && mmsepictureFragmentLayout.getVisibility() == View.VISIBLE) {
                        mmsepictureFragmentLayout.setVisibility(View.GONE);
                        destroyFragment(mmseFragment);
                        mmseFragment = null;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "onClick: Error closing mmseFragment", e);
                }
            }
        });
        timerFrameLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = view.getX() - event.getRawX();
                        dY = view.getY() - event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        view.animate().x(event.getRawX() + dX).y(event.getRawY() + dY).setDuration(0).start();
                        break;
                    case MotionEvent.ACTION_UP:
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
                        dX1 = view.getX() - event.getRawX();
                        dY1 = view.getY() - event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        view.animate().x(event.getRawX() + dX1).y(event.getRawY() + dY1).setDuration(0).start();
                        break;
                    case MotionEvent.ACTION_UP:
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
                        dX1 = view.getX() - event.getRawX();
                        dY1 = view.getY() - event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        view.animate().x(event.getRawX() + dX1).y(event.getRawY() + dY1).setDuration(0).start();
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
        drawerLayout = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView = findViewById(R.id.nav_view);
        toolbox();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.to_be_upload) {
                uploadDialogFragment = new ReuploadDialogFragment();
                uploadDialogFragment.show(getSupportFragmentManager(), "dialog");
            } else if (id == R.id.history) {
                Toast.makeText(this, "功能暂未实现，敬请期待", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.bluetooth_connect) {
                Toast.makeText(this, "功能暂未实现，敬请期待", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.toolbox) {
                toolbox();
                return true;
            } else if (id == R.id.contact_us) { // This R.id.contact_us is the menu item ID
                aboutUsFragmentLayout.setVisibility(View.VISIBLE);
                if (aboutUsFragment == null) { 
                    aboutUsFragment = AboutUsFragment.newInstance();
                    aboutUsFragment.setiFragmentCallBack(new IFragmentCallBack() {
                        @Override
                        public void send2main(String msg) {
                            if ("close_about_us".equals(msg)) {
                                if (aboutUsFragment != null) {
                                    aboutUsFragmentLayout.setVisibility(View.GONE);
                                    destroyFragment(aboutUsFragment);
                                    aboutUsFragment = null; 
                                }
                            }
                        }

                        @Override
                        public String getFromMain(String msg) {
                            return null;
                        }
                    });
                }
                replaceFragment(R.id.contact_us_fragment, aboutUsFragment);
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
                replaceFragment(R.id.timer_fragment, myTimer);
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
                replaceFragment(R.id.accumulator_fragment, myAccumulator);
            } else if (id == R.id.picture) {
                mmsepictureFragmentLayout.setVisibility(View.VISIBLE);
                mmseFragment = new mmseFragment();
                replaceFragment(R.id.mmse_picture_fragment, mmseFragment);
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            navigationView.getMenu().clear(); 
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
        checkBluetoothPermission();
        BLEConnectHandler = new Handler(Looper.getMainLooper());
        Intent intent = new Intent(this, BluetoothLEService.class);
        bindService(intent, coon, Context.BIND_AUTO_CREATE);
        dialogFragment = new MainDialogFragment();
        blueDelegate = new BlueDelegateImpl(dw, dialogFragment);
        dialogFragment.show(getSupportFragmentManager(), "dialog");
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            showOrientationPrompt();
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void showOrientationPrompt() {
        Toast.makeText(MainActivity.this, "为了更好的使用体验，请切换至竖屏使用!", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_item, menu);
        return true;
    }


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
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
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
            int grantResultsSum = 0;
            for (int grantResult : grantResults) {
                grantResultsSum += grantResult;
            }
            if (grantResults.length > 0 && grantResultsSum == PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "权限已被授予，可以进行蓝牙操作");
                for (int i = 0; i < grantResults.length; i++) {
                    Log.e(TAG, "permission " + permissions[i] + ":" + grantResults[i]);
                }
            } else {
                Log.e(TAG, "权限被拒绝，无法执行蓝牙操作");
                Toast.makeText(MainActivity.this, "权限被拒绝，无法执行蓝牙操作",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public void confirm_submit() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
        builder.setTitle("提交确认");
        String alertMsg = "请确认已完成本次评测，提交数据\n";
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

    public void submit_strokes() {
        List<List<StrokePoint>> strokes_list = StrokeManager.getInstance().getALL();
        String url = "https://ibrain.bupt.edu.cn/scaleBackend/scalesSetRecords/androidUpload";
        UploadStrokeObject uploadStroke = new UploadStrokeObject();
        uploadStroke.setScalesSetRecordId(MyApp.getInstance().getParticipantID());
        uploadStroke.setPenMac(MyApp.getInstance().getCurMacAddress());
        LocalDateTime now = LocalDateTime.now();
        String timString = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        uploadStroke.setUploadTime(timString);
        uploadStroke.setStrokesList(strokes_list);
        Gson gson = new Gson();
        List<UploadStrokeObject> uploadList = new ArrayList<>();
        uploadList.add(uploadStroke);
        String json_str = gson.toJson(uploadList);
        Log.e("HTTP", json_str);

        OkHttpUtils.getInstance().postAsync(url, json_str, new OkHttpUtils.Callback() {
            @Override
            public void onResponse(Response response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.code() == 200) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("提交成功")
                                    .setMessage("数据已成功提交！现在开始新的评测。")
                                    .setPositiveButton("好的", (dialogInterface, i) -> {
                                        StrokeManager.getInstance().clearAll();
                                        MyApp.getInstance().setPaperid(null);
                                        MyApp.getInstance().setParticipantID(null);
                                        PointManager.getInstance().clear();
                                        dw.initDraw();
                                        dialogFragment = new MainDialogFragment();
                                        blueDelegate = new BlueDelegateImpl(dw, dialogFragment);
                                        dialogFragment.show(getSupportFragmentManager(), "dialog");
                                    })
                                    .setCancelable(false)
                                    .show();
                            Log.e("Response", response.body().toString());
                        } else {
                            Log.e("HTTP", "上传数据出现未知错误");
                            Log.e("HTTP", response.body().toString() + "");
                            uploadFailed(json_str, timString);
                        }
                    }
                });
            }

            @Override
            public void onFailure(IOException e) {
                uploadFailed(json_str, timString);
            }
        });
    }

    public void uploadFailed(String json_str, String timString) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("上传失败");
                builder.setMessage("数据上传失败，已自动缓存在本地。您稍后可以在“待上传”中重试。");
                builder.setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String fileName = MyApp.getInstance().getParticipantID() + "_" + timString;
                        saveDataToLocal(json_str, fileName, MainActivity.this);
                        StrokeManager.getInstance().clearAll();
                        MyApp.getInstance().setPaperid(null);
                        MyApp.getInstance().setParticipantID(null);
                        PointManager.getInstance().clear();
                        dw.initDraw();
                        dialogFragment = new MainDialogFragment();
                        blueDelegate = new BlueDelegateImpl(dw, dialogFragment);
                        dialogFragment.show(getSupportFragmentManager(), "dialog");
                    }
                });
                builder.setCancelable(false);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    public Map<String, String> getUnCompletedPages() {
        List<Long> pageIds = StrokeManager.getInstance().getStrokePageIDs();
        for (Long id : pageIds) {
            Log.e("PageID", id.toString());
        }
        int scale_v1_page_count = 0;
        int scale_v2_page_count = 0;
        int scale_jingrixing_count = 0;

        for (long page_id : pageIds) {
            if (page_id <= 55287 && page_id >= 55240) {
                scale_v1_page_count++;
            } else if (page_id <= 69179 && page_id >= 69135) {
                scale_v2_page_count++;
                MyApp.getInstance().setScale_name("v2");
            } else if (page_id <= 74533 && page_id >= 74521) {
                scale_jingrixing_count++;
            }
        }
        String scale_name_path = "";
        if (scale_v1_page_count > scale_v2_page_count) {
            MyApp.getInstance().setScale_name("v1");
            scale_name_path = "scale_v1.csv";
        } else if (scale_v1_page_count < scale_v2_page_count) {
            scale_name_path = "scale_v2.csv";
            MyApp.getInstance().setScale_name("v2");
        } else if (scale_v1_page_count == scale_v2_page_count) {
            scale_name_path = "scale_jingrixing.csv";
            MyApp.getInstance().setScale_name("Jingrixing");
        }

        List<CSVReaderUtil.PageMap> pages = CSVReaderUtil.readCSVFile(this, scale_name_path);
        pages.remove(0);
        for (CSVReaderUtil.PageMap page : pages) {
            Long pageID = Long.parseLong(page.pageId);
            if (pageIds.contains(pageID)) {
                page.completed = true;
            }
            String message = "Page ID: " + page.pageId + ", Short Name: " + page.shortName + ", Full Name: " + page.fullName + ", completed: " + page.completed;
            Log.e("pagemap", message);
        }

        List<String> completed_short_name = new ArrayList<>();
        for (CSVReaderUtil.PageMap page : pages) {
            if (page.completed) {
                completed_short_name.add(page.shortName);
            }
        }
        Map<String, String> uncompleted_pages = new HashMap<>();
        for (CSVReaderUtil.PageMap page : pages) {
            if (!page.completed && !completed_short_name.contains(page.shortName)
                    && !page.shortName.equals("EMPTY")
                    && !page.shortName.equals("OTHER")) {
                uncompleted_pages.put(page.shortName, page.fullName);
            }
        }
        return uncompleted_pages;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.submit) {
            confirm_submit();
        } else if (id == R.id.clear) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("笔迹清除")
                    .setMessage("确定要删除所有笔迹吗？该操作不可撤回")
                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            StrokeManager.getInstance().clearAll();
                            MyApp.getInstance().setPaperid(null);
                            PointManager.getInstance().clear();
                            dw.initDraw();
                            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_clear_success, null);
                            Toast toast = new Toast(MainActivity.this);
                            toast.setView(view);
                            toast.setDuration(Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                    })
                    .setNegativeButton("取消", null)
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
        toggle.syncState();
    }

     // 批量重新上传接口
     public void Reupload(ReuploadCallback callback) {
         File externalFilesDir = getExternalFilesDir(null);
         if (externalFilesDir == null) {
             Toast.makeText(MainActivity.this, "存储目录不可用", Toast.LENGTH_SHORT).show();
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
             Toast.makeText(MainActivity.this, "当前没有数据可以上传", Toast.LENGTH_SHORT).show();
             return;
         }

         AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
         String message = "确定重新上传数据";
         builder.setTitle("重新上传提示")
                 .setMessage(message)
                 .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         // 从第一个文件开始上传
                         uploadNextFile(filenames, 0, callback);
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

    /** 递归/顺序上传文件列表中的第 index 个 */
    private void uploadNextFile(List<String> filenames, int index, ReuploadCallback callback) {
        // 所有文件都处理完了
        if (index >= filenames.size()) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("上传结束")
                    .setMessage("所有本地暂存数据已处理完成。")
                    .setPositiveButton("好的", (dialog, which) -> callback.onUploadComplete())
                    .setCancelable(false)
                    .show();
            ReuploadDialogFragment.Refresh();
            return;
        }

        String filename = filenames.get(index);
        String jsonStr = getDataFromLocal(filename, MainActivity.this);

        // === 关键：保持和原来接口尽量一致 ===
        // 原逻辑是：从每个文件中取 JSONArray 的第 0 条记录放到总数组里
        // 这里改成：每次上传一个文件里的第 0 条记录（包在一个数组里发出去）
        String bodyToSend;
        try {
            JSONArray arr = new JSONArray(jsonStr);
            if (arr.length() == 0) {
                // 这个文件是空的，直接删掉，继续下一个
                deleteLocalFile(filename, MainActivity.this);
                Toast.makeText(MainActivity.this,
                        filename + " 为空，已跳过并删除",
                        Toast.LENGTH_SHORT).show();
                ReuploadDialogFragment.Refresh();
                uploadNextFile(filenames, index + 1, callback);
                return;
            }

            JSONObject first = arr.getJSONObject(0);
            JSONArray uploadArr = new JSONArray();
            uploadArr.put(first);
            bodyToSend = uploadArr.toString();   // 形如 [ { ... } ]
        } catch (JSONException e) {
            e.printStackTrace();
            // 解析失败，提示一下，然后继续下一条
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("本地数据格式错误")
                    .setMessage("文件 " + filename + " 内容解析失败，已跳过。")
                    .setPositiveButton("继续上传下一条", (d, w) -> {
                        // 跳过这个文件，不删除的话可以留给人工排查
                        uploadNextFile(filenames, index + 1, callback);
                    })
                    .setCancelable(false)
                    .show();
            return;
        }

        String url = "https://ibrain.bupt.edu.cn/scaleBackend/scalesSetRecords/androidUpload";

        OkHttpUtils.getInstance().postAsync(url, bodyToSend, new OkHttpUtils.Callback() {
            @Override
            public void onResponse(Response response) {
                runOnUiThread(() -> {
                    if (response.code() == 200) {
                        // 当前这条上传成功
                        deleteLocalFile(filename, MainActivity.this);
                        ReuploadDialogFragment.Refresh();

                        Toast.makeText(MainActivity.this,
                                filename + " 上传成功并已删除本地数据",
                                Toast.LENGTH_SHORT).show();

                        // 继续下一条
                        uploadNextFile(filenames, index + 1, callback);
                    } else {
                        // 非 200，走 ErrorInfoFetcher，展示具体错误
                        ErrorInfoFetcher.fetch(response, new ErrorInfoFetcher.Callback() {
                            @Override
                            public void onResult(String fullErrorText) {
                                runOnUiThread(() -> {

                                    ReuploadDialogFragment.Refresh();

                                    if (isDuplicateSubmitError(fullErrorText)) {
                                        // 👉 情况1：重复提交，可以给用户选择是否删除本地数据
                                        new AlertDialog.Builder(MainActivity.this)
                                                .setTitle("检测到重复提交")
                                                .setMessage(fullErrorText + "\n\n是否删除当前这条本地数据？")
                                                .setPositiveButton("删除并继续下一条", (d, w) -> {
                                                    // 删除当前文件
                                                    deleteLocalFile(filename, MainActivity.this);
                                                    ReuploadDialogFragment.Refresh();

                                                    Toast.makeText(MainActivity.this,
                                                            filename + " 为重复提交，已删除本地数据",
                                                            Toast.LENGTH_SHORT).show();

                                                    // 继续上传下一条
                                                    uploadNextFile(filenames, index + 1, callback);
                                                })
                                                .setNegativeButton("保留并继续下一条", (d, w) -> {
                                                    // 不删除当前文件，用户以后可以再处理
                                                    uploadNextFile(filenames, index + 1, callback);
                                                })
                                                .setCancelable(false)
                                                .show();

                                    } else {
                                        // 👉 情况2：其他类型错误，按原来的“失败提示 + 继续下一条”逻辑
                                        new AlertDialog.Builder(MainActivity.this)
                                                .setTitle("上传失败")
                                                .setMessage(fullErrorText)
                                                .setPositiveButton("继续上传下一条", (d, w) -> {
                                                    // 当前文件保留不删，留待以后重传
                                                    uploadNextFile(filenames, index + 1, callback);
                                                })
                                                .setCancelable(false)
                                                .show();
                                    }
                                });
                            }

                            @Override
                            public void onFailed(String reason) {
                                runOnUiThread(() -> {
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setTitle("上传失败")
                                            .setMessage("获取错误详情失败：" + reason)
                                            .setPositiveButton("继续上传下一条", (d, w) -> {
                                                // 不删除本地文件，留待后续重试
                                                uploadNextFile(filenames, index + 1, callback);
                                            })
                                            .setCancelable(false)
                                            .show();
                                });
                            }
                        });

                    }
                });
            }

            @Override
            public void onFailure(IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("上传失败")
                            .setMessage("网络/服务器异常：" + e.getMessage())
                            .setPositiveButton("继续上传下一条", (dialog, which) -> {
                                // 不删除本地文件，留待后续重试
                                uploadNextFile(filenames, index + 1, callback);
                            })
                            .setCancelable(false)
                            .show();
                });
            }
        });
    }

    /** 判断错误信息里是否是“重复提交”的情况 */
    private boolean isDuplicateSubmitError(String msg) {
        if (msg == null) return false;
        return msg.contains("此份文件设置的上传时间已存在于系统")
                || msg.contains("重复提交了相同的笔迹文件")
                || msg.contains("重复提交"); // 冗余保险
    }

    private void replaceFragment(int containerId, Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(containerId, fragment);
        transaction.commitAllowingStateLoss(); // Changed to commitAllowingStateLoss for robustness 
    }

    private void destroyFragment(Fragment fragment) {
        if (fragment != null && !getSupportFragmentManager().isStateSaved()) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.remove(fragment);
            transaction.commitAllowingStateLoss(); // Changed to commitAllowingStateLoss for robustness
        }
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
                Manifest.permission.RECORD_AUDIO,
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

    private void catchGlobalException() {
        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            if (ex instanceof IndexOutOfBoundsException) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(MyApp.getInstance(),
                                    "检测到蓝牙笔异常，已自动忽略并继续运行",
                                    Toast.LENGTH_SHORT)
                            .show();
                });
                Log.e("GlobalCatch", "捕获到 IndexOutOfBoundsException，已忽略", ex);
            } else {
                Thread.getDefaultUncaughtExceptionHandler()
                        .uncaughtException(thread, ex);
            }
        });
    }

}
