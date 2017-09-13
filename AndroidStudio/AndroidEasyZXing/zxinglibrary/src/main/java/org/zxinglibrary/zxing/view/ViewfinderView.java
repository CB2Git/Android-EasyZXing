
package org.zxinglibrary.zxing.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;

import org.zxinglibrary.R;
import org.zxinglibrary.zxing.camera.CameraManager;
import org.zxinglibrary.zxingx.codeutil.DensityUtil;

import java.util.Collection;
import java.util.HashSet;

public final class ViewfinderView extends View {

    /**
     * 刷新界面的时间
     */
    private static final long ANIMATION_DELAY = 3L;

    private static final int OPAQUE = 0xFF;

    /**
     * 下面的单位均为dp/sp
     */
    /**
     * 四个绿色边角对应的长度
     */
    private int ScreenRate = 20;

    /**
     * 四个边角对应的宽度
     */
    private int CORNER_WIDTH = 3;

    /**
     * 扫描框中的中间线的宽度
     */
    private int MIDDLE_LINE_WIDTH = 1;

    /**
     * 扫描框中的中间线的与扫描框左右的间隙
     */
    private int MIDDLE_LINE_PADDING = CORNER_WIDTH;

    /**
     * 中间那条线每次刷新移动的距离
     */
    private int SPEEN_DISTANCE = 2;

    /**
     * 提示字体大小
     */
    private int TEXT_SIZE = 16;

    /**
     * 字体距离扫描框下面的距离
     */
    private int TEXT_PADDING_TOP = 13;

    /**
     * 画笔对象的引用
     */
    private Paint paint;

    /**
     * 中间滑动线的最顶端位置
     */
    private int mSlideTop;

    /**
     * 中间滑动线的最底端位置
     */
    private int mSlideBottom;

    private Bitmap resultBitmap;

    private int mMaskColor;

    private int mResultColor;

    private int mResultPointColor;

    private int mScanLineColor;

    private Collection<ResultPoint> possibleResultPoints;

    private Collection<ResultPoint> lastPossibleResultPoints;

    private boolean isFirst;

