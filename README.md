### 简化版Zxing for Android



#### 针对人群

* 不想各种百度用法，最好有一个demo，想怎么copy就怎么copy
* 不想找了半天，结果资源不完整，浪费时间
* 不想各种修改，最好完全是独立的模块，粘贴到项目中就可以使用
* 懒得看源码，希望最好有一个详细文档

#### demo截图

![demo Screenshot](/Screenshot/Screenshot.png)

#### demo apk 

[EasyZXingDemo.apk](/需要的文件/EasyZXingDemo.apk)


#### demo项目结构

[/src/com/demo/zxingt](/src/com/demo/zxingt)下为demo Activity

[/src/com/zxing/](/src/com/zxing/)下为ZXing主要的功能类

[/com/zxingx/](/com/zxingx/)下主要为衍生的工具类，包括二维码生成，尺寸工具类和响应码约定类

#### 集成方法

* 将[此目录](/需要的文件/)下的文件依次复制到你的项目中，请自行修改R文件的引用为你的项目
* 参考demo中的[MainActivity.java](/src/com/demo/zxingt/MainActivity.java)查看你所需要的功能，然后根据需要拷贝。

#### 使用

* 生成不带logo二维码

参考`com.demo.zxingt.MainActivity.create()`方法

* 生成带logo的二维码

参考`com.demo.zxingt.MainActivity.createWithLogo()`方法

* 扫描二维码

参考`com.demo.zxingt.MainActivity.createWithLogo()`方法，返回值回调参考`com.demo.zxingt.MainActivity.onActivityResult`方法

* 扫描相册中的二维码

参考`com.demo.zxingt.MainActivity.scanLocal`方法，返回值回调参考`com.demo.zxingt.MainActivity.onActivityResult``方法，为了结构清晰，我将扫描本地二维码从*com.zxing.activity.CaptureActivity`中单独抽离出来了

#### 自定义

* **自定义扫描框大小以及位置**

`com.zxing.camera.CameraManager.configManager#getFramingRect()`方法的返回值用来设定扫描框的大小以及位置，默认情况下为*居中*，宽度默认的是屏幕宽度的60%，高度相对于宽度，默认是宽度的90%

* **自定义扫描框的样式**

如果你觉得默认的扫描框样式不好看，可以自行在`com.zxing.view.ViewfinderView#onDraw()`方法中绘制你所需要的样式。

* **自定义扫描界面**

扫描界面为`com.zxing.activity.CaptureActivity`，界面布局为`\res\layoutact_capture.xml`，请不要改变`SurfaceView`和`com.zxing.view.ViewfinderView`的大小，**不然会导致界面拉伸从而无法扫描**，正确做法是使用**RelativeLayout**or**FrameLayout**在界面的*上方*放置你所需要显示的控件，比如开启闪光灯按钮

#### 注意

由于二维码扫描需要使用到相机权限，所以如果你的项目*targetSdkVersion>=23 Android Version >= 6.0*,请使用动态申请相机权限.
