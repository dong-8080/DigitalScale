package com.bupt.myapplication.util;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 事件记录工具类
 * 用于记录用户操作事件并保存为JSON格式到本地文件
 */
public class EventLogger {
    private static final String TAG = "EventLogger";
    private static final String EVENT_LOG_FILE_NAME = "event_log.json";
    
    // 事件类型常量
    public static final String EVENT_APP_START = "APP_START";
    public static final String EVENT_FIRST_CONFIRM = "FIRST_CONFIRM";
    public static final String EVENT_PARTICIPANT_INFO_CONFIRM = "PARTICIPANT_INFO_CONFIRM";
    public static final String EVENT_START_USE = "START_USE";
    public static final String EVENT_SUBMIT = "SUBMIT";
    
    /**
     * 事件数据类
     */
    public static class EventData {
        private String eventType;      // 事件类型
        private String timestamp;      // 时间戳（格式：yyyy-MM-dd HH:mm:ss）
        private String participantId;  // 被试编号（可选）
        private String penMacAddress;  // 蓝牙笔编号（可选）
        
        public EventData(String eventType, String timestamp, String participantId, String penMacAddress) {
            this.eventType = eventType;
            this.timestamp = timestamp;
            this.participantId = participantId;
            this.penMacAddress = penMacAddress;
        }
        
        // Getters and Setters
        public String getEventType() {
            return eventType;
        }
        
        public void setEventType(String eventType) {
            this.eventType = eventType;
        }
        
        public String getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
        
        public String getParticipantId() {
            return participantId;
        }
        
        public void setParticipantId(String participantId) {
            this.participantId = participantId;
        }
        
        public String getPenMacAddress() {
            return penMacAddress;
        }
        
        public void setPenMacAddress(String penMacAddress) {
            this.penMacAddress = penMacAddress;
        }
    }
    
    /**
     * 记录事件
     * @param context 上下文
     * @param eventType 事件类型
     * @param participantId 被试编号（可为null）
     * @param penMacAddress 蓝牙笔编号（可为null）
     */
    public static void logEvent(Context context, String eventType, String participantId, String penMacAddress) {
        try {
            // 生成时间戳
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String timestamp = sdf.format(new Date());
            
            // 创建事件数据
            EventData eventData = new EventData(eventType, timestamp, participantId, penMacAddress);
            
            // 读取现有的事件列表
            List<EventData> eventList = loadEvents(context);
            
            // 添加新事件
            eventList.add(eventData);
            
            // 保存到文件
            saveEvents(context, eventList);
            
            Log.d(TAG, "Event logged: " + eventType + " at " + timestamp);
        } catch (Exception e) {
            Log.e(TAG, "Error logging event", e);
        }
    }
    
    /**
     * 记录事件（不包含被试编号和蓝牙笔编号）
     */
    public static void logEvent(Context context, String eventType) {
        logEvent(context, eventType, null, null);
    }
    
    /**
     * 从文件加载事件列表
     */
    private static List<EventData> loadEvents(Context context) {
        List<EventData> eventList = new ArrayList<>();
        try {
            File file = new File(context.getExternalFilesDir(null), EVENT_LOG_FILE_NAME);
            if (file.exists() && file.length() > 0) {
                FileInputStream fis = new FileInputStream(file);
                byte[] bytes = new byte[(int) file.length()];
                fis.read(bytes);
                fis.close();
                
                String jsonString = new String(bytes, "UTF-8");
                Gson gson = new Gson();
                EventListWrapper wrapper = gson.fromJson(jsonString, EventListWrapper.class);
                if (wrapper != null && wrapper.events != null) {
                    eventList = wrapper.events;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading events", e);
        }
        return eventList;
    }
    
    /**
     * 保存事件列表到文件
     */
    private static void saveEvents(Context context, List<EventData> eventList) {
        try {
            File file = new File(context.getExternalFilesDir(null), EVENT_LOG_FILE_NAME);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            EventListWrapper wrapper = new EventListWrapper();
            wrapper.events = eventList;
            String jsonString = gson.toJson(wrapper);
            
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(jsonString.getBytes("UTF-8"));
            fos.close();
            
            Log.d(TAG, "Events saved to file: " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Error saving events", e);
        }
    }
    
    /**
     * 事件列表包装类（用于JSON序列化）
     */
    private static class EventListWrapper {
        List<EventData> events;
    }
    
    /**
     * 获取事件日志文件的完整路径（用于调试）
     */
    public static String getLogFilePath(Context context) {
        File file = new File(context.getExternalFilesDir(null), EVENT_LOG_FILE_NAME);
        return file.getAbsolutePath();
    }
}

