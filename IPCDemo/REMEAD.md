Android 进程间通信-Intent、Messenger、AIDL
===

​Android进程间通信（IPC，Inter-Process Communication）底层采用的是 Binder 机制，具体到应用层有网友根据安卓四大组件将进程间通信方式分为对应的四种方式 Activity, Broadcast, ContentProvider, Service。Activity可以跨进程调用其他应用程序的Activity；Content Provider可以跨进程访问其他应用程序中的数据（以Cursor对象形式返回）；Broadcast可以向android系统中所有应用程序发送广播，而需要跨进程通讯的应用程序可以监听这些广播；Service可通过 AIDL(Android Interface Definition Language) 实现跨进程通信。

本文根据实现 IPC 的具体手段，分为以下四种方式：

* 使用 Intent 通信
* 持久化数据通信（ContentProvider，本地文件等）
* 使用信使（Messenger）通信
* 使用 AIDL 通信

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




使用 AIDL 通信
---

参考 
---
[Android Activity和Intent机制学习笔记](http://www.cnblogs.com/feisky/archive/2010/01/16/1649081.html)

[Intent 官方文档](http://developer.android.com/reference/android/content/Intent.html)

