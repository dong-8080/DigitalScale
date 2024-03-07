package com.bupt.myapplication.data;

import com.bbb.bpen.model.PointData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

// 单例模式，存储蓝牙笔生成的point对象
// 所有对点的操作都基于此进行
public class PointManager {
    // 实例对象，以及绘制点和屏幕未提交的点
    // TODO: pointScreenList保存页面上所有的点，不删除

    // 2023年12月2日备注
    // 大概pointDrawList是需要绘制的数据点，基本是清零的；pointScreenList是页面上所有的点，可能是向后端提交才清零
    private static PointManager instance;
    private List<PointData> pointDrawList;
    private List<PointData> pointScreenList;

    private PointManager() {
        this.pointDrawList = new CopyOnWriteArrayList<>();
        this.pointScreenList = new CopyOnWriteArrayList<>();
    }

    public static synchronized PointManager getInstance() {
        if (instance == null) {
            instance = new PointManager();
        }
        return instance;
    }

    public List<PointData> getPointDrawList() {
        return pointDrawList;
    }


    public List<PointData> getAllPointScreenList() {
        return pointScreenList;
    }

    public List<PointData> getCurrentPointScreenList(String pageId){
        long currentPageID = Long.parseLong(pageId);
        List<PointData> currentPointScreenList = new ArrayList<>();
        for(int i=0;i<pointScreenList.size();i++){
            PointData pointData = pointScreenList.get(i);
            if (currentPageID == pointData.getPage_id()){
                currentPointScreenList.add(pointData);
            }
        }
        return currentPointScreenList;
    }

    public void addPointToList(List<PointData> pointDataList) {
        pointDrawList.addAll(pointDataList);
        pointScreenList.addAll(pointDataList);
    }

    // 从服务器获取的数据，用于绘制历史记录笔迹
    public void addPointToOnlyDrawList(List<PointData> pointDataList){
        pointDrawList.addAll(pointDataList);
    }

    public void addPointToOnlyScreenList(List<PointData> pointDataList){
        pointScreenList.addAll(pointDataList);
    }

    // 是否包含绘图的数据点
    public boolean isContainDrawPoint() {
        return pointDrawList.size() > 0;
    }

    public boolean isContainCurrentDrawPoint(String paperId) {
        long currentPageID = Long.parseLong(paperId);
        List<PointData> currentPointDrawList =
                pointDrawList.stream()
                        .filter(point -> point.getPage_id() == currentPageID)
                        .collect(Collectors.toList());
        return currentPointDrawList.size()>0;
    }

    // 笔迹书写是否结束
    public boolean isStrokeFinish() {
        return pointScreenList.get(-1).isStroke_end();
    }

    // 返回最后一段轨迹
    public List<PointData> getLatestStroke() {
        int startIndex = 0;
        int endIndex = 0;
        for (int i = pointScreenList.size()-1; i >= 0; i--) {
            if (pointScreenList.get(i).isStroke_end()) {
                endIndex = i;
                break;
            }
        }
        for (int j = endIndex; j >= 0; j--) {
            if (pointScreenList.get(j).isStroke_start()) {
                startIndex = j;
                break;
            }
        }
        // 新创建List，不然会修改原先的对象
        List<PointData> lastStroke = new ArrayList<>(
                pointScreenList.subList(startIndex, endIndex + 1)
        );
        return lastStroke;
    }

    public void clearDrawList(){
        this.pointDrawList.clear();
    }

    public void clear(){
        this.pointDrawList.clear();
        this.pointScreenList.clear();
    }
}
