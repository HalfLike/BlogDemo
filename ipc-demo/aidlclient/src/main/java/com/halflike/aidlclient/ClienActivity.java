package com.halflike.aidlclient;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.halflike.aidlcommon.QCEmail;
import com.halflike.aidlcommon.ICallback;
import com.halflike.aidlcommon.IServer;

import java.util.Date;

public class ClienActivity extends Activity {

    public static final int MSG_RECEIVE_EMAIL = 1;
    public static final String TAG = "halflike";

    IServer mServer = null;

    TextView mConsole;
    Button mConnect;
    Button mDisconnect;
    Button mGetEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clien);

        mConsole = (TextView) findViewById(R.id.console);
        mConnect = (Button) findViewById(R.id.connect);
        mDisconnect = (Button) findViewById(R.id.disconnect);
        mGetEmail = (Button) findViewById(R.id.getEamil);

        mConnect.setOnClickListener(mConnectListener);
        mDisconnect.setOnClickListener(mDisconnectListener);
        mGetEmail.setOnClickListener(mGetEmailListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mServer != null) {
            unbindService(mServiceConnection);
        }
    }

    private void log(String msg) {
        if (mConsole != null) {
            mConsole.setText(mConsole.getText() + "\n" + msg);
        }
    }

    // 通过绑定服务端 service，建立连接
    private OnClickListener mConnectListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setAction("com.halflike.aidlserver.ServerService");
            bindService(intent,
                    mServiceConnection, Context.BIND_AUTO_CREATE);
            log("system:Contting...");
        }
    };

    // 与服务端断开连接
    private OnClickListener mDisconnectListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mServer != null) {
                try {
                    mServer.unregisteCallback();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            unbindService(mServiceConnection);
            log("system:Disconnecting.");
        }
    };

    // 获取邮件信息
    private OnClickListener mGetEmailListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mServer == null) {
                log("system:Error! Disconncttion.");
                return;
            }
            try {
                QCEmail email = mServer.getEmail();
                log("Emial:");
                log("At " + new Date(email.creatTime).toString());
                log("from " + email.fromWho + "to " + email.toWho);
                log("content:" + email.content);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mServer = IServer.Stub.asInterface(service);
            try {
                if (mServer.registeCallback(mCallback)) {
                    log("system:Connection and registe callback succeed.");
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServer = null;
        }
    };

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_RECEIVE_EMAIL:
                    log("system:Bob have a new email.");
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    /**
     * 实现从服务端接收回调的客户端接口
     */
    ICallback mCallback = new ICallback.Stub() {

        @Override
        public void receiveEmail() throws RemoteException {
            Log.d(TAG, "client receive notify");
            mHandler.sendMessage(mHandler.obtainMessage(MSG_RECEIVE_EMAIL));
        }
    };

}
