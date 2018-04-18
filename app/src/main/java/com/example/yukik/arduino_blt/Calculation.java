package com.example.yukik.arduino_blt;

import android.util.Log;

/**
 * Created by yukik on 2018/04/09.
 */

public  class Calculation{

    /* tag */
    private static final String TAG = "Calcuration";

    // サンプリングレートの下限値、上限値の設定
    private static final int SAMPLE_RATE_MIN = 4000;
    private static final int SAMPLE_RATE_MAX = 96000;

    int rightRate;
    int leftRate;
    int backRate;

    byte[] rightData;
    byte[] leftData;
    byte[] backData;



    // サンプリングレートの計算
    public int calcSampleRate(int freq) {
        int sampleRate = freq * 4;

        if(sampleRate < SAMPLE_RATE_MIN) {
            sampleRate = SAMPLE_RATE_MIN;
        } else if(sampleRate > SAMPLE_RATE_MAX) {
            sampleRate = SAMPLE_RATE_MAX;
        }
        return sampleRate;
    }

    // 波形データ生成
    public byte[] createWaves(int sampleRate, int time) {
        int dataNum = (int)((double)sampleRate * ((double)time / 1000.0));
        byte[] data = new byte[dataNum];
        // ここに波形データ格納の処理も入る
        int flag = 0;

        for(int i = 0; i < dataNum-1; i = i + 2) {
            if(flag == 0) {
                data[i] = (byte)0xff;
                data[i+1] = (byte)0xff;
                flag++;
            } else {
                data[i] = (byte)0x00;
                data[i+1] = (byte)0x00;
                flag--;
            }
        }
        return data;
    }

    public Calculation partSound(String sendMsg) {

        this.rightRate = calcSampleRate(Integer.parseInt(sendMsg.substring(0, 4))*6+8000);
        this.leftRate = calcSampleRate(Integer.parseInt(sendMsg.substring(4, 8))*6+8000);
        this.backRate = calcSampleRate(Integer.parseInt(sendMsg.substring(8, 12))*6+8000);

        this.rightData = createWaves(rightRate, 10);
        this.leftData = createWaves(leftRate, 10);
        this.backData = createWaves(backRate, 10);

        Log.i(TAG, "right Rate"+rightRate);


        return this;
    }
}
