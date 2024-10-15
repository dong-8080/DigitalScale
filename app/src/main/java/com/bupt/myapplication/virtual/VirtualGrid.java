package com.bupt.myapplication.virtual;

import android.util.Log;

import com.bbb.bpen.model.PointData;

import java.util.ArrayList;
import java.util.List;

// 抄的之前项目里的参照
public class VirtualGrid {
    private int center_x;
    private int center_y;

    private int width;
    private List<PointData> relatedPoints;
    private int tolerance;

    private float scalingFactor;

    private String id;

    public VirtualGrid(int center_x, int center_y, int width, float scalingFactor, int tolerance, String id) {
        this.center_x = center_x;
        this.center_y = center_y;
        this.width = width;
        this.scalingFactor = scalingFactor;
        this.tolerance = tolerance;
        this.relatedPoints = new ArrayList<>();
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    // 绑定田字格中所有的笔迹
    // 实现为对于传入的所有points, 判定其距离小于宽度加误差度，则在方格内，从源列表中将该数据删除，添加到绑定数据中
    public void bindStrokes(List<PointData> pointDataList, String pageID) {
        float maxD = this.width / 2 + this.tolerance;
        long pageIdNumbered = Long.parseLong(pageID);
        for (PointData pdata : pointDataList) {
            if (pageIdNumbered == pdata.getPage_id()) {
                int distance = calculateChebyshevDistance(pdata);
                if (distance < maxD) {
                    this.relatedPoints.add(pdata);

                }
            }
        }

    }

    public List<PointData> getRelatedPoints () {
        return this.relatedPoints;
    }

    private static String TAG = "EventTriggerReceiver";

    // 先将PointData的坐标转化为dp单位，再计算棋盘距离
    // pdata中的x、y单位是mm，需要转换成对应屏幕的dp尺寸，使用scalingFactor进行缩放
    private int calculateChebyshevDistance (PointData pointData){
        int pdata_x = (int) (pointData.get_x() * this.scalingFactor);
        int pdata_y = (int) (pointData.get_y() * this.scalingFactor);

        int deltaX = Math.abs(pdata_x - this.center_x);
        int deltaY = Math.abs(pdata_y - this.center_y);
        Log.e(TAG, "pdata:" + pdata_x + " " + pdata_y + "center_x" + this.center_x + " " + this.center_y);
        return Math.max(deltaX, deltaY);
    }
}
