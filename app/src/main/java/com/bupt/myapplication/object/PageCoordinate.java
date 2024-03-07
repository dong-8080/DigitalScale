package com.bupt.myapplication.object;


import java.util.List;

public class PageCoordinate {
    String paper_type;
    String paper_id;
    List<CenterPoint> center_points;

    public PageCoordinate(String paper_type, String paper_id, List<CenterPoint> center_points) {
        this.paper_type = paper_type;
        this.paper_id = paper_id;
        this.center_points = center_points;
    }

    public String getPaperType() {
        return paper_type;
    }

    public String getPaperId() {
        return paper_id;
    }

    public List<CenterPoint> getCenterPoints() {
        return center_points;
    }

    public void setPaperType(String paper_type) {
        this.paper_type = paper_type;
    }

    public void setPaperId(String paper_id) {
        this.paper_id = paper_id;
    }

    public void setCenterPoints(List<CenterPoint> center_points) {
        this.center_points = center_points;
    }
}
