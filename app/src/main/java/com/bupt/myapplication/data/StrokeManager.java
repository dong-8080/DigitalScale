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

        cnt++;
    }

    public List<List<StrokePoint>> getALL(){
        return this.storageStrokeList;
    }

    public void clearAll(){
        this.storageStrokeList.clear();
    }

    // TODO: 这里用一个数组来存储，容易引发数组越界，应该用列表，或者从外部文件中使用一个页码和文件的映射
    public static boolean[] getStrokePages(List<List<StrokePoint>> StrokeList) {
        boolean[] IsWrite = new boolean[56];
        for (List<StrokePoint> stroke : StrokeList) {
            if (stroke.size() > 0) {
                StrokePoint firstStrokePoint = stroke.get(0);
                IsWrite[(int) firstStrokePoint.page_id - 55232] = true;
            }
        }
        return IsWrite;
    }

    // 获取当前笔迹中包含的全部ID
    public List<Long> getStrokePageIDs(){
        List<Long> pageIds = new ArrayList<>();
        for(List<StrokePoint> strokes: this.storageStrokeList){
            pageIds.add(strokes.get(0).getPage_id());
        }
        return pageIds;
    }


    public static void clearCounter() {
        cnt = 0;
    }
}
