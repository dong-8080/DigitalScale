package com.bupt.myapplication.bbbdraw;

import android.bluetooth.BluetoothGatt;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.bbb.bpen.command.BiBiCommand;
import com.bbb.bpen.delegate.BlueDelegate;
import com.bbb.bpen.model.Pen;
import com.bbb.bpen.model.PointData;
import com.bupt.myapplication.MyApp;
import com.bupt.myapplication.data.PointManager;
import com.bupt.myapplication.data.StrokeManager;
import com.bupt.myapplication.dialog.MainDialogFragment;
import com.bupt.myapplication.recyclerList.BLEScanManager;
import com.bupt.myapplication.util.StringUtils;
import com.bupt.myapplication.view.DrawingView;

import java.util.List;


public class BlueDelegateImpl implements BlueDelegate {
    public String TAG = "BLE";
    private DrawingView dw;
    private BLEScanManager bleScanManager;

    private MainDialogFragment myDialogFragment = null;

    private String connectedAddress = "";

    public BlueDelegateImpl(DrawingView dw, MainDialogFragment var0) {
        this.dw = dw;
        this.bleScanManager = BLEScanManager.getInstance();
        this.myDialogFragment = var0;
    }

    @Override
    public void didDiscoverWithPen(Pen device, int rssi) {
        // 开启扫描后会调用此方法
        Log.e("discover", "discover Pen " + device.getAddress() + " rssi" + rssi);
        bleScanManager.addData(device.getAddress());
        Log.e("BIBIPENSCAN", "call did discover with pen");
    }

    @Override
    public void didConnectFail(BluetoothGatt gatt, int status, int newState) {
        Log.e(TAG, "didConnectFail, status:" + status + " newState:" + newState);
    }

    // 连接时的状态主要由以下两个回调函数判定
    // 接收到消息时得全局存储消息，以确定蓝牙笔是否连接
    @Override
    public void didDisconnect(Pen device, int status, int newState) {
        MyApp.getInstance().BLEDisConnected();
        Log.e(TAG, "didDisconnect, status:" + status + " newState:" + newState);
    }

    @Override
    public void didConnect(Pen device, int status, int newState) {

        Log.e(TAG, "didConnect, status:" + status + " newState:" + newState);
        Log.e(TAG, "didConnect, device mac:" + device.getAddress());
        this.connectedAddress = device.getAddress();
        MyApp.getInstance().setCurMacAddress(device.getAddress());
        MyApp.getInstance().BLEDidConnected();

        try {
            BiBiCommand.stopscan(dw.getContext());
        }catch (Exception e){
            e.printStackTrace();
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MyApp.getInstance(), "连接蓝牙笔成功", Toast.LENGTH_LONG).show();
                myDialogFragment.changeColor(connectedAddress);

            }
        });
    }

    @Override
    public void notifyBattery(int battery) {}


    // 绘图重要函数
    @Override
    public void notifyRealTimePointData(List<PointData> pointDrawArray, int i) {

        PointManager.getInstance().addPointToList(pointDrawArray);

        // 仅从最后一个点触发点击相关的广播事件
        // 第一个点触发页面切换事件
        if (pointDrawArray != null && pointDrawArray.size() > 0) {
            PointData startPoint = pointDrawArray.get(0);
            PointData endPoint = pointDrawArray.get(pointDrawArray.size() - 1);

            // 判定是否需要切换背景纸张，执行换页
            if (startPoint.isStroke_start() &&
                    !StringUtils.isStringEqual(startPoint.getPage_id(), MyApp.getInstance().getPaperid())) {

                String newPaperId = String.valueOf(startPoint.getPage_id());
                MyApp.getInstance().setPaperid(newPaperId);


                Log.e("PaperChanged", "纸张切换至"+newPaperId);
                dw.notifyChangeBackGround();
                StrokeManager.clearCounter();

            } else {

                dw.notifyDraw();
                // 触发抬笔后的判定逻辑
                // TODO: something caused error, fix this! 2023年12月4日
                if (endPoint.isStroke_end()){
                    // 当前笔迹信息与纸张和控件id对应，即pageID-gridID-stroke_list
                    List<PointData> strokes = PointManager.getInstance().getLatestStroke();
                    Log.e("Stroke", strokes.size()+" ");

                    // 当笔在其他材料上写字时，提笔会产生长度为1的数据，后续逻辑无法解析造成全部崩溃
                    // 为了防止乱玩笔，只储存长度为1的笔迹。理论上不会出错的
                    if(strokes.size()>1) {
                        StrokeManager.getInstance().append(strokes);
                    }
                }
            }
        }

    }


    @Override
    public void notifyBatchPointData(List<PointData> pointDrawArray) {}

    @Override
    public void notifyFirmwareWithNewVersion(String newVersion) {}

    @Override
    public void notifyDataSynchronizationMode(int mode) {
    }

    @Override
    public void notifyContinueToUseSuccess() {
        Log.e(TAG, "notifyContinueToUseSuccess");
    }

    @Override
    public void notifyContinueToUseFail() {
        Log.e(TAG, "notifyContinueToUseFail");
    }

    @Override
    public void notifyBoundMobile(String mobile) {
    }

    @Override
    public void notifyModel(String model) {
        Log.e(TAG, "notifyModel:" + model);
    }

    @Override
    public void unsynchronizedDataWithPercentage(float percentage) {
    }

    @Override
    public void notifySyncComplete() {
    }

    @Override
    public void accelerometerDataSendFromPenOnXYZ(float x, float y, float z, int jiaodu) {
    }

    @Override
    public void notifyWrittingBatchPointData(List<PointData> pointDrawArray) {
    }

    @Override
    public void notifyOfflineBatchPointData(List<PointData> list, int i) {

    }

    @Override
    public void notifyCameraState() {
    }

    @Override
    public void notifyChargeState(int chargestatus) {
    }
}
