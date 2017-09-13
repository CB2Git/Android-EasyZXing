package org.androideasyzxing;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.zxinglibrary.zxing.activity.CaptureActivity;
import org.zxinglibrary.zxingx.codeutil.DensityUtil;
import org.zxinglibrary.zxingx.codeutil.QREncodeUtil;
import org.zxinglibrary.zxingx.result.ZXScanResult;

/**
 * 对于闪光灯的控制，由于个人觉得库的功能不能太耦合，所以此功能没有实现，仅仅提供二维码扫描的界面而已
 * </p>
 * 还有如果想使用回调的形式，其实是兼容的
 */
public class MainActivity extends AppCompatActivity {

    private EditText mQrCode;

    private ImageView mQrCodeImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mQrCode = (EditText) findViewById(R.id.main_qr_code);
        mQrCodeImage = (ImageView) findViewById(R.id.main_qr_code_image);
    }

    /**
     * 只扫描一次二维码
     */
    public void onScanQRCode(View view) {
        Intent intent = new Intent(this, ScanQrCodeActivity.class);
        startActivity(intent);
    }

    /**
     * 多次扫描二维码
     */
    public void onScanQRCode2(View view) {
        Intent intent = new Intent(this, ScanQrCodeActivity2.class);
        startActivity(intent);
    }

    /**
     * 使用回调的形式处理二维码
     */
    public void onScanQRCode3(View view) {
        Intent intent = new Intent(this, ScanQrCodeActivity3.class);
        startActivityForResult(intent, ZXScanResult.SCAN);
    }

    /**
     * 扫描本地二维码(需要申请权限android.permission.READ_EXTERNAL_STORAGE)
     *
     * @param view
     */
    public void onScanQRCodeLocal(View view) {
        if (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Intent mIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(mIntent, 1);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 11);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 11) {
            if (PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                onScanQRCodeLocal(null);
            } else {
                Toast.makeText(this, "没有权限读取相册", Toast.LENGTH_LONG).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ZXScanResult.SCAN) {
            handlerScan(resultCode, data);
        }
        if (requestCode == 1) {
            //需要申请权限android.permission.READ_EXTERNAL_STORAGE
            handlerScanLocal(resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 处理回调形式调用的扫描二维码
     */
    private void handlerScan(int resultCode, Intent data) {
        if (resultCode == ZXScanResult.SCAN_SUCCESS) {
            Bundle bundle = data.getExtras();
            String qrtext = bundle.getString(ZXScanResult.SCAN_RESULT);
            // 显示扫描到的内容
            Toast.makeText(this, qrtext, Toast.LENGTH_SHORT).show();
        } else if (resultCode == ZXScanResult.SCAN_CANCEL) {
            Toast.makeText(this, "用户取消了扫描", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "错误啦", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 处理本地相册选择回调
     */
    private void handlerScanLocal(int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "取消选取图片", Toast.LENGTH_SHORT).show();
        }
        if (resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {
                    MediaStore.Images.Media.DATA
            };

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            //获取图片路径
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            //使用这个方法将图片路径装换为二维码文字
            String s = CaptureActivity.scanLocalImage(picturePath);
            if (TextUtils.isEmpty(s)) {
                Toast.makeText(this, "解析失败或者没有二维码", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
            }

        }
    }


    /**
     * 创建不含图标的二维码
     */
    public void onCreateQRCode1(View view) {
        String QRText = mQrCode.getText().toString();
        Bitmap qrImage = QREncodeUtil.createQRImage(QRText, DensityUtil.dp2px(this, 225), DensityUtil.dp2px(this, 225), null);
        if (qrImage != null) {
            mQrCodeImage.setImageBitmap(qrImage);
        } else {
            Toast.makeText(this, "生成错误啦", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 创建含图标的二维码
     */
    public void onCreateQRCode2(View view) {
        String QRText = mQrCode.getText().toString();
        Bitmap logo = BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_launcher);
        Bitmap qrImage = QREncodeUtil.createQRImage(QRText, DensityUtil.dp2px(this, 225), DensityUtil.dp2px(this, 225), logo);
        if (qrImage != null) {
            mQrCodeImage.setImageBitmap(qrImage);
        } else {
            Toast.makeText(this, "生成错误啦", Toast.LENGTH_SHORT).show();
        }
    }

}
