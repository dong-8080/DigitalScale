package com.bupt.myapplication.util;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    public static String macConvert(String input) {
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < input.length(); i += 2) {
            if (i > 0) {
                output.append(":");
            }
            output.append(input.substring(i, Math.min(i + 2, input.length())));
        }

        String result = output.toString();
        return result;
    }

    // 判断手机号码是否正确
    public static boolean isValidPhoneNumber(String phoneNumber) {
        String regex = "^1[3456789]\\d{9}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }


    private static final String[] CN_NUMBERS = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
    private static final String[] CN_UNITS = {"", "十", "百", "千"};

    // 阿拉伯数字转化为大写汉字
    public static String convertToChinese(int number) {

        if (number < 0 || number >= 10000) {
            throw new IllegalArgumentException("Number out of range (0-9999)");
        }

        if (number == 0) {
            return CN_NUMBERS[0];
        }

        StringBuilder result = new StringBuilder();
        int unitIndex = 0;

        while (number > 0) {
            int digit = number % 10;
            if (digit != 0 || (result.length() > 0 && result.charAt(0) != '零')) {
                result.insert(0, CN_UNITS[unitIndex]);
                result.insert(0, CN_NUMBERS[digit]);
            }
            unitIndex++;
            number /= 10;
        }

        return result.toString();
    }

    // 判断纸张ID是否一致, 初始化应该为空的直接不相等
    public static boolean isStringEqual(long paper_id_from_point, String paper_id_stored){
        if (paper_id_stored==null){
            return false;
        }
        long paper_id_stored_numbered = Long.parseLong(paper_id_stored);
        return paper_id_stored_numbered==paper_id_from_point;
    }


    // 读取raw下的json字符串，测试的时候使用
    public static String readRawJsonFile(Context context, int resId) {
        InputStream inputStream = context.getResources().openRawResource(resId);
        InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputreader);
        StringBuilder stringBuilder = new StringBuilder();
        String line;

        try {
            while ((line = buffreader.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
        } catch (Exception e) {
            return null;
        } finally {
            try {
                buffreader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return stringBuilder.toString();
    }


}