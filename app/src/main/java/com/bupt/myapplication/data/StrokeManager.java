package com.bupt.myapplication.data;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;

import com.bbb.bpen.model.PointData;
import com.bupt.myapplication.MainActivity;
import com.bupt.myapplication.R;

import java.util.ArrayList;
import java.util.List;


public class StrokeManager {
    // 存储笔迹，注意这里不使用List<PointData>来表示一个笔迹，新建了一个对象来表示
    // 目的是避免混淆、去除无效字段、修改结尾数据点
    private static StrokeManager instance;

    //计数器，记当前页面上有几段笔迹
    private static int cnt = 0;

    private List<List<StrokePoint>> storageStrokeList = new ArrayList<>();

    public static synchronized StrokeManager getInstance() {
        if (instance == null) {
            instance = new StrokeManager();
        }
        return instance;
    }

    // 将List<PointData>改成List<Stroke>
    public void append(List<PointData> stroke){
        List<StrokePoint> stroke_Point_convert = new ArrayList<>();
        for(int i=0; i<stroke.size()-1;i++){
            stroke_Point_convert.add(new StrokePoint(stroke.get(i)));
        }
        StrokePoint lastStrokePoint = stroke_Point_convert.get(stroke_Point_convert.size() - 1);
        lastStrokePoint.setStroke_end(true);
        this.storageStrokeList.add(stroke_Point_convert);
        // 将 UI 相关逻辑移动到 Runnable
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                MainActivity.UndoButtonEnabled();
            }
        };

        // 将 Runnable 发布到主线程
        new Handler(Looper.getMainLooper()).post(runnable);
        cnt++;
    }

    public List<List<StrokePoint>> getALL(){
        return this.storageStrokeList;
    }

    public void clearAll(){
        this.storageStrokeList.clear();
    }

    public static boolean[] getStrokePages(List<List<StrokePoint>> StrokeList) {
        boolean[] IsWrite = new boolean[31];
        for (List<StrokePoint> stroke : StrokeList) {
            if (stroke.size() > 0) {
                StrokePoint firstStrokePoint = stroke.get(0);
                IsWrite[(int) firstStrokePoint.page_id - 54257] = true;
            }
        }
        return IsWrite;
    }

    //TODO：撤销上一段笔迹
    public void withdraw() {
        if (storageStrokeList.isEmpty()) {
            Log.d(String.valueOf(123123), "wrong "+storageStrokeList.size());
            return;
        }
        Log.d(String.valueOf(123123), "size "+storageStrokeList.size());
        storageStrokeList.remove(storageStrokeList.size() - 1);
        Log.d(String.valueOf(123123), "size "+storageStrokeList.size());
        cnt--;
        if(cnt==0){
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    MainActivity.UndoButtonUnEnabled();
                }
            };

            // 将 Runnable 发布到主线程
            new Handler(Looper.getMainLooper()).post(runnable);
        }
    }

    public static void clearCounter() {
        cnt = 0;
    }
}
