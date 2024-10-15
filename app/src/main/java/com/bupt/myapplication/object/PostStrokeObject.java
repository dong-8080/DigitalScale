package com.bupt.myapplication.object;

import com.bupt.myapplication.data.StrokePoint;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PostStrokeObject {
    public List<List<StrokePoint>> json;
    public String penId;
    private String tim;
    private String timeStamp;

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getTim() {
        return tim;
    }

    public void setTim(String tim) {
        this.tim = tim;
    }

    public String getPenId() {
        return penId;
    }

    public void setPenId(String penId) {
        this.penId = penId;
    }

    public List<List<StrokePoint>> getJson() {
        return json;
    }

    public void setJson(List<List<StrokePoint>> json) {
        this.json = json;
    }
    //private LocalDateTime createTime;

    //public void setCreateTime(LocalDateTime createTime) {
    //    this.createTime = createTime;
    //}
}
