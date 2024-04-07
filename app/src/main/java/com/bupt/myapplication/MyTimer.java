package com.bupt.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;


public class MyTimer extends Fragment {

    private TextView textView;
    private Button closeButton;
    private Button startButton;
    private Button clearButton;
    private View root;
    private IFragmentCallBack iFragmentCallBack;
    private long startTime;
    private long curTime;
    private long runnedTime = 0;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
//            Log.d("timer", "ok");
            if(isStart == false) return;
            curTime = SystemClock.uptimeMillis() - startTime + runnedTime;
            int seconds = (int) (curTime / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            int tenMs = (int) (curTime % 1000);
            tenMs = tenMs / 10;
            String timeString = String.format("%02d:%02d:%02d", minutes, seconds, tenMs);
            textView.setText(timeString);
            if(isStart) {
                handler.postDelayed(this, 10); // 10ms后再次调度此任务
            }
        }
    };
    private boolean isStart = false;


    public void setiFragmentCallBack(IFragmentCallBack callback){
        iFragmentCallBack = callback;
    }
    public MyTimer() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(root == null){
            root = inflater.inflate(R.layout.fragment_timer, container, false);
        }
        textView = root.findViewById(R.id.tv_time);
        closeButton = root.findViewById(R.id.close_button);
        startButton = root.findViewById(R.id.start_button);
        clearButton = root.findViewById(R.id.clear_button);
        textView.setText("00:00:00");
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iFragmentCallBack.send2main("close");
            }
        });
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isStart == false){
                    isStart = true;
                    startTime = SystemClock.uptimeMillis();
                    handler.postDelayed(runnable, 0);
                    startButton.setText("停止");
                }
                else{
                    isStart = false;
                    runnedTime = curTime;
                    startButton.setText("开始");
                }
            }
        });
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isStart = false;
                startButton.setText("开始");
                runnedTime = 0;
                startTime = SystemClock.uptimeMillis();
                textView.setText("00:00:00");
            }
        });

        return root;
    }
}