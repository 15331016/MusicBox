package com.example.yc.musicbox;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

public class MusicService extends Service {

    public static MediaPlayer mp = new MediaPlayer();//音乐
    public MusicService() {
        try {
            mp.setDataSource(Environment.getExternalStorageDirectory() + "/data/K.Will_Melt.mp3");//读取sd卡
            mp.prepare();
            mp.setLooping(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private IBinder mBinder = new MyBinder();
    @Override
    public IBinder onBind(Intent intent) {
        // 必须实现的接口
        return mBinder;
    }

    public class MyBinder extends Binder
    {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 101://“还未播放”状态，点击开始播放
                    mp.start();
                    break;
                case 102://“正在播放”状态，点击暂停
                    mp.pause();
                    break;
                case 103://“正在暂停”状态，点击继续播放
                    mp.start();
                    break;
                case 104://点击停止，进度条归零
                    mp.stop();
                    try {
                        mp.prepare();
                        mp.seekTo(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
            return super.onTransact(code, data, reply, flags);
        }
    }

}
