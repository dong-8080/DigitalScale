package com.bupt.myapplication.object;

import com.bupt.myapplication.data.StrokePoint;

import java.util.List;

public class UploadStrokeObject {
    private String scalesSetRecordId;
    private String penMac;
    private String uploadTime;
    private List<List<StrokePoint>> strokesList;

    public String getScalesSetRecordId() {
        return scalesSetRecordId;
    }

    public void setScalesSetRecordId(String scalesSetRecordId) {
        this.scalesSetRecordId = scalesSetRecordId;
    }

    public String getPenMac() {
        return penMac;
    }

    public void setPenMac(String penMac) {
        this.penMac = penMac;
    }

    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }

    public List<List<StrokePoint>> getStrokesList() {
        return strokesList;
    }

    public void setStrokesList(List<List<StrokePoint>> strokesList) {
        this.strokesList = strokesList;
    }


}