
package com.zxingx.codeutil;

import java.util.Hashtable;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.zxing.decoding.RGBLuminanceSource;

/**
 * 解析本地二维码图片
 */
public class QRDecodeUtil {

    private static String QR_CHARACTER_SET = "utf-8";

    /**
     * 解析本地二维码图片
     * 
     * @param localPath 本地图片的路径
     * @return 如果解析失败返回null
     */
    public static String decodeLocalImage(String localPath) {
        if (TextUtils.isEmpty(localPath)) {
            return null;
        }
        Map<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
        hints.put(DecodeHintType.CHARACTER_SET, QR_CHARACTER_SET);

        // 获得待解析的图片
        Bitmap bitmap = BitmapFactory.decodeFile(localPath);
        RGBLuminanceSource source = new RGBLuminanceSource(bitmap);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        Result result;
        try {
            result = reader.decode(binaryBitmap, (Hashtable<DecodeHintType, String>) hints);
            return result.getText();
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }
        return null;
    }
}
