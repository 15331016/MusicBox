# 服务与多线程——简单音乐播放器

### 实验目的

> * 学会使用 MediaPlayer
> * 学会简单的多线程编程，使用 Handle 更新 UI
> * 学会使用 Service 进行后台工作
> * 学会使用 Service 与 Activity 进行通信

### 实验内容

实现一个简单的播放器，要求功能有：
> * 播放、暂停，停止，退出功能
> * 后台播放功能
> * 进度条显示播放进度、拖动进度条改变进度功能
> * 播放时图片旋转，显示当前播放时间功能

### 实验过程

我的MainActivity.java主要调用函数过程如下，我将按以下顺序逐步写实验过程。
![image](https://github.com/15331016/MusicBox/raw/master/MusicBox/images/1.png)

#### 1. UI界面

![image](https://github.com/15331016/MusicBox/raw/master/MusicBox/images/2.png)

我的UI界面如上所示。在完成实验文档提供的主界面布局后，自己进行了一些修改：
> * 在res/values/styles中去掉应用标题栏，将顶部状态栏底色改为黑色。
> * 更换app图标icon。同时为主布局增加一张背景图。
> * 为button新建样式shape.xml。
> * 改变seekbar的原有样式，包括thumb颜色和进度条高度。

#### 2. SD卡动态文件读取权限申请

由于我是在Android 7.1.1的手机上进行测试运行，所以我在将mp.3文件放入指定路径后，必须先设置动态获取文件权限，在征得用户权限认可后才可读取音乐文件。

首先在Manifest里注册权限：

![image](https://github.com/15331016/MusicBox/raw/master/MusicBox/images/3.png)

然后在MainActivity.java里添加权限确认函数，请求权限会弹出询问框：

![image](https://github.com/15331016/MusicBox/raw/master/MusicBox/images/4.png)

再增加如下的回调函数，在用户进行选择后会调用该函数，若用户拒绝则直接退出程序：

![image](https://github.com/15331016/MusicBox/raw/master/MusicBox/images/5.png)

运行测试效果如下：

![image](https://github.com/15331016/MusicBox/raw/master/MusicBox/images/6.png)

#### 3. Service+MediaPlayer

先是创建一个service类，命名为MusicService.java。并且在Manifest里进行注册。
然后通过Binder来建立MainActivity.java与MusicService.java的通信。
在MainActivity.java里通过如下代码，使得activity启动时便通过bindService,从而与MusicService绑定。bindService成功后回调onServiceConnected函数，通过IBinder 获取Service对象。

![image](https://github.com/15331016/MusicBox/raw/master/MusicBox/images/7.png)

同时在MainActivity.java里重写onDestroy函数，当程序结束后，service停止服务，此时必须解除绑定。

![image](https://github.com/15331016/MusicBox/raw/master/MusicBox/images/8.png)

关于service绑定，MainActivity.Java部分讲完，接下来讲MusicService.java里的设置。
既然已经activity已经绑定service，那么我们可以将媒体文件放在service里，然后通过binder来保持通信。（以下代码皆写在MusicService.java）

![image](https://github.com/15331016/MusicBox/raw/master/MusicBox/images/9.png)

首先在service里读取sd卡里指定路径的mp3文件：

![image](https://github.com/15331016/MusicBox/raw/master/MusicBox/images/10.png)

然后通过binder里的onTransact函数来保持通信。

![image](https://github.com/15331016/MusicBox/raw/master/MusicBox/images/11.png)

上面的onTransact函数是service能够响应activity发送过来的调用请求。code是信号，data是指activity传送过来的数据，reply是指service传回给activity里的数据。
data和reply可以通过write和read写入/读取传送的值。
而在MainActivity.Java里，我们通过transact向service发送请求保持通信，通过这一机制我们便可以控制service里mp3文件的播放。以下是部分button的click事件。

![image](https://github.com/15331016/MusicBox/raw/master/MusicBox/images/12.png)

![image](https://github.com/15331016/MusicBox/raw/master/MusicBox/images/13.png)

（QUIT退出键，直接结束程序。）

至此，基本可以实现button对音乐播放的控制。

#### 4. Handler

接下来只剩进度条的部分还没完成，由于我们要时刻捕获音乐的播放进度从而改变音乐的当前时间和seekbar上thumb的位置，所以需要采取多线程thread，再通过handler来实时更新UI。我们先定义一下时间的显示格式：

![image](https://github.com/15331016/MusicBox/raw/master/MusicBox/images/14.png)

然后定义Thread和Handler，如下，实现音乐的当前时间和seekbar的实时更新。

![image](https://github.com/15331016/MusicBox/raw/master/MusicBox/images/15.png)

注意到上面代码里，要获取音乐文件的播放进度时（getCurrentPosition），本来是想通过transact进行传值回调的，但是发现，直接引用MusicService里的mediaplayer也可以实现而且方便了很多，这么说，之前的button控制播放也可以引用MusicService.mp.start()之类的来实现，这样岂不是减少了代码量？不知道transact和“直接引用”这两者有优劣之分吗？求解。

接着实现拖动进度条改变音乐播放进度的函数：

![image](https://github.com/15331016/MusicBox/raw/master/MusicBox/images/16.png)

#### 5. 补充一下ObjectAnimator

忘了提图片旋转的动画实现了，这里补充下。

![image](https://github.com/15331016/MusicBox/raw/master/MusicBox/images/17.png)

![image](https://github.com/15331016/MusicBox/raw/master/MusicBox/images/18.png)

然后通过 .start() / .pause() / .resumn()来控制图片的旋转与否。
之前我不知道.setDuration()即是通过设置动画时长从而来控制动画变化的速率，一开始里面填的是1000，结果图片转得像螺旋一样飞快，网上查了很久居然找不到设置速率的代码真是恼人，后来还是靠自己突然开窍发现的……

### 实验思考及感想

说几个实验遇到的小错误：
> * 刚开始点击stop按钮后停止音乐播放，但是再次点击PLAY的时候音乐却无法重新开始，究其原因，原来是mediaplayer设置了.stop()之后应该再顺便设置.prepare()让音乐进入就绪状态的。不过直接在.stop()后面设置了.prepare()又报错了，只得通过try catch才能成功设置。不知道是为什么。

![image](https://github.com/15331016/MusicBox/raw/master/MusicBox/images/19.png)

> * 设private IBinder mBinder时候居然漏了IBinder的首字母I，然后还设置成功了。结果下面onServiceConnected函数倒是一堆报错。找了好久才发现这个bug……


2017/11/22
