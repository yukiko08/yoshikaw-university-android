package com.example.yukik.arduino_blt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements Runnable, View.OnClickListener {
    /* tag */
    private static final String TAG = "BluetoothSample";

    /* Bluetooth Adapter */
    private BluetoothAdapter mAdapter;

    /* Bluetoothデバイス */
    private BluetoothDevice mDevice;

    /* Bluetooth UUID */
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    /* デバイス名 */
    private final String DEVICE_NAME = "RNBT-2526";

    /* Soket */
    private BluetoothSocket mSocket;

    /* Thread */
    private Thread mThread = null;

    /* Threadの状態を表す */
    private boolean isRunning;

    /** 接続ボタン. */
    private Button connectButton;



    /** ステータス. */
    private TextView mStatusTextView;

    /** Bluetoothから受信した値. */
    private TextView mInputTextView;

    /** right. */
    private TextView rightTextView;

    /** left. */
    private TextView leftTextView;

    /** back */
    private TextView backTextView;


    /** Action(ステータス表示). */
    private static final int VIEW_STATUS = 0;

    /** Action(取得文字列). */
    private static final int VIEW_INPUT = 1;


    /**一度だけ用いる時刻**/

    private int DayFlag = 0;



    /** Connect確認用フラグ */
    private boolean connectFlg = false;

    /** BluetoothのOutputStream. */
    OutputStream mmOutputStream = null;
    private Context context;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInputTextView = (TextView)findViewById(R.id.inputValue);
        mStatusTextView = (TextView)findViewById(R.id.statusValue);

        rightTextView = (TextView)findViewById(R.id.statusRight);
        leftTextView = (TextView)findViewById(R.id.statusLeft);
        backTextView = (TextView)findViewById(R.id.statusBack);

        connectButton = (Button)findViewById(R.id.connectButton);

        connectButton.setOnClickListener(this);

        // Bluetoothのデバイス名を取得
        // デバイス名は、RNBT-XXXXになるため、
        // DVICE_NAMEでデバイス名を定義
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mStatusTextView.setText("SearchDevice");
        Set< BluetoothDevice > devices = mAdapter.getBondedDevices();
        for ( BluetoothDevice device : devices){

            if(device.getName().equals(DEVICE_NAME)){
                mStatusTextView.setText("find: " + device.getName());
                mDevice = device;
            }
        }



    }

    @Override
    protected void onPause(){
        super.onPause();

        isRunning = false;
        try{
            mSocket.close();
        }
        catch(Exception e){}
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void run() {
        InputStream mmInStream = null;

        Message valueMsg = new Message();
        valueMsg.what = VIEW_STATUS;
        valueMsg.obj = "connecting...";
        mHandler.sendMessage(valueMsg);

        try{

            // 取得したデバイス名を使ってBluetoothでSocket接続
            mSocket = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
            mSocket.connect();
            mmInStream = mSocket.getInputStream();
            mmOutputStream = mSocket.getOutputStream();

            // InputStreamのバッファを格納
            byte[] buffer = new byte[1024];

            // 取得したバッファのサイズを格納
            int bytes;
            valueMsg = new Message();
            valueMsg.what = VIEW_STATUS;
            valueMsg.obj = "connected.";
            mHandler.sendMessage(valueMsg);

            connectFlg = true;

            String sendMsg = "";


            //sound準備
            Calculation cal = new Calculation();
            SoundThread rThread = new SoundThread();
            rThread.start();

            /*SoundThread lThread = new SoundThread();
            lThread.start();
            SoundThread bThread = new SoundThread();
            bThread.start();*/

            while(isRunning){

                // InputStreamの読み込み
                bytes = mmInStream.read(buffer);
                // String型に変換
                String readMsg = new String(buffer, 0, bytes);

                // null以外なら表示
                if(readMsg.trim() != null && !readMsg.trim().equals("")){
                    //Log.i(TAG,"receive value="+readMsg.trim());

                    //check last word
                    if(!readMsg.equals("r")){
                        /*StringBuilder sb =new StringBuilder();
                        readMsg = sb.deleteCharAt(sb.indexOf("r")).toString();*/
                        sendMsg = sendMsg + readMsg;
                        Log.i(TAG,"now sendMsg:"+sendMsg);

                    }else{

                        Log.i(TAG,"sendMsg:"+sendMsg);
                        if(sendMsg.length()==13) {
                            valueMsg = new Message();
                            valueMsg.what = VIEW_INPUT;
                            valueMsg.obj = sendMsg;
                            mHandler.sendMessage(valueMsg);

                            writeFile(sendMsg);
                            Calculation data=  cal.partSound(sendMsg);
                            rThread.set(data.rightData);
                            /*lThread.set(data.leftData);
                            bThread.set(data.backData);*/

                        }
                        sendMsg = "";
                    }
                }
                else{
                     Log.i(TAG,"value=nodata");
                }

            }
        }catch(Exception e){

            e.printStackTrace();

            valueMsg = new Message();
            valueMsg.what = VIEW_STATUS;
            valueMsg.obj = "Error1:" + e;
            mHandler.sendMessage(valueMsg);

            try{
                mSocket.close();
            }catch(Exception ee){}
            isRunning = false;
            connectFlg = false;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.equals(connectButton)) {
            // 接続されていない場合のみ
            if (!connectFlg) {
                mStatusTextView.setText("try connect");

                mThread = new Thread(this);
                // Threadを起動し、Bluetooth接続
                isRunning = true;
                Log.i(TAG, "Running true");
                //このスレッドのrunを呼ぶ
                mThread.start();
            }

        }
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }



    private void writeFile(String sendMsg){
        //日時を取得
        Date now = new Date(System.currentTimeMillis());
        // 日時のフォーマットオブジェクト作成
        DateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH時mm分");
        // フォーマット
        String nowText = formatter.format(now);

        if (isExternalStorageReadable() == true) {
            File file = new File("/storage/7F54-1325/Documents/test.txt");
            try {
                FileWriter fw = new FileWriter(file,true);
                if(DayFlag == 0){
                    fw.write(nowText);
                    fw.write("\r\n");
                    DayFlag = 1;
                }else{
                    fw.write(sendMsg);
                    fw.write("\r\n");
                }
                fw.close();
                Log.i(TAG, "can file write");
            } catch (IOException e) {
                Log.i(TAG, "error file write");
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "write denied");
        }
    }




    /**
     * 描画処理はHandlerでおこなう
     */
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int action = msg.what;
            String msgStr = (String)msg.obj;
            if(action == VIEW_INPUT){
                mInputTextView.setText(msgStr);

                rightTextView.setText(msgStr.substring(0,4));
                leftTextView.setText(msgStr.substring(4,8));
                backTextView.setText(msgStr.substring(8,12));

            }
            else if(action == VIEW_STATUS){
                mStatusTextView.setText(msgStr);
            }
        }
    };


}




