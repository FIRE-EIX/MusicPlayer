package com.example.myapplication8;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends Activity implements View.OnClickListener {
    Intent intent;
    private Button up, play, next;
    private static TextView timeText;
    private TextView textView;
    ActivityReceiver mActivityReceiver;
    private static TextView endText;
    private ComponentName component;
    private static SeekBar seekBar;
    public static final String UPDATE_ACTION = "com.trampcr.action.UPDATE_ACTION";
    private static SimpleDateFormat time = new SimpleDateFormat("m:ss");
    public static Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            // super.handleMessage(msg);
            // 将SeekBar位置设置到当前播放位置，
            // msg.arg1是service传过来的音乐播放进度信息,将其设置为进度条进度
            seekBar.setMax(msg.arg2);
            seekBar.setProgress(msg.arg1);
            //将进度时间其转为mm:ss时间格式
            timeText.setText(new SimpleDateFormat("mm:ss", Locale.getDefault()).format(new Date(msg.arg1)));
            endText.setText(new SimpleDateFormat("mm:ss", Locale.getDefault()).format(new Date(msg.arg2)));
            return false;
        }
    });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        play = findViewById(R.id.play);
        Button up = findViewById(R.id.upSong);
        Button next = findViewById(R.id.next);
        seekBar = findViewById(R.id.seekbar);
        timeText = findViewById(R.id.timeText);
        textView = findViewById(R.id.text_view);
        endText = findViewById(R.id.endText);
        play.setOnClickListener(this);
        up.setOnClickListener(this);
        next.setOnClickListener(this);
        mActivityReceiver = new ActivityReceiver();
        //创建IntentFilter
        IntentFilter filter = new IntentFilter();
        //指定BroadcastReceiver监听的Action
        filter.addAction(UPDATE_ACTION);
        //注册BroadcastReceiver
        registerReceiver(mActivityReceiver, filter);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    intent = new Intent("com.example.myapplication8.musicService");
                    intent.setPackage(getPackageName());
                    intent.putExtra("num", "Seek_ACTION");
                    intent.putExtra("process", seekBar.getProgress());
                    startService(intent);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public class ActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String name = intent.getStringExtra("name");
            textView.setText(name);
            boolean state = intent.getBooleanExtra("state", true);
            if (state) {
                play.setText("暂停");
            } else {
                play.setText("播放");
            }
        }
    }

    @Override
    public void onClick(View v) {
        String num = "";
        intent = new Intent("com.example.myapplication8.musicService");
        int status;
        intent.setPackage(this.getPackageName());
        switch (v.getId()) {
            case R.id.play:
                num = "PLAY_ACTION";
                break;
            case R.id.upSong:
                num = "PREVIOUS_ACTION";
                break;
            case R.id.next:
                num = "NEXT_ACTION";
                break;
        }
        intent.putExtra("num", num);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (intent != null) {
            stopService(intent);
        }
        unregisterReceiver(mActivityReceiver);
    }
}