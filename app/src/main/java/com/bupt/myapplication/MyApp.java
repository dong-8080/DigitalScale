package com.bupt.myapplication;

import android.app.Application;

import java.util.List;

// 上个项目抄来的，忘记有没有叼用了
// 目测是没用的（？。。
public class MyApp extends Application {
    private static MyApp instance;

    // 当前可绑定笔的mac地址，最多只能设定为三个
    private List<String> bindedMacAddresses;

    // 当前正在连接笔的mac地址，为一支确定的笔
    private String curMacAddress;
    // 当前是否有笔连接，如有连接可以跳过进入答题页面绑笔的弹窗
    private String userId;
    private String phone;

    // 选择试卷的id
    private String paperid;

    // 选择试卷的年级
    private String grade;

    public static void setInstance(MyApp instance) {
        MyApp.instance = instance;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }
}
