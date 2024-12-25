package com.bupt.myapplication;

import android.app.Application;

import java.util.List;

// 上个项目抄来的，忘记有没有叼用了
// TODO：某些地方需要调用，这里应该主要记录纸和笔的ID，和另一个GlobalVars有些冲突，把两个合并一下
public class MyApp extends Application {
    private static MyApp instance;

    // 当前可绑定笔的mac地址，最多只能设定为三个
    private List<String> bindedMacAddresses;

    // 当前正在连接笔的mac地址，为一支确定的笔
    private String curMacAddress;
    // 当前是否有笔连接，如有连接可以跳过进入答题页面绑笔的弹窗
    private String userId;
    // 选择试卷的id
    private String paperid;

    // 被试的ID
    private String participantID;

    // 量表名称, 区分不同的版本，比如v1，v2，jingrixing
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

    public List<String> getBindedMacAddresses() {
        return bindedMacAddresses;
    }

    public void setBindedMacAddresses(List<String> bindedMacAddresses) {
        this.bindedMacAddresses = bindedMacAddresses;
    }

    public String getCurMacAddress() {
        return curMacAddress;
    }

    public void setCurMacAddress(String curMacAddress) {
        this.curMacAddress = curMacAddress;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return this.userId;
    }




    public void removeBindedPenMacAddress(String macAddress){
        this.bindedMacAddresses.remove(macAddress);
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
