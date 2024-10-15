package com.bupt.myapplication.data;

import android.util.Log;

import com.bbb.bpen.model.PointData;
import com.bupt.myapplication.object.CenterPoint;
import com.bupt.myapplication.object.StorageStroke;

import java.util.ArrayList;
import java.util.List;

// 建立新的笔迹和控件ID的映射关系
// 每一个新的笔迹都会判定是否是有效笔迹，即填写在输入框中
// 如果是有效笔迹，则添加进存储的笔迹列表中去，允许重复填入
public class StorageStrokeManager {

    private static float scalingFactor = 11.810055f;
    private static StorageStrokeManager instance;
    // 以pageID-gridID-List<List<PointData>>存储的有效笔迹数据
    // 老母猪xxx一套又一套，就是得套起来不然太乱了

    //唯一实例化
    private List<StorageStroke> storageStrokeList = new ArrayList<>();

    public static synchronized StorageStrokeManager getInstance() {
        if (instance == null) {
            instance = new StorageStrokeManager();
        }
        return instance;
    }

    // 读取对应页面的数据点，并找到对应的gridID
    //就是判断一段笔迹属于哪个格子
    private String generateGridID(String pageID, List<PointData> stroke){
        // 获取笔迹的中心点坐标
        float center_x = 0;
        float center_y = 0;
        for(PointData pd: stroke){
            center_x+=pd.get_x();
            center_y+=pd.get_y();
        }
        center_x = center_x/stroke.size();
        center_y = center_y/stroke.size();

        // 获取纸面上所有的点
        List<CenterPoint> centerPoints = PagePointManager.getInstance().getCenterPoints(pageID);

        // 获取和当前笔迹最相关的纸上的点
        CenterPoint relatedPoint = getRelatedPoint(center_x, center_y,centerPoints);

        int distance = calculateChebyshevDistance(center_x*scalingFactor, center_y*scalingFactor, relatedPoint.getX(), relatedPoint.getY());

        Log.e("Storage", "related point:"+relatedPoint.getX()+" "+relatedPoint.getY());
        Log.e("Storage", "strokes center:" + (int)center_x*scalingFactor+" "+ (int)center_y*scalingFactor);

        // 距离判定是否笔迹在识别框内, 即到笔迹中心点到纸最近中心点的距离小于一般宽度加上误差距离
        // 若在判定框内返回ID，否则表示无效返回null
        int judge_distance = Integer.parseInt(relatedPoint.getWidth())/2 + 20;
        if (distance<judge_distance){
            return relatedPoint.getId();
        }else {
            return null;
        }
    }

    //找到与中心点距离最近的格子
    public CenterPoint getRelatedPoint(float x, float y, List<CenterPoint> centerPoints){
        if (centerPoints==null||centerPoints.isEmpty()){
            return null;
        }

        // 转换单位
        float x_scale = x * scalingFactor;
        float y_scale = y * scalingFactor;

        CenterPoint nearestPoint  = centerPoints.get(0);
        double minDistance = calculateChebyshevDistance(x_scale, y_scale,nearestPoint.getX(),nearestPoint.getY());

        for (CenterPoint cp:centerPoints){
            double distance = calculateChebyshevDistance(x_scale, y_scale, cp.getX(), cp.getY());
            if (distance<minDistance){
                minDistance = distance;
                nearestPoint = cp;
            }
        }
        return nearestPoint;

    }

    // 最主要方法
    public void appendStroke(List<PointData> stroke){
        String pageID = String.valueOf(stroke.get(0).getPage_id());
        String gridID = generateGridID(pageID, stroke);

        Log.e("Storage", "pageID "+pageID+"; gridID "+gridID);

        // 若笔迹无效不添加
        if (gridID!=null) {
            // 若storageStroke空新创建一个，否则代表已经存在直接添加
            StorageStroke storageStroke = getStorageStroke(pageID, gridID);
            if (storageStroke == null) {
                storageStroke = new StorageStroke(pageID, gridID);
            }
            storageStroke.appendStroke(stroke);
            this.storageStrokeList.add(storageStroke);
        }
    }

    // 从整个列表中获取对应两个ID的storageStroke对象，并从列表中删除；否则返回空, 代表没有相应的对象
    private StorageStroke getStorageStroke(String pageID, String gridID){
        for(StorageStroke storageStroke: storageStrokeList){
            if (gridID.equals(storageStroke.getGridID()) && pageID.equals(storageStroke.getPageID())){
                storageStrokeList.remove(storageStroke);
                return storageStroke;
            }
        }
        return null;
    }

    public List<StorageStroke> getStorageStrokeList(){
        return this.storageStrokeList;
    }

    // 计算切比雪夫距离
    public static int calculateChebyshevDistance(float x1, float y1, String x2, String y2) {
        int x1_scale = (int) x1;
        int x2_scale = Integer.parseInt(x2);
        int y1_scale = (int) y1;
        int y2_scale = Integer.parseInt(y2);
        int deltaX = Math.abs(x2_scale - x1_scale);
        int deltaY = Math.abs(y2_scale - y1_scale);
        return Math.max(deltaX, deltaY);
    }
}
