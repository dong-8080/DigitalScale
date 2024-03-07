package com.bupt.myapplication.data;

import android.util.Log;

import com.bupt.myapplication.MyApp;
import com.bupt.myapplication.object.CenterPoint;
import com.bupt.myapplication.object.PageCoordinate;
import com.bupt.myapplication.util.JsonUtil;

import java.util.HashMap;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;

// 对应页面所有grid的ID
public class PagePointManager {
    private static PagePointManager instance;
    
    public static synchronized PagePointManager getInstance(){
        if (instance==null){
            instance = new PagePointManager();
        }
        return instance;
    }

    // 按照pageID存储，比较方便
    private Map<String, List<CenterPoint>> pagePointMapping;

    public Map<String, List<CenterPoint>> getPagePointList(){
        Map<String, List<CenterPoint>> pagePointMapping = new HashMap<>();
        // 读取当前页面的json数据，返回所有的中心点
        List<PageCoordinate> page_json = JsonUtil.readPageCoordinate(MyApp.getInstance());
        for(PageCoordinate pageCoordinate:page_json){
            pagePointMapping.put(pageCoordinate.getPaperId(), pageCoordinate.getCenterPoints());
        }
        return pagePointMapping;
    }

    public List<CenterPoint> getCenterPoints(String pageID){
        if (pagePointMapping==null || pagePointMapping.isEmpty()){
            pagePointMapping = this.getPagePointList();
            Log.e("TAG", "IN IF");
        }
        return pagePointMapping.get(pageID);
    }

}
