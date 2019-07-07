package com.home.roundturntabledemo.view.component;

import android.content.Context;
import android.graphics.*;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import com.home.roundturntabledemo.R;
import com.home.roundturntabledemo.data.RoundTurntableData;

import java.util.List;

public class RoundTurntableView extends View {

    private final Object obj = new Object();
    private int mDrawWidth; // 控件要繪製的寬度
    private boolean hasMeasured = false; // 避免重複計算寬度
    private List<RoundTurntableData> bitInfos;
    private int mItemCount; // 選項的個數
    private Canvas mCanvas; // 保存的畫布
    // 非選中和選擇的顏色色塊
    private int[] defaultColors = new int[]{
            0xFF5398D9,
            0xFFF4E3B1,
            0xFFA53A3B,
            0xFF5398D9,
            0xFFF4E3B1,
            0xFFA53A3B
    };
    private int[] resultColors = new int[]{
            0xFF14325C,
            0xFF14325C,
            0xFF14325C,
            0xFF14325C,
            0xFF14325C,
            0xFF14325C
    };
    private RectF mRange = new RectF(); // 繪製盤塊的範圍
    private Paint mArcPaint; // 繪製盤塊的畫筆
    private TextPaint mTextPaint; // 繪製文字的畫筆
    private Paint mBackColorPaint; // 圓形背景的畫筆
    private Paint mBackColorPaint2; // 圓形背景的畫筆2
    private Paint mBackColorPaint3; // 圓形背景的畫筆2
    private int mCenter; // 控件的中心位置, 處於中心位置, x和y是相等的
    private float mBackColorWidth = 780; // 圓形背景的寬度, 這邊是在1080p下的780
    private float mRangeWidth = 730; // 內圈畫盤小圓的寬度, 這邊是在1080p下的730
    private float mLitterBitWidth = 115; // 裡面的小圖大小, 這邊是1080p下的115
    private float mTextSize = dip2px(9); // 文字的大小
    private Bitmap mCheckBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_background); // 中心圖片信息
    private float mCheckBitmapWidth = 1; // 1080p下的270
    private RectF mCheckBitmapRect = new RectF(); // 圖片繪製區域
    private boolean isResult = false;
    private int resultPosition;

    public RoundTurntableView(Context context) {
        this(context, null);
    }

    public RoundTurntableView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 設置控件為正方形
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = Math.min(getMeasuredWidth(), getMeasuredHeight()); // Math.min(): 求兩數中最小
        mCenter = width / 2; // 中心點
        mDrawWidth = width;
        setMeasuredDimension(width, width); // 屏幕的寬度
        init();
    }

    /**
     * 根據設定的比例去調整原有的寬度
     */
    private float getDrawWidth(float width) {
        return width * mDrawWidth / 950f;
    }

    public void setBitInfos(List<RoundTurntableData> bitInfos) {
        this.bitInfos = bitInfos;
        mItemCount = this.bitInfos.size();
        onDrawInvalidate();
    }

    public void setResult(int resultPosition) {
        isResult = true;
        this.resultPosition = resultPosition;
        onDrawInvalidate();
    }

    public void removeResult() {
        isResult = false;
        onDrawInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.mCanvas = canvas;
        drawCanvas();
    }

    private void init() {
        if (!hasMeasured) {
            // 得到各個比例下的圖片大小
            mBackColorWidth = getDrawWidth(mBackColorWidth);
            mRangeWidth = getDrawWidth(mRangeWidth);
            mCheckBitmapWidth = getDrawWidth(mCheckBitmapWidth);
            mLitterBitWidth = getDrawWidth(mLitterBitWidth);
            mTextSize = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP, mTextSize / 3, getResources().getDisplayMetrics());
            hasMeasured = true;
        }
        // 初始化繪製圓弧的畫筆
        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);
        mArcPaint.setDither(true);
        // 初始化繪製文字的畫筆
        mTextPaint = new TextPaint();
        mTextPaint.setColor(0xFF14325C);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setDither(true);
        // 圓形背景的畫筆
        mBackColorPaint = new Paint();
        mBackColorPaint.setColor(0xFFD96B0C);
        mBackColorPaint.setAntiAlias(true);
        mBackColorPaint.setDither(true);
        // 圓形背景的畫筆2
        mBackColorPaint2 = new Paint();
        mBackColorPaint2.setColor(0xFF14325C);
        mBackColorPaint2.setAntiAlias(true);
        mBackColorPaint2.setDither(true);
        // 圓形背景的畫筆3
        mBackColorPaint3 = new Paint();
        mBackColorPaint3.setColor(0xFFA53A3B);
        mBackColorPaint3.setAntiAlias(true);
        mBackColorPaint3.setDither(true);
        // 未選中的分割線的畫筆
        // 繪製分割線的畫筆
        Paint mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setDither(true);
        mLinePaint.setStrokeWidth(dip2px(1f));
        mLinePaint.setStyle(Paint.Style.FILL);
        mLinePaint.setColor(0x00ff9406);
        // 內圈畫盤
        mRange = new RectF(
                mCenter - mRangeWidth / 2, mCenter - mRangeWidth / 2,
                mCenter + mRangeWidth / 2, mCenter + mRangeWidth / 2);
        // 中心圖片繪製區域
        mCheckBitmapRect = new RectF(
                mCenter - mCheckBitmapWidth / 2, mCenter - mCheckBitmapWidth / 2,
                mCenter + mCheckBitmapWidth / 2, mCenter + mCheckBitmapWidth / 2);
    }

    /**
     * 對每個選項進行繪製
     */
    private Bitmap getDrawItemBitmap(float tmpAngle, float sweepAngle, int position) {
        boolean needToNew = false; // 是否需要重新繪製
        if (bitInfos.get(position).info.itemBitmap == null || needToNew|| isResult || !isResult) {
            if (!isResult) {
                switch (position) { // 選擇背景顏色
                    case 0:
                        mArcPaint.setColor(defaultColors[position]);
                        break;
                    case 1:
                        mArcPaint.setColor(defaultColors[position]);
                        break;
                    case 2:
                        mArcPaint.setColor(defaultColors[position]);
                        break;
                    case 3:
                        mArcPaint.setColor(defaultColors[position]);
                        break;
                    case 4:
                        mArcPaint.setColor(defaultColors[position]);
                        break;
                    case 5:
                        mArcPaint.setColor(defaultColors[position]);
                        break;
                }
            } else {
                switch (position) { // 選擇背景顏色
                    case 0:
                        mArcPaint.setColor(resultColors[position]);
                        break;
                    case 1:
                        mArcPaint.setColor(resultColors[position]);
                        break;
                    case 2:
                        mArcPaint.setColor(resultColors[position]);
                        break;
                    case 3:
                        mArcPaint.setColor(resultColors[position]);
                        break;
                    case 4:
                        mArcPaint.setColor(resultColors[position]);
                        break;
                    case 5:
                        mArcPaint.setColor(resultColors[position]);
                        break;
                }
                if (resultPosition == position) {
                    mArcPaint.setColor(defaultColors[position]);
                }
            }

            // 繪製每一個小塊
            bitInfos.get(position).info.itemBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            Canvas itemCanvas = new Canvas(bitInfos.get(position).info.itemBitmap);
            itemCanvas.rotate(tmpAngle, mCenter, mCenter); // 根據角度進行同步旋轉
            itemCanvas.drawArc(mRange, -sweepAngle / 2 - 90, sweepAngle, true, mArcPaint); // 繪製背景顏色, 從最上邊開始畫
            drawIconAndText(position, itemCanvas); // 繪製小圖片和文本, 因為一起畫好畫點
        } else {
            Canvas itemCanvas = new Canvas(bitInfos.get(position).info.itemBitmap);
            itemCanvas.rotate(tmpAngle, mCenter, mCenter); // 根據角度進行同步旋轉
        }
        return bitInfos.get(position).info.itemBitmap;
    }

    private void drawCanvas() {
        if (bitInfos == null || bitInfos.size() == 0) return;
        mCanvas.drawCircle(mCenter, mCenter, mBackColorWidth / 2 + 50, mBackColorPaint3); // 繪製背景圖3
        mCanvas.drawCircle(mCenter, mCenter, mBackColorWidth / 2 + 20, mBackColorPaint2); // 繪製背景圖2
        mCanvas.drawCircle(mCenter, mCenter, mBackColorWidth / 2, mBackColorPaint); // 繪製背景圖
        mCanvas.drawBitmap(getFontBitmap(), 0, 0, null); // 畫前景圖片
    }

    /**
     * 繪製前景圖片, 這裡包含的是圖片信息和文字信息, 還有背景圓弧背景展示
     */
    private Bitmap getFontBitmap() {
        // 繪畫區域的圖片, 這邊會將各個小圖片拼接成一張圖片
        Bitmap fontBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(fontBitmap);
        // 根據角度進行同步旋轉
        // 圓盤角度
        float mStartAngle = 0;
        canvas.rotate(mStartAngle, mCenter, mCenter);
        float tmpAngle = 0;
        float sweepAngle = (float) (360 / mItemCount);
        for (int i = 0; i < mItemCount; i++) {
            // 這邊可以得到新的bitmap
            canvas.drawBitmap(getDrawItemBitmap(tmpAngle, sweepAngle, i), 0, 0, null);
            tmpAngle += sweepAngle;
        }
        return fontBitmap;
    }

    /**
     * 繪製小圖片和文字
     */
    private void drawIconAndText(int i, Canvas canvas) {
        float rt = mLitterBitWidth / mRangeWidth; // 根據的標註, 比例為115/730
        // 計算繪畫區域的直徑
        int mRadius = (int) (mRange.right - mRange.left);
        int imgWidth = (int) (mRadius * rt);
        int x = mCenter; // 獲取中心點坐標
        int y = (int) (mCenter - mRadius / 2 + (float) mRadius / 2 * 1 / 4f); // 這邊讓圖片從四分之一出開始畫
        // 確定小圖片的區域
        Rect rect = new Rect(x - imgWidth / 2, y - imgWidth / 2, x + imgWidth
                / 2, y + imgWidth / 2);
        // 將圖片畫上去
        canvas.drawBitmap(bitInfos.get(i).bitmap, null, rect, null);
        // 繪製文本
        if (!TextUtils.isEmpty(bitInfos.get(i).text)) {
            // 最大字數限制為8個字
            if (bitInfos.get(i).text.length() > 8) {
                bitInfos.get(i).text = bitInfos.get(i).text.substring(0, 8);
            }
            StaticLayout textLayout = new StaticLayout(bitInfos.get(i).text, mTextPaint,
                    imgWidth, Layout.Alignment.ALIGN_NORMAL, 1f, 0, false);
            canvas.translate(mCenter, rect.bottom + dip2px(6));
            textLayout.draw(canvas);
            canvas.translate(-mCenter, -(rect.bottom + dip2px(2))); // 畫完之後移動回來
        }
    }

    /**
     * dp轉像素
     */
    public final int dip2px(float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getContext().getResources().getDisplayMetrics());
    }

    /**
     * 刷新畫布
     **/
    private void onDrawInvalidate() {
        synchronized (obj) {
            invalidate();
        }
    }

    /**
     * 提供背景寬度, 以方便調整箭頭的大小
     */
    public float getBackgroundWidth() {
        return mBackColorWidth;
    }
}
