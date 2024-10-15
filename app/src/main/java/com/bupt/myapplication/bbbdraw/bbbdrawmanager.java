package com.bupt.myapplication.bbbdraw;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.bbb.bpen.model.PointData;


public class bbbdrawmanager {
    //本地代码库在jnilibs中，但咋打开查看这部分代码。。
    public static native void DrawLine(Canvas canvas, PointData pdata, Paint paint, float page_width,
                                       float page_height, float page_width_forscreen,
                                       float page_height_forscreen, float penwidth);
}
