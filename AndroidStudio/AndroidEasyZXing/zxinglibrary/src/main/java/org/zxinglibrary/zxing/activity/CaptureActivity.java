
package org.zxinglibrary.zxing.activity;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import org.zxinglibrary.R;
import org.zxinglibrary.zxing.camera.CameraManager;
import org.zxinglibrary.zxing.decoding.CaptureActivityHandler;
import org.zxinglibrary.zxing.decoding.InactivityTimer;
import org.zxinglibrary.zxing.decoding.RGBLuminanceSource;
import org.zxinglibrary.zxing.view.ViewfinderView;
import org.zxinglibrary.zxingx.result.ZXScanResult;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

/*
    <!-- 震动权限 -->
    <uses-permission android:name="android.permission.VIBRATE" />
    <!-- 使用照相机权限 -->
    android 6.0(targetSdkVersion >= 23) 需要动态申请权限
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-feature android:name="android.hardware.camera" />
    <!-- 自动聚焦权限 -->
    <uses-feature android:name="android.hardware.camera.autofocus" />
*/
public class CaptureActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private static String TAG = "CaptureActivity";

    private boolean hasSurface;

    private String characterSet;

    /**
     * 扫描框的视图
     */
    private ViewfinderView viewfinderView;

    private CaptureActivityHandler handler;

    private Vector<BarcodeFormat> decodeFormats;

    private InactivityTimer inactivityTimer;

    /**
     * 扫描成功以后是否震动一下提醒用户
     */
    private boolean vibrate = true;

    private boolean isInit = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void initCaptureActivity() {
        CameraManager.init(getApplication());
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
        if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
            hasSurface = false;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 10);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isInit) {
            initCaptureActivity();
            isInit = true;
        }
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        //如果没有摄像机权限，那么将SurfaceView隐藏下，避免黑屏
        if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
            surfaceView.setVisibility(View.INVISIBLE);
        } else {
            surfaceView.setVisibility(View.VISIBLE);
        }
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
        }
        decodeFormats = null;
        characterSet = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == ZXScanResult.SCAN) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasSurface = true;
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.zxing_tip);
                    builder.setMessage(R.string.zxing_must_granted);
                    builder.setCancelable(false);
                    builder.setNegativeButton(R.string.zxing_reject, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    builder.setPositiveButton(R.string.zxing_retry, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(CaptureActivity.this, new String[]{Manifest.permission.CAMERA}, 10);
                        }
                    });
                    builder.show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.zxing_tip);
                    builder.setMessage(R.string.zxing_no_granted);
                    builder.setCancelable(false);

                    builder.setPositiveButton(R.string.zxing_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    builder.show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    /**
     * Handler scan result
     *
     * @param result
     * @param barcode 获取结果
     */
    public void handleDecode(Result result, Bitmap barcode) {
        inactivityTimer.onActivity();
        makePrompt();
        String resultString = result.getText();
        Log.i(TAG, "scan result:" + resultString);
        Intent resultIntent = new Intent();
        if (TextUtils.isEmpty(resultString)) {
            this.setResult(ZXScanResult.SCAN_ERROR, resultIntent);
        } else {
            Bundle bundle = new Bundle();
            bundle.putString(ZXScanResult.SCAN_RESULT, resultString);
            resultIntent.putExtras(bundle);
            this.setResult(ZXScanResult.SCAN_SUCCESS, resultIntent);
        }
        if (doHandlerMsg(resultString)) {
            finish();
        }
    }

    /**
     * 处理扫描结果，返回true,表示finish此Activity
     *
     * @param result
     * @return
     */
    protected boolean doHandlerMsg(String result) {
        return true;
    }

    /**
     * 重新进行扫描
     */
    protected void reSweep() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (handler != null) {
                    handler.restartPreviewAndDecode();
                }
            }
        });
    }


    /**
     * 设置震动
     */
    protected void setVibrate(boolean vibrate) {
        this.vibrate = vibrate;
    }

    private static final long VIBRATE_DURATION = 200L;

    /**
     * 当扫描出结果以后的提示,默认震动一下
     */
    private void makePrompt() {
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * 解析QR图内容
     */
    public static String scanLocalImage(String picturePath) {

        if (TextUtils.isEmpty(picturePath)) {
            return null;
        }

        Map<DecodeHintType, String> hints1 = new Hashtable<DecodeHintType, String>();
        hints1.put(DecodeHintType.CHARACTER_SET, "utf-8");

        // 获得待解析的图片
        Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
        RGBLuminanceSource source = new RGBLuminanceSource(bitmap);
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        Result result;
        try {
            result = reader.decode(bitmap1, hints1);
            return result.getText();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;

    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }
}
