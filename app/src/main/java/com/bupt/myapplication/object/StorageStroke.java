package com.bupt.myapplication.object;

import android.graphics.Point;

import com.bbb.bpen.model.PointData;

import java.util.ArrayList;
import java.util.List;

// 存储下来的数据点，需要上传到后端，以及进行进一步的分析
public class StorageStroke {
    private String pageID;
    private String gridID;

    // 笔迹列表，每一个新的笔迹都会添加进来
    private List<List<PointData>> strokes;

    public StorageStroke(){
        this.pageID = "";
        this.gridID = "";
        this.strokes = new ArrayList<>();
    }

    public StorageStroke(String pageID, String gridID){
        this.pageID  = pageID;
        this.gridID = gridID;
        this.strokes = new ArrayList<>();
    }

    public StorageStroke(String pageID, String gridID, List<List<PointData>> strokes){
        this.pageID  = pageID;
        this.gridID = gridID;
        this.strokes = strokes;
    }

    public String getPageID() {
        return pageID;
    }

    public String getGridID() {
        return gridID;
    }

    public void appendStroke(List<PointData> stroke){
        this.strokes.add(stroke);
    }
}
