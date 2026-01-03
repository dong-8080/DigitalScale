package com.bupt.myapplication;


import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
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
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bbb.bpen.binder.BiBiBinder;
import com.bbb.bpen.delegate.BlueDelegate;
import com.bbb.bpen.service.BluetoothLEService;
import com.bupt.myapplication.bbbdraw.BlueDelegateImpl;
import com.bupt.myapplication.fragment.AboutUsFragment;
import com.bupt.myapplication.fragment.FeedbackFragment;
import com.bupt.myapplication.fragment.IFragmentCallBack;
import com.bupt.myapplication.fragment.IntroductionFragment;
import com.bupt.myapplication.fragment.MyAccumulator;
import com.bupt.myapplication.fragment.MyTimer;
import com.bupt.myapplication.fragment.mmseFragment;
import com.bupt.myapplication.util.CSVReaderUtil;
import com.bupt.myapplication.util.DraggableTouchListener;
import com.bupt.myapplication.util.FragmentManagerHelper;
import com.bupt.myapplication.util.PermissionHelper;
import com.bupt.myapplication.util.StrokeUploadManager;
import com.bupt.myapplication.data.PointManager;
import com.bupt.myapplication.data.StrokeManager;
import com.bupt.myapplication.dialog.MainDialogFragment;
import com.bupt.myapplication.dialog.ReuploadDialogFragment;
import com.bupt.myapplication.view.DrawingView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private MainDialogFragment dialogFragment;
    private ReuploadDialogFragment uploadDialogFragment;
    private FrameLayout timerFrameLayout;
    private FrameLayout accumulatorFrameLayout;
    private FrameLayout aboutUsFragmentLayout;
    private FrameLayout introductionFragmentLayout;
    private FrameLayout mmsepictureFragmentLayout;

    private ConstraintLayout contactUsContainer;
    public DrawingView dw;
    private AboutUsFragment aboutUsFragment;
    private FeedbackFragment feedbackFragment;
    private IntroductionFragment introductionFragment;
    private com.bupt.myapplication.fragment.mmseFragment mmseFragment;

    public Handler BLEConnectHandler;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    public BlueDelegate blueDelegate;

    private StrokeUploadManager uploadManager;
    private AlertDialog submitUploadingDialog;

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
        accumulatorFrameLayout = findViewById(R.id.accumulator_fragment);
        accumulatorFrameLayout.setVisibility(View.GONE);
        // IMPORTANT: Ensure R.id.about_us_fragment_container exists in activity_main.xml
        aboutUsFragmentLayout = findViewById(R.id.contact_us_fragment);
        aboutUsFragmentLayout.setVisibility(View.GONE);
        introductionFragmentLayout = findViewById(R.id.fragment_container2);
        introductionFragmentLayout.setVisibility(View.GONE);
        mmsepictureFragmentLayout = findViewById(R.id.mmse_picture_fragment);
        mmsepictureFragmentLayout.setVisibility(View.GONE);

        contactUsContainer = findViewById(R.id.fragment_container);
        PermissionHelper.checkAndRequestPermissions(this);
        setupContactUsContainer();
        setupDraggableFragments();
        drawerLayout = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.to_be_upload) {
                uploadDialogFragment = new ReuploadDialogFragment();
                uploadDialogFragment.show(getSupportFragmentManager(), "dialog");
            } else if (id == R.id.history) {
                Toast.makeText(this, "功能暂未实现，敬请期待", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.bluetooth_connect) {
                Toast.makeText(this, "功能暂未实现，敬请期待", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.contact_us) {
                aboutUsFragmentLayout.setVisibility(View.VISIBLE);
                if (feedbackFragment == null) {
                    feedbackFragment = FeedbackFragment.newInstance();
                    feedbackFragment.setiFragmentCallBack(new IFragmentCallBack() {
                        @Override
                        public void send2main(String msg) {
                            if ("close_feedback".equals(msg)) {
                                if (feedbackFragment != null) {
                                    aboutUsFragmentLayout.setVisibility(View.GONE);
                                    FragmentManagerHelper.destroyFragment(getSupportFragmentManager(), feedbackFragment);
                                    feedbackFragment = null;
                                }
                            }
                        }

                        @Override
                        public String getFromMain(String msg) {
                            return null;
                        }
                    });
                }
                FragmentManagerHelper.replaceFragment(getSupportFragmentManager(), R.id.contact_us_fragment, feedbackFragment);
            } else if (id == R.id.timer) {
                showTimerFragment();
            } else if (id == R.id.accumulator) {
                showAccumulatorFragment();
            } else if (id == R.id.picture) {
                mmsepictureFragmentLayout.setVisibility(View.VISIBLE);
                mmseFragment = new mmseFragment();
                FragmentManagerHelper.replaceFragment(getSupportFragmentManager(), R.id.mmse_picture_fragment, mmseFragment);
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        init();
    }

    public void closeFragment(View view) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (fragment != null) {
            FragmentManagerHelper.destroyFragment(fragmentManager, fragment);
        }
    }

    private void showTimerFragment() {
        timerFrameLayout.setVisibility(View.VISIBLE);
        MyTimer myTimer = new MyTimer();
        myTimer.setiFragmentCallBack(createCloseCallback(myTimer, timerFrameLayout));
        FragmentManagerHelper.replaceFragment(getSupportFragmentManager(), R.id.timer_fragment, myTimer);
    }

    private void showAccumulatorFragment() {
        accumulatorFrameLayout.setVisibility(View.VISIBLE);
        MyAccumulator myAccumulator = new MyAccumulator();
        myAccumulator.setiFragmentCallBack(createCloseCallback(myAccumulator, accumulatorFrameLayout));
        FragmentManagerHelper.replaceFragment(getSupportFragmentManager(), R.id.accumulator_fragment, myAccumulator);
    }

    private IFragmentCallBack createCloseCallback(Fragment fragment, FrameLayout layout) {
        return new IFragmentCallBack() {
            @Override
            public void send2main(String msg) {
                if ("close".equals(msg)) {
                    FragmentManagerHelper.destroyFragment(getSupportFragmentManager(), fragment);
                    layout.setVisibility(View.GONE);
                }
            }

            @Override
            public String getFromMain(String msg) {
                return null;
            }
        };
    }


    private void init() {
        BLEConnectHandler = new Handler(Looper.getMainLooper());
        Intent intent = new Intent(this, BluetoothLEService.class);
        bindService(intent, coon, Context.BIND_AUTO_CREATE);
        dialogFragment = new MainDialogFragment();
        blueDelegate = new BlueDelegateImpl(dw, dialogFragment);
        dialogFragment.show(getSupportFragmentManager(), "dialog");
        
        uploadManager = new StrokeUploadManager(this, dw, this::resetAfterUpload);
        
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            showOrientationPrompt();
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void resetAfterUpload() {
        dialogFragment = new MainDialogFragment();
        blueDelegate = new BlueDelegateImpl(dw, dialogFragment);
        dialogFragment.show(getSupportFragmentManager(), "dialog");
    }

    private void setupContactUsContainer() {
        contactUsContainer.setOnClickListener(view -> {
            closeFragmentIfVisible(aboutUsFragment, aboutUsFragmentLayout);
            closeFragmentIfVisible(introductionFragment, introductionFragmentLayout);
            closeFragmentIfVisible(mmseFragment, mmsepictureFragmentLayout);
        });
    }

    private void closeFragmentIfVisible(Fragment fragment, FrameLayout layout) {
        try {
            if (fragment != null && layout.getVisibility() == View.VISIBLE) {
                layout.setVisibility(View.GONE);
                FragmentManagerHelper.destroyFragment(getSupportFragmentManager(), fragment);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error closing fragment", e);
        }
    }

    private void setupDraggableFragments() {
        DraggableTouchListener dragListener = new DraggableTouchListener();
        timerFrameLayout.setOnTouchListener(dragListener);
        accumulatorFrameLayout.setOnTouchListener(dragListener);
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.handlePermissionResult(this, requestCode, permissions, grantResults);
    }

    public void confirm_submit() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("提交确认")
                .setMessage("请确认已完成本次评测，提交数据\n")
                .setPositiveButton("确认", (dialog, id) -> {
                    if (uploadManager != null) {
                        showSubmitUploadingDialog();
                        uploadManager.submitStrokes(this::hideSubmitUploadingDialog);
                    }
                })
                .setNegativeButton("关闭", null)
                .show();
    }

    private void showSubmitUploadingDialog() {
        if (submitUploadingDialog != null && submitUploadingDialog.isShowing()) return;
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_uploading, null, false);
        TextView tv = v.findViewById(R.id.tv_uploading);
        if (tv != null) tv.setText("正在提交，请稍候…");

        submitUploadingDialog = new MaterialAlertDialogBuilder(this)
                .setView(v)
                .setCancelable(false)
                .create();
        submitUploadingDialog.setCanceledOnTouchOutside(false);
        submitUploadingDialog.show();
    }

    private void hideSubmitUploadingDialog() {
        runOnUiThread(() -> {
            if (submitUploadingDialog != null) {
                try {
                    submitUploadingDialog.dismiss();
                } catch (Exception ignored) {}
                submitUploadingDialog = null;
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

    public void Reupload(ReuploadCallback callback) {
        if (uploadManager != null) {
            uploadManager.reupload(callback);
        }
    }

    public void ReuploadSingle(String filename, ReuploadCallback callback) {
        if (uploadManager != null) {
            uploadManager.reuploadSingle(filename, callback);
        }
    }


}
