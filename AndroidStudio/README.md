### 简化版Zxing for Android(Android Studio Library)


#### demo截图

![demo Screenshot](/Screenshot/Screenshot.png)

#### demo apk 

[EasyZXingDemo.apk](/Screenshot/EasyZXingDemo.apk)


创建自己的Activity继承自`org.zxinglibrary.zxing.activity.CaptureActivity`

然后在Activity的布局中如下设置即可，其他具体功能请查看Demo

    <?xml version="1.0" encoding="utf-8"?>
	<FrameLayout
	    xmlns:android="http://schemas.android.com/apk/res/android"
	    xmlns:tools="http://schemas.android.com/tools"
	    android:layout_width="match_parent"
	    android:layout_height="match_parent"
	    tools:context="org.androideasyzxing.ScanQrCodeActivity">
	
	    <include layout="@layout/act_capture"/>
	
	</FrameLayout>


+ 兼容Android 6.0的权限动态申请

+ 扫描灵敏度提升N倍(不在扫描框中也可以扫描)

+ 加入无限扫描模式