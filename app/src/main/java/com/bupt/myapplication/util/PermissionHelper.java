package com.bupt.myapplication.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionHelper {
    private static final String TAG = "PermissionHelper";
    public static final int PERMISSION_REQUEST_CODE = 200;
    public static final int REQUEST_BLUETOOTH_PERMISSION = 310;

    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_PRIVILEGED,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.INTERNET,
    };

    private static final String[] BLUETOOTH_PERMISSIONS = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    public static void checkAndRequestPermissions(Activity activity) {
        if (!hasPermissions(activity, REQUIRED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(activity, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
        }
    }

    public static void checkBluetoothPermission(Activity activity) {
        if (!hasPermissions(activity, BLUETOOTH_PERMISSIONS)) {
            ActivityCompat.requestPermissions(activity, BLUETOOTH_PERMISSIONS, REQUEST_BLUETOOTH_PERMISSION);
        }
    }

    public static boolean hasPermissions(Activity activity, String... permissions) {
        if (permissions != null) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void handlePermissionResult(Activity activity, int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            int grantResultsSum = 0;
            for (int grantResult : grantResults) {
                grantResultsSum += grantResult;
            }
            if (grantResults.length > 0 && grantResultsSum == PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "权限已被授予，可以进行蓝牙操作");
                for (int i = 0; i < grantResults.length; i++) {
                    Log.e(TAG, "permission " + permissions[i] + ":" + grantResults[i]);
                }
            } else {
                Log.e(TAG, "权限被拒绝，无法执行蓝牙操作");
                Toast.makeText(activity, "权限被拒绝，无法执行蓝牙操作", Toast.LENGTH_LONG).show();
            }
        }
    }
}

