package com.bupt.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class Recorder extends Fragment {
    private static final int MY_PERMISSIONS_RECORD_AUDIO = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private View root;
    private Button startRecordBtn;
    private Button closeRecordBtn;
    private IFragmentCallBack iFragmentCallBack;
    private String filePath;
    private MediaRecorder recorder;
    private boolean isStart = false;
    File soundFile;                // 存放录音的文件
    boolean isRecording;
    int frequency = 11025;
    int inChannelConfig = AudioFormat.CHANNEL_IN_MONO;
    int outChannelConfig = AudioFormat.CHANNEL_OUT_MONO;
    int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    public void setiFragmentCallBack(IFragmentCallBack callback){
        iFragmentCallBack = callback;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(root == null){
            root = inflater.inflate(R.layout.fragment_recorder, container, false);
        }
        startRecordBtn = root.findViewById(R.id.start_record_button);
        closeRecordBtn = root.findViewById(R.id.close_button1);
        soundFile = new File(getContext().getExternalFilesDir(null), "audioRecord.pcm");


        startRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isStart == false){
                    isStart = true;
                    startRecordBtn.setText(R.string.record_stop);
                    Thread t = new Thread() {
                        public void run() {
                            try {
                                record();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    t.start();
                }
                else{
                    isStart = false;
                    startRecordBtn.setText(R.string.record_start);
                    isRecording = false;
                    if (soundFile.exists()) {
                        try {
                            play(frequency);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        closeRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iFragmentCallBack.send2main("close");
            }
        });
        return root;
    }

    void record() throws IOException {
        // 动态权限申请
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            return;
        }

        if (soundFile.exists()) soundFile.delete();
        soundFile.createNewFile();

        FileOutputStream fos = new FileOutputStream(soundFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        DataOutputStream dos = new DataOutputStream(bos);

        int bufferSize = AudioRecord.getMinBufferSize(frequency, inChannelConfig, audioFormat);
        short[] buffer = new short[bufferSize];
        AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, inChannelConfig, audioFormat, bufferSize);

        audioRecord.startRecording();
        isRecording = true;
        while(isRecording){
            int bufferRead = audioRecord.read(buffer, 0, bufferSize);
            for(int i=0; i<bufferRead; i++){
                dos.writeShort(buffer[i]);
            }
        }
        audioRecord.stop();
        audioRecord.release();

        dos.close();
        bos.close();
        fos.close();

        GlobalVars.getInstance().setSoundFile(soundFile);
    }

    void play(int frq) throws IOException{
        int length = (int)soundFile.length()/2;
        if(!soundFile.exists() || length==0) {
            Toast.makeText(getContext(), "音频文件不存在或为空", Toast.LENGTH_SHORT).show();
            return;
        }
        short[] data = new short[length];
        FileInputStream fis = new FileInputStream(soundFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dis = new DataInputStream(bis);
        int i=0;
        while(dis.available()>0) {
            data[i] = dis.readShort();
            i++;
        }
        dis.close();
        bis.close();
        fis.close();

        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frq, outChannelConfig, audioFormat, length*2, AudioTrack.MODE_STREAM);
        audioTrack.play();
        audioTrack.write(data, 0, length);
        audioTrack.stop();
        //audioTrack.release();		// 不能马上release，因为write后是异步播放，此时还刚开始播放，release就不播放了
    }
}