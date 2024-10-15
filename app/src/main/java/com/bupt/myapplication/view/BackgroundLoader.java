package com.bupt.myapplication.view;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;


import com.bupt.myapplication.R;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// 暂时只启用本地加载图片的模式
public class BackgroundLoader {

    private Context context;

    private DrawingView dw;
    private String pageID;

    public BackgroundLoader(Context context, String pageID, DrawingView dw) {
        this.context = context;
        this.dw = dw;
        this.pageID = pageID;
    }

    // 从本地加载图片
    public Bitmap load_local() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inDensity = DisplayMetrics.DENSITY_DEFAULT;
        options.inTargetDensity = DisplayMetrics.DENSITY_DEFAULT;

        Bitmap bitmap = null;
        String background_name = "background_"+pageID;
        System.out.println(background_name);
        int resourceId = context.getResources().getIdentifier(background_name, "drawable",context.getPackageName());
        if (resourceId != 0) {
            bitmap = BitmapFactory.decodeResource(this.context.getResources(), resourceId, options);
        } else {
            bitmap = BitmapFactory.decodeResource(this.context.getResources(), R.drawable.background_empty, options);
        }
        return bitmap;
    }

    public Bitmap load() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inDensity = DisplayMetrics.DENSITY_DEFAULT;
        options.inTargetDensity = DisplayMetrics.DENSITY_DEFAULT;

        String cache_filename = this.pageID + " " + "drawing_background.png";

        if (isImageCached(context, cache_filename)) {
            // Load image from cache
            File cacheFile = new File(context.getCacheDir(), cache_filename);
            Bitmap bitmap = BitmapFactory.decodeFile(cacheFile.getAbsolutePath(), options);
            return bitmap;
        } else {
            // 暂时返回一个空图片，并网络请求需要的图片
//            new LoadImageTask(context, imageView).execute(IMAGE_URL);
            Bitmap bitmap = BitmapFactory.decodeResource(this.context.getResources(),
                    R.drawable.background_empty, options);
//            Log.e("BACKGROUND", "pending for " + pageID);
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    pendingForImage(pageID, cache_filename);
//                }
//            }).start();
////            pendingForImage(pageID, cache_filename);
//            Log.e("BACKGROUND", "pending for " + pageID);
            return bitmap;
        }
    }

    private boolean isImageCached(Context context, String cache_filename) {
        File cacheFile = new File(context.getCacheDir(), cache_filename);
        return cacheFile.exists();
    }

//    private void pendingForImage(String pageIdD, String cache_filename) {
//        String url = HttpUtil.API + "getbackgroundimg";
//
//        RequestBGImage req = new RequestBGImage(pageIdD);
//
//        Gson gson = new Gson();
//        RequestBody requestBody = RequestBody.create(
//                MediaType.parse("application/json"),
//                gson.toJson(req)
//        );
//        HttpUtil httpUtil = HttpUtil.getInstance();
//        httpUtil.postRequest(url, requestBody, new HttpUtil.OkHttpCallback() {
//            @Override
//            public String onResponse(String response) {
//                Gson gson = new Gson();
//                ResponseBGImage responseObject = gson.fromJson(response, ResponseBGImage.class);
//
////                String toastMessage = responseObject.getMsg();
//                String backgroundUrl = responseObject.getData();
//                Log.e("BACKGROUND", "background:"+backgroundUrl);
//                if (!backgroundUrl.startsWith("http")) {
//                    backgroundUrl = "https://" + backgroundUrl;
//                }
//                cacheFile(cache_filename, backgroundUrl);
//
//                return backgroundUrl;
//            }
//
//            @Override
//            public void onFailure(IOException e) {
//                Log.e("HTTPTEST", "onFailure");
//                Log.e("HTTPTEST", e.toString());
//            }
//        });
//    }

    private void cacheFile(String cache_filename, String imageUrl) {
        File storageDir = context.getCacheDir(); // 获取应用的缓存目录
        File imageFile = new File(storageDir, cache_filename);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(imageUrl)
                .build();

        try (Response res = client.newCall(request).execute()) {
            if (res.isSuccessful()) {
                InputStream inputStream = res.body().byteStream();
                FileOutputStream outputStream = new FileOutputStream(imageFile);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }

                outputStream.flush();
                outputStream.close();
                inputStream.close();

                // 提醒重新绘制图片

                Log.e("BACKGROUND", "cached file success");
                this.dw.notifyChangeBackGround();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
