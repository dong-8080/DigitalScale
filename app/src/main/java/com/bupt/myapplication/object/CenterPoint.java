package com.bupt.myapplication.object;

// 纸张上数据点的对应类型
public class CenterPoint {
    String id;
    String type;
    String event;
    String x;
    String y;
    String width;
    String maximum;
    String description;

    public CenterPoint(String id, String type, String event, String x, String y, String width, String maximum, String description) {
        this.id = id;
        this.type = type;
        this.event = event;
        this.x = x;
        this.y = y;
        this.width = width;
        this.maximum = maximum;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getEvent() {
        return event;
    }

    public String getX() {
        return x;
    }

    public String getY() {
        return y;
    }

    public String getWidth() {
        return width;
    }

    public String getMaximum() {
        return maximum;
    }

    public String getDescription() {
        return description;
    }
}