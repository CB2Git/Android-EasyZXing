
package com.zxing.activity;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.demo.easyzxing.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.zxing.camera.CameraManager;
import com.zxing.decoding.CaptureActivityHandler;
import com.zxing.decoding.InactivityTimer;
import com.zxing.decoding.RGBLuminanceSource;
import com.zxing.view.ViewfinderView;
import com.zxingx.result.ZXScanResult;

//<!-- 震动权限 -->
//<uses-permission android:name="android.permission.VIBRATE" />  
// <!-- 使用照相机权限 -->  
// android 6.0(targetSdkVersion >= 23) 需要动态申请权限
// <uses-permission android:name="android.permission.CAMERA" />
//<uses-feature android:name="android.hardware.camera" />
//<!-- 自动聚焦权限 -->
//<uses-feature android:name="android.hardware.camera.autofocus" />   
public class CaptureActivity extends Activity implements Callback {

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置无标题栏风格
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.act_capture);
        // 保持屏幕常亮
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        CameraManager.init(getApplication());
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
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
        // TODO 这里做当出现扫描结果的时候需要的提醒，默认为震动一下(需要震动权限)
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
        CaptureActivity.this.finish();
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

    // TODO
    /*
     * 获取带二维码的相片进行扫描
     */
    public void pickPictureFromAblum(View v) {
        Intent mIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(mIntent, 1);

    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent) 对相册获取的结果进行分析
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1:
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {
                            MediaStore.Images.Media.DATA
                    };

                    Cursor cursor = getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    cursor.close();

                    Result resultString = scanningImage1(picturePath);
                    if (resultString == null) {
                        Toast.makeText(getApplicationContext(), "解析错误，请选择正确的二维码图片", Toast.LENGTH_LONG).show();
                    } else {
                        String resultImage = resultString.getText();
                        if (resultImage.equals("")) {
                            Toast.makeText(CaptureActivity.this, "扫描失败", Toast.LENGTH_SHORT).show();
                        } else {
                            Intent resultIntent = new Intent();
                            Bundle bundle = new Bundle();
                            bundle.putString("result", resultImage);
                            resultIntent.putExtras(bundle);
                            CaptureActivity.this.setResult(RESULT_OK, resultIntent);
                        }
                        CaptureActivity.this.finish();
                    }
                    break;
                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 解析QR图内容
     * 
     * @return
     */
    // 解析QR图片
    private Result scanningImage1(String picturePath) {

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

            result = reader.decode(bitmap1, (Hashtable<DecodeHintType, String>) hints1);
            return result;
        } catch (NotFoundException e) {
            Toast.makeText(CaptureActivity.this, "解析错误，请选择正确的二维码图片",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (ChecksumException e) {
            Toast.makeText(CaptureActivity.this, "解析错误，请选择正确的二维码图片",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (FormatException e) {
            Toast.makeText(CaptureActivity.this, "解析错误，请选择正确的二维码图片",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return null;
    }

    // 关闪光灯
    // CameraManager.get().closeLight();
    // 开闪光灯
    // CameraManager.get().openLight();

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
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
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
