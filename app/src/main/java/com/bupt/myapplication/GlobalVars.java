package com.bupt.myapplication;

import java.io.File;

// TODO: ma
public class GlobalVars {
    private static GlobalVars instance;
    private String globalAddr;
    private boolean opened = false;

    private GlobalVars() {}

    public static synchronized GlobalVars getInstance() {
        if (instance == null) {
            instance = new GlobalVars();
        }
        return instance;
    }

    public String getGlobalAddr() {
        return globalAddr;
    }

    public void setGlobalAddr(String globalAddr) {
        this.globalAddr = globalAddr;
    }
    public boolean getOpened() {
        return opened;
    }
    public void setOpened(boolean flag) {
        this.opened = flag;
    }

}
