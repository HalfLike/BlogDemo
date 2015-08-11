package com.halflike.ipcdeom;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.halflike.ipcdeom.server.ServerService;

public class ClientActivity extends Activity {

    static final String TAG = "halflike";

    TextView recordView;
    Button sayHelloBtn;

    Messenger sMessenger = null;
    Messenger cMessenger = null;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "connect server succeed.");
            // 获取服务端的 Messenger
            sMessenger = new Messenger(service);
            Message msg = Message.obtain(null, ServerService.MSG_BIND_MESSENGER);
            // 传递客户端的 Messenger 到服务端
            msg.replyTo = cMessenger;
            try {
                sMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "disconnect server");
            sMessenger = null;
        }
    };

    class ClientHandler extends Handler {
        public ClientHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ServerService.MSG_SAY_HELLO:
                    recordView.setText(recordView.getText() + "\n" +
                            msg.getData().getString("content"));
                    break;
                case ServerService.MSG_BIND_MESSENGER:
                    recordView.setText(recordView.getText() + "\n" +
                            msg.getData().getString("content"));
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recordView = (TextView) findViewById(R.id.record);
        sayHelloBtn = (Button) findViewById(R.id.sayHello);
        sayHelloBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "client:hello, server");
                if (sMessenger != null) {
                    Message msg = Message.obtain(null, ServerService.MSG_SAY_HELLO);
                    try {
                        sMessenger.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    recordView.setText(recordView.getText() + "\n" + "client:Hi, I am client.");
                }
            }
        });
        cMessenger = new Messenger(new ClientHandler(this.getMainLooper()));
        // 启动服务端
        Intent intent = new Intent(this, ServerService.class);
        Log.d(TAG, "bind server.");
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        unbindService(serviceConnection);
        super.onDestroy();
    }
}
