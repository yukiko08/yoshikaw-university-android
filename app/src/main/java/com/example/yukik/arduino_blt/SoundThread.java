package com.example.yukik.arduino_blt;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

/**
 * Created by yukik on 2018/04/09.
 */

public class SoundThread extends Thread{

    /* tag */
    private static final String TAG = "SoundThread";
    private AudioTrack mTrack = null;
    Calculation s_cal = new Calculation();
    private byte[]data = s_cal.createWaves(8000,100);
    private boolean mstop = false;

    public SoundThread(){

        try {
            // AudioTrackコンストラクタ
            mTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    44100,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_8BIT,
                    441,
                    AudioTrack.MODE_STREAM);


            /* 再生完了のリスナー設定
            mTrack.setNotificationMarkerPosition(data.length);
            mTrack.setPlaybackPositionUpdateListener(
                    new AudioTrack.OnPlaybackPositionUpdateListener() {
                        public void onPeriodicNotification(AudioTrack track) {
                        }

                        public void onMarkerReached(AudioTrack track) {
                            if (track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                                track.stop();
                                track.release();
                                track = null;
                            }
                        }
                    }
            );*/
            // 波形データの書き込みと再生
            mTrack.reloadStaticData();
            mTrack.play();

            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

            Log.i(TAG, "can pley sound");


        } catch (Exception e) {
            Log.i(TAG, "sound error");
            e.printStackTrace();
            mTrack.flush();
            if (mTrack != null) {
                mTrack.stop();
                mTrack.release();
            }
            mTrack = null;
        }




    }




    public  void set(byte[] Data){
        data = Data;
        //Log.i(TAG,"data set");
    }


    public void run(){


        while(!mstop) {


            //ひープとスタック　newをしたとき　どこにデータができるか
            //場所が入っている?dataに
            //aは保証されている
            //stateiligal　設定されてない場所に行くk。
            //スレッドでクラスを何度も呼ぶと変数などの容量がめってあ必要になる
            //変数はどこにあるか　そのスレッドのスタックにある。
            //byte[] a = data;
            //Log.i(TAG,"data = "+data);
            if(data !=null) {
                mTrack.write(data, 0, data.length);
            }else{
                Log.i(TAG,"data = null");
            }
        }

    }




}


