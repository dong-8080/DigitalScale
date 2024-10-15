package com.bupt.myapplication.view;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.bbb.bpen.model.PointData;
import com.bupt.myapplication.MyApp;
import com.bupt.myapplication.R;
import com.bupt.myapplication.data.PointManager;


import java.io.Serializable;
import java.util.List;
import java.util.Random;

// 自定义画图控件
public class DrawingView extends View implements Serializable {

    Paint mDrawpaint;

    // 测试后删除
    Paint mTextpaint;

    PointData lastPoint;
    Canvas mCanvas;
    Bitmap bitmap;
    Bitmap background;
    Context context;

    float page_width;
    float page_height;

    public float page_width_forscreen;
    public float page_height_forscreen;

    private String text = "";

    public DrawingView(Context context) {
        super(context);
        this.context = context;
    }

    public DrawingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public DrawingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.e("Page", "background measure width:" + getMeasuredWidth() + " height" + getMeasuredHeight());
    }

    // 页面切换
    private Bitmap get_drawing_background(String pageID) {
        Log.e("PAGE", "pageID " + pageID + " ");
        if (pageID != null) {
            BackgroundLoader loader = new BackgroundLoader(context, pageID,this);
            Log.e("BACKGROUND", "load cached image");
            // 简化一下从资源文件加载
            return loader.load_local();
        }

        // 加载默认的空白页面
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inDensity = DisplayMetrics.DENSITY_DEFAULT;
        options.inTargetDensity = DisplayMetrics.DENSITY_DEFAULT;
        Log.e("BACKGROUND", "load empty image");

        background = BitmapFactory.decodeResource(getResources(), R.drawable.drawing_background_init, options);
        return background;
    }

    public void initDraw() {
        //初始化画笔
        mDrawpaint = new Paint(Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
        mDrawpaint.setStyle(Paint.Style.STROKE);
        mDrawpaint.setStrokeWidth(5);
        mDrawpaint.setAntiAlias(true); // 抗锯齿
        mDrawpaint.setStrokeJoin(Paint.Join.ROUND);
        mDrawpaint.setStrokeCap(Paint.Cap.ROUND);

        // 测试后删除
        mTextpaint = new Paint(Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
        mTextpaint.setStrokeWidth(5);
        mTextpaint.setTextSize(30);

        mCanvas = new Canvas();

        // 获取纸类型 测试样例为正度16K
//        Paper paper = new Paper(16);
//        page_width = paper.getWidth();
//        page_height = paper.getHeight();

        String pageID = MyApp.getInstance().getPaperid();
        Log.e("pageid", "pageid:"+pageID);

        // 重新定义纸张类型,根据page的大小,再减去两边的出血线条各3mm
        page_width = 185.f - 6;
        page_height = 260.f - 6;

//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inScaled = false;
//        options.inDensity = DisplayMetrics.DENSITY_DEFAULT;
//        options.inTargetDensity = DisplayMetrics.DENSITY_DEFAULT;

        background = get_drawing_background(pageID);

        // 此处screenWidth=1200
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        Log.e("TAG", "screenWidth:"+screenWidth);

        // background width and height
        int newWidth = screenWidth;
        int newHeight = (int) (background.getHeight() * ((float) newWidth / background.getWidth()));

        bitmap = Bitmap.createScaledBitmap(background, newWidth, newHeight, false);
        mCanvas = new Canvas(bitmap);

        page_width_forscreen = newWidth;
        page_height_forscreen = newHeight;

//        float factor = 1800/2114;
//
//        // TODO: 绘制辅助框
//        if (pageID!=null) {
//            List<CenterPoint> centerPoints = PagePointManager.getInstance().getCenterPoints(pageID);
//            for (CenterPoint centerPoint : centerPoints) {
//                float x = Float.parseFloat(centerPoint.getX());
//                float y = Float.parseFloat(centerPoint.getY());
//                float width = Float.parseFloat(centerPoint.getWidth());
//
//                Log.e("TAG", "center x:" + x + "center y"+ y + "screenWidth"+ screenWidth);
//
//                float left = (x-width/2)*factor;
//                float top = (y-width/2)*factor;
//                float right = (x+width/2)*factor;
//                float bottom = (y-width/2)*factor;
//
//
//                mCanvas.drawRect(left, top,right, bottom, mDrawpaint);
//            }
//        }


        // 画此页面已存在的历史记录
        if (pageID != null) {
            drawHistoryStroke(pageID);
        }
    }

    //TODO：撤回一段笔迹之后要重新画当前页面所有

    // 切换页面后, 再次初始化, 将之前保存的数据绘制出来
    void drawHistoryStroke(String pageID) {
        List<PointData> pointScreenList = PointManager.getInstance().getCurrentPointScreenList(pageID);
        PointManager.getInstance().addPointToOnlyDrawList(pointScreenList);

        Log.e("DRAWERROR", "History pointScreenList:" + pointScreenList.size());
        notifyDraw();
    }

    public void notifyDraw() {

        while (PointManager.getInstance().isContainDrawPoint()) {
            List<PointData> pointList = PointManager.getInstance().getPointDrawList();

            while (pointList.size() > 0) {
                PointData pdata = pointList.get(0);

                if (pdata == null || pdata.isStroke_end()) {
                    Log.d("onDraw ", "drawData 笔画结束 ");
                    try {
                        pointList.remove(0);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                } else {
                    // 正常绘制，这里应该判断下纸是是否匹配的
                    // Demo中只需要在正16K纸上进行测试即可
                    // 前辈写的代码没一句是废的
                    try {
                        pointList.remove(0);
                    } catch (Exception e) {
                        Log.e("TAG", "IndexOutOfBoundsException");
                    }


                    if (lastPoint == null || pdata.isStroke_start() == true) {
                        lastPoint = pdata;
                    }

                    float xStart = page_width_forscreen * (pdata.get_x()) / page_width;
                    float yStart = page_height_forscreen * (pdata.get_y()) / page_height;
                    float xEnd = page_width_forscreen * (lastPoint.get_x()) / page_width;
                    float yEnd = page_height_forscreen * (lastPoint.get_y()) / page_height;

                    Log.e("TAG", "factor" + page_height_forscreen/page_width);
                    Log.e("TAG", "xStart" + xStart+" ");

                    lastPoint = pdata;

                    // 设定笔粗细
                    float PENWIDTH_MIN = 0.1f;
                    float StrokeWidthFactor = 2f;
                    float pwidth = 0;
                    float s_width = PENWIDTH_MIN + StrokeWidthFactor * pdata.getlinewidth() + pwidth;

                    mDrawpaint.setStrokeWidth(s_width);
                    mDrawpaint.setColor(Color.BLACK);

                    mCanvas.drawLine(xStart, yStart, xEnd, yEnd, mDrawpaint);

                }
            }
        }
    }
    //擦除工具
    public void notifyErase() {
        List<PointData> tmpList = PointManager.getInstance().getPointEraseList();
        Log.d("success", "tmpList"+tmpList);
        while (tmpList.size() > 0) {
            PointData pdata = tmpList.get(0);

            if (pdata == null || pdata.isStroke_end()) {
                Log.d("onDraw ", "drawData 笔画结束 ");
                try {
                    tmpList.remove(0);
                }catch (Exception e){
                    e.printStackTrace();
                }
            } else {
                // 正常擦除，这里应该判断下纸是是否匹配的
                // Demo中只需要在正16K纸上进行测试即可
                // 前辈写的代码没一句是废的
                try {
                    tmpList.remove(0);
                } catch (Exception e) {
                    Log.e("TAG", "IndexOutOfBoundsException");
                }


                if (lastPoint == null || pdata.isStroke_start() == true) {
                    lastPoint = pdata;
                }

                float xStart = page_width_forscreen * (pdata.get_x()) / page_width;
                float yStart = page_height_forscreen * (pdata.get_y()) / page_height;
                float xEnd = page_width_forscreen * (lastPoint.get_x()) / page_width;
                float yEnd = page_height_forscreen * (lastPoint.get_y()) / page_height;

                Log.e("TAG", "factor" + page_height_forscreen/page_width);
                Log.e("TAG", "xStart" + xStart+" ");

                lastPoint = pdata;

                // 设定橡皮擦大小
                float ERASER_WIDTH_MIN = 10f;
                float EraserWidthFactor = 2f;
                float pwidth = 0;
                float s_width = ERASER_WIDTH_MIN + EraserWidthFactor * pdata.getlinewidth() + pwidth;

                // 设置橡皮擦颜色，这里设置为白色，可以根据需求修改
                mDrawpaint.setColor(Color.WHITE);
                mDrawpaint.setStrokeWidth(s_width);

                // 擦除路径
                mCanvas.drawLine(xStart, yStart, xEnd, yEnd, mDrawpaint);

            }
        }
        invalidate();
        PointManager.getInstance().EraseListClear();
    }

    // TODO: 演示使用模拟提交，使得字迹变化, 测试方法
    public void notifyReDraw(List<PointData> pointList) {

        while (pointList.size() > 0) {
            PointData pdata = pointList.get(0);
            if (pdata != null) {
            }

            if (pdata == null || pdata.isStroke_end()) {
                Log.d("onDraw ", "drawData 笔画结束 ");
                pointList.remove(0);
            } else {
                // 正常绘制，这里应该判断下纸是是否匹配的
                // Demo中只需要在正16K纸上进行测试即可
                pointList.remove(0);

                if (lastPoint == null || pdata.isStroke_start() == true) {
                    lastPoint = pdata;
                }

                float xStart = page_width_forscreen * (pdata.get_x()) / page_width;
                float yStart = page_height_forscreen * (pdata.get_y()) / page_height;
                float xEnd = page_width_forscreen * (lastPoint.get_x()) / page_width;
                float yEnd = page_height_forscreen * (lastPoint.get_y()) / page_height;

                lastPoint = pdata;

                // 设定笔粗细
                float PENWIDTH_MIN = 0.1f;
                float StrokeWidthFactor = 2f;
                float pwidth = 0;
                float s_width = PENWIDTH_MIN + StrokeWidthFactor * pdata.getlinewidth() + pwidth;

                mDrawpaint.setStrokeWidth(s_width * 2);

                // 随机颜色标识提交的笔迹
                int[] colors = {Color.GRAY, Color.BLUE, Color.MAGENTA, Color.RED, Color.YELLOW, Color.CYAN};
                Random random = new Random();
                int randomIndex = random.nextInt(colors.length);
                int randomColor = colors[randomIndex];
                mDrawpaint.setColor(randomColor);

                mCanvas.drawLine(xStart, yStart, xEnd, yEnd, mDrawpaint);

            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // View 大小改变时，重新创建Bitmap并关联Canvas
        initDraw();
    }

    public void notifyTextChanged(String text) {
        this.text = text;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bitmap, 0, 0, null);
        // 测试后删除
        canvas.drawText(text, 80, 80, mTextpaint);
        invalidate();

    }

    // 换页, 保存好当前数据, 重新初始化,换底图
    public void notifyChangeBackGround() {
        Log.e("BACKGROUND", "notifyChangeBackGround");
        initDraw();
    }
}