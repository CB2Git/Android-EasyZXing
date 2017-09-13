
package org.zxinglibrary.zxingx.result;

/**
 * Zxing 扫描的结果返回码
 */
public class ZXScanResult {

    /**
     * 扫描出现错误(扫描结果为空)
     */
    public static int SCAN_ERROR = 1;

    /**
     * 扫描成功，和{@link android.app.Activity.RESULT_OK}等同
     */
    public static int SCAN_SUCCESS = -1;

    /**
     * 取消了扫描，和{@link android.app.Activity.RESULT_CANCELED}等同
     */
    public static int SCAN_CANCEL = 0;

    /**
     * 扫描请求码
     */
    public static int SCAN = 10;

    /**
     * 扫描相册中二维码请求码
     */
    public static int SCAN_LOCAL = 11;

    /**
     * 获取扫描结果的key值
     */
    public static String SCAN_RESULT = "scan_result";
}
