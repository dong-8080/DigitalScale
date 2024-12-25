package com.bupt.myapplication.object;

import com.bupt.myapplication.data.StrokePoint;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


// todo: 像后端提交数据，改名字
public class PostStrokeObject {
    public List<List<StrokePoint>> json;
    public String penId;
    private String time;
    private String timeStamp;

    // todo: 测试使用
    private String patientId;

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getTim() {
        return time;
    }

    public void setTim(String time) {
        this.time = time;
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
