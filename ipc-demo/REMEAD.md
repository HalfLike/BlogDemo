Android 进程间通信-Intent、Messenger、AIDL
===

​Android进程间通信（IPC，Inter-Process Communication）底层采用的是 Binder 机制，具体到应用层有网友根据安卓四大组件将进程间通信方式分为对应的四种方式 Activity, Broadcast, ContentProvider, Service。Activity可以跨进程调用其他应用程序的Activity；Content Provider可以跨进程访问其他应用程序中的数据（以Cursor对象形式返回）；Broadcast可以向android系统中所有应用程序发送广播，而需要跨进程通讯的应用程序可以监听这些广播；Service可通过 AIDL(Android Interface Definition Language) 实现跨进程通信。

本文根据实现 IPC 的具体手段，分为以下四种方式：

* 使用 Intent 通信
* 持久化数据通信（ContentProvider，本地文件等）
* 使用信使（Messenger）通信
* 使用 AIDL 通信

关于示例代码
---
下面提到的示例代码可在我的Github的[BlogDemo](https://github.com/HalfLike/BlogDemo)库中的`ipc-demo`目录下的示例工程获取。工程是用 *AndroidStudion* 创建，源代码位置 `BlogDemo/ipc-demo/[工程名]/src/main/java/`。

使用 Intent 通信
---

Intent 是安卓的一种消息传递机制，可用于 Activity/Service 交互，传送广播（Broadcast）事件到 BroadcastReceiver。当交互的 Activity/Service 在不同进程内，此时 Intent 的传递便是跨进程的，。具体使用方法有：

* 通过Context.startActivity() or Activity.startActivityForResult() 启动一个Activity；
* 通过 Context.startService() 启动一个服务，或者通过Context.bindService() 和后台服务交互；
* 通过广播方法(比如 Context.sendBroadcast(),Context.sendOrderedBroadcast(),  Context.sendStickyBroadcast()) 发给broadcast receivers。

关于 Intent 的详细内容可参考文章 [Android Activity和Intent机制学习笔记](http://www.cnblogs.com/feisky/archive/2010/01/16/1649081.html)。

通过 Intent.putExtra() 可将基本类型数据， Bundle及可序列化的对象存入 Intent 中在进程间传递。其中基本类型数据包括 double，float，byte，short，int，long，CharSequence（String, CharBuffer等的父类），char，boolean，及其对应的数组。放入 extra 的对象需要实现序列化（Bundle也实现了Parcelable）。对象的序列化有两各方式：

* 实现Java的序列化接口Serializable 
* 实现Android特有的序列化接口Parcelable

Serializable 使用简单只需要注明实现即可不需要实现额外的方法，可保存对象的属性到本地文件、数据库、网络流以方便数据传输。Parcelable 相比 Serializable 效率更高更适合移动端序列化数据传递，需要手动将类的变量打包并实现必要的方法，且实现 Parcelable 的类的变量如果是对象亦需要实现了序列化。Parcelable 序列化是存储在内存的，不能适用用保存对象到本地、网络流、数据库。具体使用方法可参考文章[android Activity之间数据传递 Parcelable和Serializable接口的使用](http://blog.csdn.net/js931178805/article/details/8268144) 及 Android文档 [Parcelable](http://developer.android.com/reference/android/os/Parcelable.html)。

持久化数据通信
---
一种间接实现安卓进程间通信的方法是持久化数据，如使用数据库，ContentProvider，本地文件，SharePreference等，将数据存储在两个程序皆可获取的地方，可以间接达到程序间通信，并不能算是真正的IPC。这种方法一般效率较低，缺乏主动通知能力，一般用于程序间的简单数据交互。

使用信使（Messenger）通信
---
Messenger 在进程间通信的方式和 Hanlder-Message 类似，Hanlder在A进程中，B进程持有A的 Messenger 通过此发送 Message 到A实现进程间通信。Messenger 是对 Binder 的简单包装。相对于 AIDL 方式，Messenger 是将请求放入消息队列中然后逐条取出处理的，而纯 AIDL 接口可以将多条请求并发传递到服务端（多线程处理请求）。如果不需要并发处理请求时，采用 Messenger 是更好的选择，这种方法更为简单易于实现，不需要额外写 AIDL，不需要考虑多线程，对于 Handler-Message 机制更为广大安卓开发者熟悉不易出错。

使用 Messenger 一般步骤如下：

* 服务端的 Service 实现一个 Handler 来处理请求。
* 用该 Handler 创建一个 Messenger。
* 在服务端返回客户端的方法 onBind() 中返回 Messenger.getBinder() 创建的 IBinder。
* 客户端通过得到的 IBinder 初始化一个 Messenger（引用了服务端的 Handler），此时客户端可通过该 Messenger 发送消息到服务端。

从上面的步骤我们知道如何将服务端的信鸽传送到客户端使客户端可以发送请求到服务端，那么服务端如何将请求送达客户端呢？当客户端获得服务端的 Messenger 后，可以很轻松的将自己的 Messenger 通过 Message 发送到服务端，使服务端可以发送请求到客户端。赋值 Message.replyTo 为客户端自己的 Messenger ，再将此 Message 发送到服务端即可。

下面是使用简单的 Demo 演示了使用 Messenger 如何跨进程通信，服务端为一个 Service，客户端为Activity。通过指定 ServerService 的 `android:process` 属性使其在另一个进程。
服务端进程代码如下：
	
```java
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
```

客户端通过 bindService() 连接服务端并获得 Messenger，代码如下：

```java
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
```

使用 AIDL 通信
---

Android 是进程间内存是分离的，因此需要将对象分解成操作系统可理解的元数据，并将此打包让操作系统帮忙传递对象到另一个进程。这个过程是十分复杂繁重的，因此 Google 定义了 AIDL（Android Interface Definition Language）帮助开发者简化工作。一般，你只有在客户端需要访问另一进程的 Service ，且需要 Service 多纯种处理客户端请求时才有必要使用 AIDL；如果只需要在进程内和 Service 通信，只需要实现 Binder 通过 onBind() 返回对象；如果需要进程间通信但不需要并发处理请求，可考虑使用 Messenger，Messenger 底层实现和 AIDL 类似，上层采用 handler-message 方式通信，更为简单易用(具体请参考上文)。用 AIDL 实现进程间通信的步骤为：

1. 创建 **.aidl** 文件
*  实现 **.aidl** 文件中定义的接口
*  向客户端曝露接口

#### 创建 **.aidl** 文件
AIDL 使用 java 的语法来定义。如果你作用的是 Eclipse，可在 `src/` 目录下创建 `.aidl` 文件，并编译，会在 `gen/` 目录下自动创建同名的 `.java` 文件。下面通过三个示例说明 AIDL 的用法。

```java
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
```

```java
// QCMessag.aidl
package com.halflike.aidlcommon;

parcelable QCEmail;

```

```java
// ICallback.aidl
package com.halflike.aidlcommon;

// Declare any non-default types here with import statements

oneway interface ICallback {

    // 服务端通知客户端收到一封邮件
    void receiveEmail();

}
```

AIDL 接口定义和 java 类似，但对参数、返回值的类型有限制。AIDL 支持的数据类型有：

* java 所有的基本数据类型，包括 **boolean** 和数值类型（int, char, float等）；
* String，CharSequence，List，Map。List 和 Map 中存储的元素也需要是 AIDL 支持的基本数据类型，或 AIDL 文件定义的接口，或已声明的 pacelable 类。

`QCEmail.aidl`声明了一个序列化的类，可在 `src/` 目录相同包名下实现 `QCEmail.java`，这样其他 `.aidl` 文件只要 **import** 此类即可用于进程间传送此类对象。

`ICallback.aidl` 定义的接口有 `oneway` 修饰，作用是使此接口的调用变为非阻塞的。例如，示例中 server 调用此接口通知 client 接收到邮件，server 不用关心 client 后续的操作不需要等待 client，用 `oneway` 修饰后可以达到这样的效果，不被 client 阻塞继续运行。

还有一点示例并未涉及，所有非基本数据类型的参数**必须指明其方向**，关键词为 `in`，`out`，`inout`.
非基本数据类型即为引用类型，我们知道在 java 中引用类型作参数时，即可以传递引用对象的值也可以改变引用对象的值。而在 AIDL 中将参数打包传递开销是很大的，十分有必须指明参数的方向。比如，server 传递信息到 client 并不需要同步 client 做的修改时可指定为 `in` 方向：

```java
interface ICallToClient {
	void startDownload(in Address adr);
}
```

#### 实现 **.aidl** 文件中定义的接口

`.aidl` 文件在编译后会生成对应的 `.java`文件，生成的文件包含一个实现了 `IBinder` 的内部类 `Stub`，实现了 `Stub` 的对象可在两个进程间传递。一般是在服务端的 service 的 `onBind()` 方法传递给客户端。关键代码如下示例：

```java
//ServerService.java

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
```



#### 向客户端曝露接口

当客户端通过 `bindService()` 连接服务端 service 时，客户端的 `onServiceConnected()` 方法会收到服务端 `onBind()` 方法返回的 `mBinder`，再通过对应接口的`YourServiceInterface.Stub.asInterface(service)`转换为 AIDL 中定义的接口 `YourServiceInterface`。关键代码如下：

```java
//ClientActivity.java

IServer mServer = null;

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
```

参考 
---
[android Activity之间数据传递 Parcelable和Serializable接口的使用](http://blog.csdn.net/js931178805/article/details/8268144) 

[Android Activity和Intent机制学习笔记](http://www.cnblogs.com/feisky/archive/2010/01/16/1649081.html)

[Intent](http://developer.android.com/reference/android/content/Intent.html)

[Using a Messenger](http://developer.android.com/guide/components/bound-services.html#Messenger)

[Android Interface Definition Language (AIDL)](http://developer.android.com/intl/zh-cn/guide/components/aidl.html)

