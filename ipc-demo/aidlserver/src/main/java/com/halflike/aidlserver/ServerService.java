package com.halflike.aidlserver;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.halflike.aidlcommon.ICallback;
import com.halflike.aidlcommon.IServer;
import com.halflike.aidlcommon.QCEmail;

import java.util.Timer;
import java.util.TimerTask;

public class ServerService extends Service {

    public static final String TAG = "halflike";

    private ICallback mCallback = null;
    private QCEmail mNewEamil = new QCEmail();

    @Override
    public void onCreate() {
        super.onCreate();
        // 每5秒产生一封新邮件，并通知客户端
        mTimer.schedule(productEmail, 0, 5000);
        Log.d(TAG, "ServerService onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IServer.Stub mBinder = new IServer.Stub() {
        @Override
        public QCEmail getEmail() throws RemoteException {
            return mNewEamil;
        }

        @Override
        public boolean registeCallback(ICallback callback) throws RemoteException {
            mCallback = callback;
            if (mCallback != null) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean unregisteCallback() throws RemoteException {
            mCallback = null;
            return true;
        }
    };

    // 通过定时产生新邮件模拟服务端接收到邮件
    private Timer mTimer = new Timer(true);
    private TimerTask productEmail = new TimerTask() {
        @Override
        public void run() {
            mNewEamil.fromWho = "Anny";
            mNewEamil.toWho = "Bob";
            mNewEamil.content = "Hello Bob. I like you. Can you be My Boyfriend?";
            mNewEamil.creatTime = System.currentTimeMillis();
            if (mCallback != null) {
                try {
                    mCallback.receiveEmail();
                    Log.d(TAG, "notify client");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    };

}
