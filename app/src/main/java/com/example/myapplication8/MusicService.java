package com.example.myapplication8;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import static android.media.MediaPlayer.SEEK_CLOSEST;

public class MusicService extends Service {
    MediaPlayer mediaPlayer = new MediaPlayer();
//    static final String a1 = "com.example.myapplication8.a1";
    String fileNames[];
    int length;
    private boolean reset = false;
    int count = 1;
    private boolean state = false;
    boolean prepare = false;
    AssetManager am;
    int i = 0;
    int end;
    AssetFileDescriptor afd;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            fileNames = getAssets().list("music");
        } catch (IOException e) {
            e.printStackTrace();
        }
        length = fileNames.length;
        am = getAssets();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    //播放音乐
    private void initMediaPlayer() throws IOException {
        mediaPlayer.reset();
        try {
            //mCursor = getContentResolver().query(MUSIC_URL, mCursorCols, "duration > 10000", null, null);
            afd = am.openFd("music/" + fileNames[i]);
            // 使用MediaPlayer加载指定的声音文件。
            mediaPlayer.setDataSource(afd.getFileDescriptor(),
                    afd.getStartOffset(), afd.getLength());
            afd.close();
            // mediaPlayer.setDataSource("src/assets/"+fileNames[0]);
            //mediaPlayer.prepare();//进入到准备状态
            try {
                mediaPlayer.prepare();
            } catch (IllegalStateException e) {
                mediaPlayer.release();
                mediaPlayer = null;
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(afd.getFileDescriptor(),
                        afd.getStartOffset(), afd.getLength());
                mediaPlayer.prepare();
            }

            //每隔50毫秒发送音乐进度
            System.out.println("1111" + prepare);
            end = mediaPlayer.getDuration();
            mediaPlayer.start();
            System.out.println("yinyue :" + i);
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    //if(mediaPlayer.isPlaying()){
                    Message msg = Message.obtain();
                    //Message对象的arg1参数携带音乐当前播放进度信息，类型是int
                    msg.arg2 = end;
                    msg.arg1 = mediaPlayer.getCurrentPosition();
                    MainActivity.handler.sendMessage(msg);
                }
            }, 0, 500);
        } catch (Exception E) {
            E.printStackTrace();
        }

    }

    void UpdateUi() {
        Intent sendIntent = new Intent(MainActivity.UPDATE_ACTION);
        sendIntent.putExtra("name", fileNames[i]);
        sendIntent.putExtra("state", state);
        sendBroadcast(sendIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        if (intent != null) {
            String num = intent.getStringExtra("num");
            switch (num) {
                case "PLAY_ACTION":
                    if (state && mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        state = false;
                    } else {
                        if (count == 1) {
                            try {
                                count++;
                                initMediaPlayer();
                                state = true;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            mediaPlayer.start();
                            state = true;
                        }
                    }
                    break;
                case "PREVIOUS_ACTION":
                    if (i > 0) {
                        i = i - 1;
                    } else {
                        i = length - 1;
                    }
                    try {
                        initMediaPlayer();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    state = true;
                    break;
                case "NEXT_ACTION":
                    playNext();
                case "Seek_ACTION":
                    int process = intent.getIntExtra("process", -1);
                    mediaPlayer.seekTo(process);
                    mediaPlayer.start();
            }
            UpdateUi();

        }
    }

    void playNext() {
        if (i >= length - 1) {
            i = 0;
        } else {
            i = i + 1;
        }
        try {
            initMediaPlayer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        state = true;
    }
}
