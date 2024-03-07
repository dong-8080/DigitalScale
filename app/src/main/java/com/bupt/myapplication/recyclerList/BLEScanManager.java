package com.bupt.myapplication.recyclerList;

import android.util.Log;

import com.bupt.myapplication.data.PagePointManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

// 数据管理类，用于接受新的扫描地址
public class BLEScanManager {

    private static BLEScanManager instance;
    private List<String> dataList;
    private List<BLEScanObserver> observers;

    private BLEScanManager(){
        this.dataList = new ArrayList<>();
        this.observers = new ArrayList<>();
    }

    public static synchronized  BLEScanManager getInstance(){
        if (instance==null) {
            instance = new BLEScanManager();
        }
        return instance;
    }

    public void addObserver(BLEScanObserver observer){
        observers.add(observer);
    }

    public void removeObserver(BLEScanObserver observer){
        observers.remove(observer);
    }

    // 需要保证不重复的添加
    public void addData(String macAddress){
        if (!dataList.contains(macAddress)) {
            dataList.add(macAddress);
            notifyObservers();
        }
    }

    private void notifyObservers(){
        Log.e("adddata", "notify changed1  "+dataList.size());
        Log.e("adddata", "observer size "+observers.size());
        for (BLEScanObserver observer:observers){
            observer.onBLEScanChanged();
        }
    }

    public List<String> getBLEScanList(){
        return dataList;
    }

}
