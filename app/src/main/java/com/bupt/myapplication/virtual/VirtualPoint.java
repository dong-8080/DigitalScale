package com.bupt.myapplication.virtual;

// 对应纸上的中心点，与绘制点无关
public class VirtualPoint {

    // type: button, grid
    public String type;
    // id: 1-1, 1-2
    public String id;

    public String event;

    // 中心点的属性
    public int x;
    public int y;
    public int width;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public VirtualPoint(String type, String id, String event, int x, int y, int width) {
        this.type = type;
        this.id = id;
        this.event = event;
        this.x = x;
        this.y = y;
        this.width = width;

    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getEvent() {
        return event;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

}
