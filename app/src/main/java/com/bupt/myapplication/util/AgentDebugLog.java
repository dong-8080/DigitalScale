package com.bupt.myapplication.util;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;

import org.xmlpull.v1.XmlPullParser;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Debug Mode: NDJSON 日志写入（请勿写入敏感信息/PII）。
 */
public final class AgentDebugLog {
    private AgentDebugLog() {}

    // NDJSON log path (system-provisioned)
    private static final String LOG_PATH = "d:\\project\\ADscaler\\Android\\.cursor\\debug.log";
    private static final String SESSION_ID = "debug-session";

    // #region agent log
    public static void log(String runId, String hypothesisId, String location, String message, Map<String, Object> data) {
        try (FileWriter fw = new FileWriter(LOG_PATH, true)) {
            long ts = System.currentTimeMillis();
            StringBuilder sb = new StringBuilder(512);
            sb.append("{");
            kv(sb, "sessionId", SESSION_ID); sb.append(",");
            kv(sb, "runId", runId); sb.append(",");
            kv(sb, "hypothesisId", hypothesisId); sb.append(",");
            kv(sb, "location", location); sb.append(",");
            kv(sb, "message", message); sb.append(",");
            sb.append("\"timestamp\":").append(ts).append(",");
            sb.append("\"data\":").append(mapToJson(data));
            sb.append("}\n");
            fw.write(sb.toString());
            fw.flush();
        } catch (Exception ignored) {
            // best-effort logging only
        }
    }

    /**
     * 预检：扫描指定 layout XML，找出缺少 android:layout_width / android:layout_height 的 tag（记录行号）。
     * 注意：这里只检查 XML 明写属性（不依赖 style 计算），用于定位“某些 ROM 强制要求明写”的崩溃点。
     */
    public static void preflightLayout(Context ctx, int layoutId, String runId, String hypothesisId) {
        Map<String, Object> data = new HashMap<>();
        try {
            Resources r = ctx.getResources();
            String name = r.getResourceName(layoutId);
            data.put("layoutId", layoutId);
            data.put("layoutName", name);

            List<Map<String, Object>> missing = new ArrayList<>();
            XmlResourceParser parser = r.getLayout(layoutId);
            int event;
            while ((event = parser.next()) != XmlPullParser.END_DOCUMENT) {
                if (event != XmlPullParser.START_TAG) continue;
                String tag = parser.getName();
                int line = parser.getLineNumber();

                // 检查 include / 任意 view tag 是否显式声明 layout_width/layout_height
                boolean hasW = hasAndroidAttr(parser, "layout_width");
                boolean hasH = hasAndroidAttr(parser, "layout_height");
                if (!hasW || !hasH) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("tag", tag);
                    item.put("line", line);
                    item.put("hasW", hasW);
                    item.put("hasH", hasH);
                    // 不要记录文本内容，避免 PII
                    missing.add(item);
                    if (missing.size() >= 8) break; // 控制日志体积
                }
            }
            data.put("missingCountTop8", missing.size());
            data.put("missingTop8", missing);
            log(runId, hypothesisId, "AgentDebugLog.preflightLayout", "layout preflight scanned", data);
        } catch (Exception e) {
            data.put("layoutId", layoutId);
            data.put("error", e.getClass().getSimpleName());
            log(runId, hypothesisId, "AgentDebugLog.preflightLayout", "layout preflight failed", data);
        }
    }
    // #endregion agent log

    private static boolean hasAndroidAttr(XmlResourceParser parser, String attrName) {
        String v = parser.getAttributeValue("http://schemas.android.com/apk/res/android", attrName);
        return v != null;
    }

    private static void kv(StringBuilder sb, String k, String v) {
        sb.append("\"").append(escape(k)).append("\":\"").append(escape(v)).append("\"");
    }

    private static String mapToJson(Map<String, Object> m) {
        if (m == null) return "{}";
        StringBuilder sb = new StringBuilder(256);
        sb.append("{");
        boolean first = true;
        for (Map.Entry<String, Object> e : m.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(escape(e.getKey())).append("\":");
            Object v = e.getValue();
            if (v == null) {
                sb.append("null");
            } else if (v instanceof Number || v instanceof Boolean) {
                sb.append(v);
            } else if (v instanceof List) {
                sb.append(listToJson((List<?>) v));
            } else if (v instanceof Map) {
                //noinspection unchecked
                sb.append(mapToJson((Map<String, Object>) v));
            } else {
                sb.append("\"").append(escape(String.valueOf(v))).append("\"");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private static String listToJson(List<?> list) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("[");
        boolean first = true;
        for (Object v : list) {
            if (!first) sb.append(",");
            first = false;
            if (v == null) sb.append("null");
            else if (v instanceof Number || v instanceof Boolean) sb.append(v);
            else if (v instanceof Map) {
                //noinspection unchecked
                sb.append(mapToJson((Map<String, Object>) v));
            } else sb.append("\"").append(escape(String.valueOf(v))).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}


