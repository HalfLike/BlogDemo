// ICallback.aidl
package com.halflike.aidlcommon;

// Declare any non-default types here with import statements

oneway interface ICallback {

    // 服务端通知客户端收到一封邮件
    void receiveEmail();

}
