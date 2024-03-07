package com.bupt.myapplication.virtual;

import com.bbb.bpen.model.PointData;
import com.bupt.myapplication.MyApp;
import com.bupt.myapplication.object.CenterPoint;
import com.bupt.myapplication.util.JsonUtil;
import com.bupt.myapplication.object.PageCoordinate;

import java.util.List;

public class VirtualGridFactory {

    public static List<CenterPoint> pagePointList;
    private static CenterPoint relatedPoint;

    private static final int gridToleranceDegree = 30;

    // 可以单例存储这个 指的是底图2114x2999到185-6的缩放
    private static float scalingFactor = 11.810055f;

//    public static VirtualGrid createVirtualGrid
    public void createVirtualGrids(){}

    public void createVirtualGrid(List<PointData> strokes){
        // Step1: 获取书写笔迹的中心点坐标
        float[] stroke_center_point = getStrokeCenter(strokes);
        float stroke_center_x = stroke_center_point[0];
        float stroke_center_y = stroke_center_point[1];

        // Step2：获取整页纸对应的中心点的坐标，对应assets下的json文件
        String pageId = MyApp.getInstance().getPaperid();
//        pagePointList = getPagePointList(pageId);

        // 找到最近的中心点

        // 构造相应的对象，并绑定笔迹

        //
    }

//    public List<CenterPoint> getPagePointList(String pageId){
//        // 读取当前页面的json数据，返回所有的中心点
//        PageCoordinate page_json = JsonUtil.readPageCoordinate(MyApp.getInstance(), pageId);
//        List<CenterPoint> centerPoints =  page_json.getCenterPoints();
//        return centerPoints;
//    }

    private float[] getStrokeCenter(List<PointData> list) {
        float totalX = 0;
        float totalY = 0;
        // 最后一个点通常是(0，0)
        list.remove(list.size() - 1);
        for (PointData p : list) {
            totalX += p.get_x();
            totalY += p.get_y();
        }

        float[] result = new float[2];
        result[0] = totalX / list.size();
        result[1] = totalY / list.size();

        return result;
    }


}
