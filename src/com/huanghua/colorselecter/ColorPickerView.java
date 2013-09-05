
package com.huanghua.colorselecter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.huanghua.samsanglockscreen.R;

public class ColorPickerView extends View {

    private int selectColor;
    private Paint mPaint;
    private Paint mCenterPaint;
    private final int[] mColors;
    private OnColorChangedListener onColorChangeListenrer;
    private Shader shader = null;
    private Bitmap mBitmap;
    private float mBitmapX;
    private float mBitmapY;
    private boolean mIsFirst = true;

    int displayW = 0;
    int displayH = 0;

    public int getSelectColor() {
        return selectColor;
    }

    public void setSelectColor(int selectColor) {
        this.selectColor = selectColor;
    }

    public OnColorChangedListener getOnColorChangeListenrer() {
        return onColorChangeListenrer;
    }

    public void setOnColorChangeListenrer(
            OnColorChangedListener onColorChangeListenrer) {
        this.onColorChangeListenrer = onColorChangeListenrer;
    }

    public interface OnColorChangedListener {
        void colorChanged(int color);
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mColors = new int[] {
                0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF,
                0xFFFF00FF, 0xFFFF0000
        };
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        selectColor = 0xFF00FFFF;
        mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCenterPaint.setColor(0xFFFF0000);
        mCenterPaint.setStrokeWidth(5);
        mIsFirst = true;
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.widget_innerline);
    }

    private int interpColor(int colors[], float unit) {

        if (unit <= 0) {
            return colors[0];
        }
        if (unit >= displayW) {
            return colors[colors.length - 1];
        }

        float pUnit = displayW / 7;
        float p = unit / pUnit;
        int i = (int) p;
        p -= i;

        if (i < 0) {
            i = 0;
        } else if (i > 5) {
            i = 5;
        }
        // now p is just the fractional part [0...1) and i is the index
        int c0 = colors[i];
        int c1 = colors[i + 1];
        int a = ave(Color.alpha(c0), Color.alpha(c1), p);
        int r = ave(Color.red(c0), Color.red(c1), p);
        int g = ave(Color.green(c0), Color.green(c1), p);
        int b = ave(Color.blue(c0), Color.blue(c1), p);

        selectColor = Color.argb(a, r, g, b);
        mSelecter.changeLastColor(selectColor);
        return selectColor;
    }

    private int ave(int s, int d, float p) {
        return s + java.lang.Math.round(p * (d - s));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (shader == null) {
            displayW = getWidth();
            displayH = getHeight();
            shader = new LinearGradient(0, 0, displayW, 0, mColors, null, TileMode.REPEAT);
            mPaint.setShader(shader);
        }
        if (mIsFirst) {
            mBitmapX = displayW / 2;
            mBitmapY = displayH / 2;
            mIsFirst = false;
        }
        if (mBitmapX + mBitmap.getWidth() / 2 > displayW) {
            mBitmapX = displayW - mBitmap.getWidth() / 2;
        }
        if (mBitmapX - mBitmap.getWidth() / 2 < 0) {
            mBitmapX = 0 + mBitmap.getWidth() / 2;
        }
        if (mBitmapY + mBitmap.getHeight() / 2 > displayH) {
            mBitmapY = displayH - mBitmap.getHeight() / 2;
        }
        if (mBitmapY - mBitmap.getHeight() / 2 < 0) {
            mBitmapY = 0 + mBitmap.getHeight() / 2;
        }
        canvas.drawRect(0, 0, displayW, displayH, mPaint);
        canvas.drawBitmap(mBitmap, mBitmapX - mBitmap.getWidth() / 2,
                mBitmapY - mBitmap.getHeight() / 2, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        if (onColorChangeListenrer != null)
            onColorChangeListenrer.colorChanged(interpColor(mColors, x));
        mBitmapX = event.getX();
        mBitmapY = event.getY();
        invalidate();
        return true;
    }

    private SansumColorSelecter mSelecter;

    public void setColorSelecter(SansumColorSelecter seleteter) {
        mSelecter = seleteter;
    }
}
