
package com.huanghua.colorselecter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.huanghua.samsanglockscreen.R;

public class SansumColorSelecter extends RelativeLayout implements OnTouchListener {

    private static final int TOTAL_COLOR = 16;
    private ImageButton[] mAllColor = new ImageButton[TOTAL_COLOR];
    private ColorSelecterLinstener mListener;
    private boolean mIsDIYColor = false;
    private ColorPickerView mColorPView;

    private int[] mColorIcon = {
            R.id.color_1, R.id.color_2, R.id.color_3, R.id.color_4, R.id.color_5,
            R.id.color_6, R.id.color_7, R.id.color_8, R.id.color_9, R.id.color_10, R.id.color_11,
            R.id.color_12, R.id.color_13, R.id.color_14, R.id.color_15, R.id.color_16
    };

    private int[] mColorValue = {
            R.color.color_1, R.color.color_2, R.color.color_3, R.color.color_4, R.color.color_5,
            R.color.color_6, R.color.color_7, R.color.color_8, R.color.color_9, R.color.color_10,
            R.color.color_11,
            R.color.color_12, R.color.color_13, R.color.color_14, R.color.color_15,
            getResources().getColor(R.color.color_16)
    };

    public SansumColorSelecter(Context context) {
        this(context, null);
    }

    public SansumColorSelecter(Context context, AttributeSet attrs) {
        super(context, attrs);
        final LayoutInflater inflater = LayoutInflater.from(context);

        inflater.inflate(R.layout.samsung_color_selecter, this, true);
        for (int i = 0; i < TOTAL_COLOR; i++) {
            mAllColor[i] = (ImageButton) findViewById(mColorIcon[i]);
            mAllColor[i].setTag(i);
            mAllColor[i].setOnTouchListener(this);
        }
        mColorPView = (ColorPickerView) findViewById(R.id.color_picker);
        mColorPView.setColorSelecter(this);
    }

    public void setSelectColorButton(int color) {
        for (int i = 0; i < TOTAL_COLOR; i++) {
            if (color == getResources().getColor(mColorValue[i])) {
                mAllColor[i].setPressed(true);
            }
        }
    }

    private void setSelectColorButton(float x, float y) {
        ImageButton ib = getColorButton(x, y);
        if (ib != null) {
            for (int i = 0; i < TOTAL_COLOR; i++) {
                if (ib == mAllColor[i]) {
                    mAllColor[i].setPressed(true);
                    if (i == TOTAL_COLOR - 1) {
                        mListener.onColorSeleter(mColorValue[i]);
                    } else {
                        mListener.onColorSeleter(getResources().getColor(mColorValue[i]));
                    }
                } else {
                    mAllColor[i].setPressed(false);
                }

            }
        }
    }

    private ImageButton getColorButton(float x, float y) {
        for (int i = 0; i < TOTAL_COLOR; i++) {
            ImageButton ib = mAllColor[i];
            int location[] = new int[2];
            ib.getLocationOnScreen(location);
            int left = location[0];
            int top = location[1];
            int width = ib.getWidth();
            int height = ib.getHeight();

            if (x > left && x < left + width && y > top
                    && y < top + height) {
                return ib;
            }
        }
        return null;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float rawX = event.getRawX();
        float rawY = event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (v instanceof ImageButton) {
                    for (int i = 0; i < TOTAL_COLOR; i++) {
                        if (v == mAllColor[i]) {
                            mAllColor[i].setPressed(true);
                            if (mListener != null) {
                                if (i == TOTAL_COLOR - 1) {
                                    mListener.onColorSeleter(mColorValue[i]);
                                } else {
                                    mListener.onColorSeleter(getResources()
                                            .getColor(mColorValue[i]));
                                }
                            }
                        } else {
                            mAllColor[i].setPressed(false);
                        }
                    }

                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (v instanceof ImageButton) {
                    setSelectColorButton(rawX, rawY);
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    public void setColorSelecterLinstener(ColorSelecterLinstener linstener) {
        this.mListener = linstener;
    }

    public interface ColorSelecterLinstener {
        void onColorSeleter(int color);
    }

    public void changeLastColor(int diyColor) {
        if (diyColor == 0) {
            mColorValue[mColorValue.length - 1] = getResources().getColor(R.color.color_16);
            mAllColor[mAllColor.length - 1]
                    .setBackgroundResource(R.drawable.settings_colorchip_none);
        } else {
            if (!mAllColor[mAllColor.length - 1].isPressed()) {
                for (int i = 0; i < TOTAL_COLOR; i++) {
                    mAllColor[i].setPressed(false);
                }
            }
            mAllColor[mAllColor.length - 1].setPressed(true);
            mAllColor[mAllColor.length - 1].setBackgroundColor(diyColor);
            mColorValue[mColorValue.length - 1] = diyColor;
        }
    }

    public void setDIYColor(boolean isDiy, int diyColor) {
        mIsDIYColor = isDiy;
        if (!mIsDIYColor) {
            mColorValue[mColorValue.length - 1] = getResources().getColor(R.color.color_16);
            mAllColor[mAllColor.length - 1]
                    .setBackgroundResource(R.drawable.settings_colorchip_none);
        } else {
            mAllColor[mAllColor.length - 1].setBackgroundColor(diyColor);
            mColorValue[mColorValue.length - 1] = diyColor;
        }
    }
}
