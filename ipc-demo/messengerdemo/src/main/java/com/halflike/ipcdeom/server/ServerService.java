package com.halflike.ipcdeom.server;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;


/**
 * Created by luox on 15/8/10.
 */
public class ServerService extends Service {

    static final String TAG = "halflike";

    public static final int MSG_BIND_MESSENGER = 0;
    public static final int MSG_SAY_HELLO = 1;
    public static final int MSG_ARE_YOU_OK = 2;

    Messenger sMessenger = null;
    Messenger cMessenger = null;

    @Override
    public void onCreate() {
        super.onCreate();
        // 创建服务端信使
        sMessenger = new Messenger(new ServerHandler(this.getMainLooper()));
        Log.d(TAG, "server stated.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sMessenger.getBinder();
    }

    class ServerHandler extends Handler {

        public ServerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_BIND_MESSENGER:
                    // 获得客户端信使
                    cMessenger = msg.replyTo;
                    if (cMessenger != null) {
                        Bundle data = new Bundle();
                        data.putString("content", "server:connet succeed!");
                        Message msg2c = Message.obtain(null, MSG_BIND_MESSENGER);
                        msg2c.setData(data);
                        try {
                            cMessenger.send(msg2c);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case MSG_SAY_HELLO:
                    if (cMessenger != null) {
                        Bundle data = new Bundle();
                        data.putString("content", "server:Hello, I am Server!");
                        Message msg2c = Message.obtain(null, MSG_SAY_HELLO);
                        msg2c.setData(data);
                        try {
                            cMessenger.send(msg2c);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }

        }
    }

}
