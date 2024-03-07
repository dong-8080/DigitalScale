package com.bupt.myapplication.object;

import com.bupt.myapplication.data.StrokePoint;

import java.util.List;

public class PostStrokeObject {
    public List<List<StrokePoint>> json;

    public List<List<StrokePoint>> getJson() {
        return json;
    }

    public void setJson(List<List<StrokePoint>> json) {
        this.json = json;
    }
}
