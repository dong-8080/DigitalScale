package com.bupt.myapplication.util;

import android.content.Context;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CSVReaderUtil {
    public static List<PageMap> readCSVFile(Context context, String fileName) {
        List<PageMap> pages = new ArrayList<>();
        StringBuilder data = new StringBuilder();
        try {
            // 使用GBK编码打开文件
            InputStream is = context.getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "GBK"));
            String line;
            while ((line = reader.readLine()) != null) { // 读取每一行
                data.append(line);
                data.append("\n"); // 保留换行符
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 将读取的数据按行分割
        String[] lines = data.toString().split("\n");
        for (String line : lines) {
            if (line.isEmpty()) continue; // 跳过空行
            String[] columns = line.split(","); // 按逗号分割
            if (columns.length >= 3) {
                PageMap page = new PageMap();
                page.pageId = columns[0];
                page.shortName = columns[1];
                page.fullName = columns[2];
                page.completed = false;
                pages.add(page);
            }
        }
        return pages;
    }

    // 背景图id和名称的映射，对应assets下的两个csv文件
    public static class PageMap {
        public String pageId;
        public String shortName;
        public String fullName;

        public boolean completed;
    }
}