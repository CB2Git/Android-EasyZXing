package org.androideasyzxing;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.zxinglibrary.zxing.activity.CaptureActivity;

/**
 * 无限扫描
 */
public class ScanQrCodeActivity2 extends CaptureActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        initActivity();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr_code2);
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

    /**
     * 处理扫描结果
     *
     * @param result 是否结束本此扫描(关闭Activity)
     * @return true, 关闭Activity，false,不关闭，调用reSweep()继续扫描
     */
    @Override
    protected boolean doHandlerMsg(String result) {
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        reSweep();
        //如果震动很频繁，那么使用下面的函数关闭震动
        //setVibrate(false);
        return false;
    }
}
