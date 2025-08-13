package com.bupt.myapplication;

import android.app.Application;

import java.util.List;
import java.util.TreeMap;

// 记录一些必要的全局信息
public class MyApp extends Application {
    private static MyApp instance;

    // 当前正在连接笔的mac地址，为一支确定的笔
    // 连接后会重置，断开后仍保留此此段，通过isConnected来判定是否连接笔
    // 这样避免出现提交时，先把笔断开，再上传倒是没有macid的情况
    private String curMacAddress=null;

    private Boolean isConnected=false;

    // 选择试卷的id
    private String paperid;

    // 被试的ID
    private String participantID;

    private String scale_name;

    public static void setInstance(MyApp instance) {
        MyApp.instance = instance;
    }

    public static MyApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public String getCurMacAddress() {
        return curMacAddress;
    }

    public void setCurMacAddress(String curMacAddress) {
        this.curMacAddress = curMacAddress;
    }

    // 判断当前是否连接好蓝牙笔
    public boolean isBLEConnected(){
        return this.isConnected;
    }

    public void BLEDidConnected(){
        this.isConnected = true;
    }

    public void BLEDisConnected(){
        this.isConnected = false;
    }

    public String getPaperid() {
        return paperid;
    }

    public void setPaperid(String paperid) {
        this.paperid = paperid;
    }

    public String getParticipantID() {
        return participantID;
    }

    public void setParticipantID(String participantID) {
        this.participantID = participantID;
    }

    public void setScale_name(String name){
        this.scale_name = name;
    }

    public String getScale_name(){
        return this.scale_name;
    }
}
