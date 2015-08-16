// IServer.aidl
package com.halflike.aidlcommon;

// Declare any non-default types here with import statements
import com.halflike.aidlcommon.QCEmail;
import com.halflike.aidlcommon.ICallback;

interface IServer {

    // 获取邮件内容
    QCEmail getEmail();
    // 客户端注册回调，用于服务端 service 主动通知客户端
    boolean registeCallback(ICallback callback);
    boolean unregisteCallback();

}
