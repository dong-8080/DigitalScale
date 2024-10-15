package com.bupt.myapplication.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.bupt.myapplication.object.CenterPoint;
import com.bupt.myapplication.object.PageCoordinate;
import com.bupt.myapplication.object.PageCoordinateList;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;


public class JsonUtil {

    public static String loadJsonFromResource(Context context, int resourceId) {
        StringBuilder jsonStringBuilder = new StringBuilder();
        try {
            Resources resources = context.getResources();
            InputStream inputStream = resources.openRawResource(resourceId);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                jsonStringBuilder.append(line);
            }

            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonStringBuilder.toString();
    }

    public static JSONObject convertToJson(String jsonString) {
        try {
            return new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String readJsonFromAssets(Context context) {
        StringBuilder content = new StringBuilder();
        AssetManager assetManager = context.getAssets();
        try {
            InputStream inputStream = assetManager.open("page_coordinates.json");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line);
            }

            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return content.toString();
    }

    public static List<PageCoordinate> readPageCoordinate(Context context) {
        String jsonString = readJsonFromAssets(context);
//        List<PageCoordinate> pageCoordinate_list = PageJsonParser.parseJson(jsonString);

        // todo：cause error!
        Gson gson = new Gson();
        PageCoordinateList pcl = gson.fromJson(jsonString, PageCoordinateList.class);
        return pcl.getPages();
    }
}


