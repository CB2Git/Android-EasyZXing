
package com.demo.zxingt;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.demo.easyzxing.R;
import com.zxing.activity.CaptureActivity;
import com.zxingx.codeutil.DensityUtil;
import com.zxingx.codeutil.QRDecodeUtil;
import com.zxingx.codeutil.QREncodeUtil;
import com.zxingx.result.ZXScanResult;

public class MainActivity extends Activity implements OnClickListener {
    private Button mScanBtn;

    private Button mCreateBtn;

    private Button mCreateLogoBtn;

    private Button mScanLocalImage;

    private EditText mText;

    private ImageView QRImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindView();
    }

    private void bindView() {
        mScanBtn = (Button) findViewById(R.id.main_scan_btn);
        mCreateBtn = (Button) findViewById(R.id.main_create_btn);
        mCreateLogoBtn = (Button) findViewById(R.id.main_create_logo_btn);
        mScanLocalImage = (Button) findViewById(R.id.main_scan_local_btn);
        QRImg = (ImageView) findViewById(R.id.main_qr_code);
        mText = (EditText) findViewById(R.id.main_text);
        mScanBtn.setOnClickListener(this);
        mCreateBtn.setOnClickListener(this);
        mCreateLogoBtn.setOnClickListener(this);
        mScanLocalImage.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.main_scan_btn) {
            scan();
        }
        if (v.getId() == R.id.main_create_btn) {
            create();
        }
        if (v.getId() == R.id.main_create_logo_btn) {
            createWithLogo();
        }
        if (v.getId() == R.id.main_scan_local_btn) {
            scanLocal();
        }
    }

    /**
     * 生成带logo的二维码
     */
    private void createWithLogo() {
        String QRText = mText.getText().toString();
        Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        Bitmap qrImage = QREncodeUtil.createQRImage(QRText, DensityUtil.dp2px(this, 225), DensityUtil.dp2px(this, 225),
                logo);
        if (qrImage != null) {
            QRImg.setImageBitmap(qrImage);
        } else {
            Toast.makeText(this, "生成错误啦", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 生成不带logo的二维码
     */
    private void create() {
        String QRText = mText.getText().toString();
        Bitmap qrImage = QREncodeUtil.createQRImage(QRText, DensityUtil.dp2px(this, 225), DensityUtil.dp2px(this, 225),
                null);
        if (qrImage != null) {
            QRImg.setImageBitmap(qrImage);
        } else {
            Toast.makeText(this, "生成错误啦", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 扫描二维码
     */
    private void scan() {
        Intent intent = new Intent(this, CaptureActivity.class);
        startActivityForResult(intent, ZXScanResult.SCAN);
    }

    /**
     * 扫描本地二维码
     */
    private void scanLocal() {
        // 启动系统相册选择本地图片
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, ZXScanResult.SCAN_LOCAL);
    }

    /**
     * 结果回调
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 使用摄像头扫描
        if (requestCode == ZXScanResult.SCAN) {
            if (resultCode == ZXScanResult.SCAN_SUCCESS) {
                Bundle bundle = data.getExtras();
                String qrtext = bundle.getString(ZXScanResult.SCAN_RESULT);
                // 显示扫描到的内容
                Toast.makeText(this, qrtext, Toast.LENGTH_SHORT).show();
            }
            else if (resultCode == ZXScanResult.SCAN_CANCEL) {
                Toast.makeText(this, "用户取消了扫描", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "错误啦", Toast.LENGTH_SHORT).show();
            }
        }
        // 扫描的是本地图片
        else if (requestCode == ZXScanResult.SCAN_LOCAL && resultCode == ZXScanResult.SCAN_SUCCESS) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            String resultString = QRDecodeUtil.decodeLocalImage(picturePath);
            Log.i("TAG", "picturePath = " + picturePath);
            if (resultString == null) {
                Toast.makeText(getApplicationContext(), "解析错误，请选择正确的二维码图片", Toast.LENGTH_LONG).show();
            } else {
                if (resultString.equals("")) {
                    Toast.makeText(this, "扫描失败", Toast.LENGTH_SHORT).show();
                } else {
                    // 显示扫描到的内容
                    Toast.makeText(this, resultString, Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "用户取消扫描或者扫描失败", Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