    private Context mContext;

    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        possibleResultPoints = new HashSet<ResultPoint>(5);
        loadInitData();
    }

    /**
     * 初始化预设信息
     */
    private void loadInitData() {
        ScreenRate = DensityUtil.dp2px(mContext, ScreenRate);
        TEXT_PADDING_TOP = DensityUtil.dp2px(mContext, TEXT_PADDING_TOP);
        CORNER_WIDTH = DensityUtil.dp2px(mContext, CORNER_WIDTH);
        MIDDLE_LINE_WIDTH = DensityUtil.dp2px(mContext, MIDDLE_LINE_WIDTH);
        MIDDLE_LINE_PADDING = DensityUtil.dp2px(mContext, MIDDLE_LINE_PADDING);
        SPEEN_DISTANCE = DensityUtil.dp2px(mContext, SPEEN_DISTANCE);
        TEXT_SIZE = DensityUtil.sp2px(mContext, TEXT_SIZE);
        TEXT_PADDING_TOP = DensityUtil.dp2px(mContext, TEXT_PADDING_TOP);
        Resources resources = getResources();
        mMaskColor = resources.getColor(R.color.zxing_viewfinder_mask);
        mResultColor = resources.getColor(R.color.zxing_result_view);
        mResultPointColor = resources.getColor(R.color.zxing_possible_result_points);
        mScanLineColor = resources.getColor(R.color.zxing_scan_line_color);
    }

    /**
     * 绘制扫描框样式
     */
    @Override
    public void onDraw(Canvas canvas) {
        // 中间的扫描框，你要修改扫描框的大小，去CameraManager里面修改
        Rect frame = CameraManager.get().getFramingRect();
        if (frame == null) {
            return;
        }

        // 初始化中间线滑动的最上边和最下边
        if (!isFirst) {
            isFirst = true;
            mSlideTop = frame.top;
            mSlideBottom = frame.bottom;
        }

        // 获取屏幕的宽和高
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        paint.setColor(resultBitmap != null ? mResultColor : mMaskColor);

        // 画出扫描框外面的阴影部分，共四个部分，扫描框的上面到屏幕上面，扫描框的下面到屏幕下面
        // 扫描框的左边面到屏幕左边，扫描框的右边到屏幕右边
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom, paint);
        canvas.drawRect(frame.right, frame.top, width, frame.bottom, paint);
        canvas.drawRect(0, frame.bottom, width, height, paint);

        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            paint.setAlpha(OPAQUE);
            canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);
        } else {
            // 画扫描框边上的角，总共8个部分
            paint.setColor(getResources().getColor(R.color.zxing_viewfinder_frame));
            canvas.drawRect(frame.left, frame.top, frame.left + ScreenRate,
                    frame.top + CORNER_WIDTH, paint);
            canvas.drawRect(frame.left, frame.top, frame.left + CORNER_WIDTH, frame.top
                    + ScreenRate, paint);
            canvas.drawRect(frame.right - ScreenRate, frame.top, frame.right,
                    frame.top + CORNER_WIDTH, paint);
            canvas.drawRect(frame.right - CORNER_WIDTH, frame.top, frame.right, frame.top
                    + ScreenRate, paint);
            canvas.drawRect(frame.left, frame.bottom - CORNER_WIDTH, frame.left
                    + ScreenRate, frame.bottom, paint);
            canvas.drawRect(frame.left, frame.bottom - ScreenRate,
                    frame.left + CORNER_WIDTH, frame.bottom, paint);
            canvas.drawRect(frame.right - ScreenRate, frame.bottom - CORNER_WIDTH,
                    frame.right, frame.bottom, paint);
            canvas.drawRect(frame.right - CORNER_WIDTH, frame.bottom - ScreenRate,
                    frame.right, frame.bottom, paint);

            // 绘制中间的线,每次刷新界面，中间的线往下移动SPEEN_DISTANCE
            mSlideTop += SPEEN_DISTANCE;
            if (mSlideTop >= frame.bottom) {
                mSlideTop = frame.top;
            }
            paint.setColor(mScanLineColor);
            canvas.drawRect(frame.left + MIDDLE_LINE_PADDING, mSlideTop - MIDDLE_LINE_WIDTH / 2, frame.right
                    - MIDDLE_LINE_PADDING, mSlideTop + MIDDLE_LINE_WIDTH / 2, paint);

            // 画扫描框下面的字
            paint.setColor(getResources().getColor(R.color.zxing_scan_hint_color));
            paint.setTextSize(TEXT_SIZE);
            // paint.setTypeface(Typeface.DEFAULT_BOLD);
            String text = getResources().getString(R.string.zxing_scan_hint);
            float textWidth = paint.measureText(text);
            canvas.drawText(text, (width - textWidth) / 2, (float) (frame.bottom + (float) TEXT_PADDING_TOP), paint);

            Collection<ResultPoint> currentPossible = possibleResultPoints;
            Collection<ResultPoint> currentLast = lastPossibleResultPoints;
            if (currentPossible.isEmpty()) {
                lastPossibleResultPoints = null;
            } else {
                possibleResultPoints = new HashSet<ResultPoint>(5);
                lastPossibleResultPoints = currentPossible;
                paint.setAlpha(OPAQUE);
                paint.setColor(mResultPointColor);
                for (ResultPoint point : currentPossible) {
                    canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 6.0f, paint);
                }
            }
            if (currentLast != null) {
                paint.setAlpha(OPAQUE / 2);
                paint.setColor(mResultPointColor);
                for (ResultPoint point : currentLast) {
                    canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 3.0f, paint);
                }
            }
            // 只刷新扫描框的内容，其他地方不刷新
            postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);
        }
    }

    public void drawViewfinder() {
        resultBitmap = null;
        invalidate();
    }

    /**
     * Draw a bitmap with the result points highlighted instead of the live scanning display.
     *
     * @param barcode An image of the decoded barcode.
     */
    public void drawResultBitmap(Bitmap barcode) {
        resultBitmap = barcode;
        invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
        possibleResultPoints.add(point);
    }
}
