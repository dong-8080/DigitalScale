package com.bupt.myapplication.object;

import java.util.List;

public class PageCoordinateList {
    private List<PageCoordinate> pages;

    public List<PageCoordinate> getPages() {
        return pages;
    }

    public void setPages(List<PageCoordinate> pages) {
        this.pages = pages;
    }

    public PageCoordinateList(List<PageCoordinate> pages) {
        this.pages = pages;
    }
}
