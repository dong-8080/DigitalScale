package com.bupt.myapplication.view;


import android.app.Activity;
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


import java.util.List;

// 自定义画图控件
// 映射出所有的笔迹，无需改动
public class DrawingView extends View {

    Paint mDrawpaint;

    PointData lastPoint;
    Canvas mCanvas;
    Bitmap bitmap;
    Bitmap background;
    Context context;

    float page_width;
    float page_height;

    public float page_width_forscreen;
    public float page_height_forscreen;

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

        mCanvas = new Canvas();

        String pageID = MyApp.getInstance().getPaperid();
        Log.e("pageid", "pageid:"+pageID);

        // 重新定义纸张类型,根据page的大小,再减去两边的出血线条各3mm
        page_width = 185.f - 6;
        page_height = 260.f - 6;

        background = get_drawing_background(pageID);

        // 获取屏幕宽高
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;

        // 获取状态栏高度
        int statusBarHeight = 0;
        int resId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resId);
        }

        // 获取 toolbar 高度（尝试获取实际高度，否则默认 56dp）
        int toolbarHeight = 0;
        View toolbar = ((Activity) context).findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbarHeight = toolbar.getHeight();
            if (toolbarHeight == 0) {
                toolbarHeight = (int) (56 * getResources().getDisplayMetrics().density); // fallback
            }
        } else {
            toolbarHeight = (int) (56 * getResources().getDisplayMetrics().density); // fallback
        }

        // 计算可用高度（不被遮挡的区域）
        int availableHeight = screenHeight - statusBarHeight - toolbarHeight;

        // 计算缩放因子，按“屏幕可用宽高”和“图片原始尺寸”进行比例计算
        float scaleW = (float) screenWidth / background.getWidth();
        float scaleH = (float) availableHeight / background.getHeight();

        // 按照较小比例等比缩放，确保图片完整显示
        float scale = Math.min(scaleW, scaleH);

        // 计算缩放后的宽高
        int newWidth = (int) (background.getWidth() * scale);
        int newHeight = (int) (background.getHeight() * scale);

        bitmap = Bitmap.createScaledBitmap(background, newWidth, newHeight, true);
        mCanvas = new Canvas(bitmap);

        // 存储缩放后尺寸用于笔迹转换
        page_width_forscreen = newWidth;
        page_height_forscreen = newHeight;

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

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // View 大小改变时，重新创建Bitmap并关联Canvas
        initDraw();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (bitmap != null) {
            int canvasWidth = canvas.getWidth();
            int canvasHeight = canvas.getHeight();

            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();

            // 计算居中偏移量
            int offsetX = (canvasWidth - bitmapWidth) / 2;
            int offsetY = (canvasHeight - bitmapHeight) / 2;

            // 在画布上居中绘制背景图
            canvas.drawBitmap(bitmap, offsetX, offsetY, null);
        }
        invalidate();

    }

    // 换页, 保存好当前数据, 重新初始化,换底图
    public void notifyChangeBackGround() {
        Log.e("BACKGROUND", "notifyChangeBackGround");
        initDraw();
    }
}