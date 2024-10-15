package com.bupt.myapplication.data;

import com.bbb.bpen.model.PointData;

public class StrokePoint {
    long page_id;
    float x;
    float y;
    float linewidth;
    boolean stroke_start;
    boolean stroke_end;

    double time_stamp;

    public StrokePoint(PointData pdata){
        this.page_id = pdata.getPage_id();
        this.x = pdata.get_x();
        this.y = pdata.get_y();
        this.linewidth = pdata.getlinewidth();
        this.stroke_start = pdata.isStroke_start();
        this.stroke_end = pdata.isStroke_end();
        this.time_stamp = pdata.getTime_stamp();
    }

    public long getPage_id() {
        return page_id;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getLinewidth() {
        return linewidth;
    }

    public boolean isStroke_start() {
        return stroke_start;
    }

    public boolean isStroke_end() {
        return stroke_end;
    }

    public double getTime_stamp() {
        return time_stamp;
    }

    public void setPage_id(long page_id) {
        this.page_id = page_id;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setLinewidth(float linewidth) {
        this.linewidth = linewidth;
    }

    public void setStroke_start(boolean stroke_start) {
        this.stroke_start = stroke_start;
    }

    public void setStroke_end(boolean stroke_end) {
        this.stroke_end = stroke_end;
    }

    public void setTime_stamp(double time_stamp) {
        this.time_stamp = time_stamp;
    }
}
