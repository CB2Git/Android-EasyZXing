package org.androideasyzxing;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import org.zxinglibrary.zxing.activity.CaptureActivity;

/**
 * 啥都不用处理，这一个Activity只是用来自定义布局而已
 */
public class ScanQrCodeActivity3 extends CaptureActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        initActivity();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr_code);
    }

    private void initActivity() {
       /* // 设置无标题栏风格
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.act_capture);
        //如果继承自AppCompatActivity，需要使用这句话消除ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }*/
        // 保持屏幕常亮
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
