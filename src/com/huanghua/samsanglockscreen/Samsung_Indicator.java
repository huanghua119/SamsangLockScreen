
package com.huanghua.samsanglockscreen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public class Samsung_Indicator extends View {

    public static final int SCREEN_COUNT = 4;
    private Bitmap NavigationHome;
    private Bitmap NavigationHomeCurrent;
    private Bitmap NavigationAdd;
    private Bitmap NavigationAddCurrent;
    private Bitmap Navigation;
    private Bitmap NavigationCurrent;
    private int mSelectPage = SCREEN_COUNT - 2;
    private int mTotlePage = SCREEN_COUNT;
    private float[] mPosY;
    private float[] mPosX;
    private float mLeftAndRightPadding;
    private static final float INIDICATOR_PADDING = 7.0f;

    public Samsung_Indicator(Context context) {
        this(context, null);
    }

    public Samsung_Indicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialIndicatorDrawables();
        setSelectAndTotle(mSelectPage, mTotlePage);
    }

    private void computeXY() {
        int imageWidth = 480;
        mPosX = new float[mTotlePage];
        mPosY = new float[mTotlePage];
        mLeftAndRightPadding = (imageWidth - INIDICATOR_PADDING * (SCREEN_COUNT - 1) - NavigationHome
                .getWidth()
                * (mTotlePage)) / 2;

        for (int i = 0; i < mTotlePage; i++) {
            mPosX[i] = mLeftAndRightPadding + i * (INIDICATOR_PADDING + NavigationHome.getWidth());
            mPosY[i] = 0;
        }
    }

    private void initialIndicatorDrawables() {
        Bitmap keyguard_navigation_add = BitmapFactory.decodeResource(getContext().getResources(),
                R.drawable.keyguard_navigation_add);
        Bitmap keyguard_navigation_add_current = BitmapFactory.decodeResource(getContext()
                .getResources(),
                R.drawable.keyguard_navigation_add_current);

        Bitmap keyguard_navigation_current = BitmapFactory.decodeResource(getContext()
                .getResources(),
                R.drawable.keyguard_navigation_current);
        Bitmap keyguard_navigation_normal = BitmapFactory.decodeResource(getContext()
                .getResources(),
                R.drawable.keyguard_navigation_normal);

        Bitmap keyguard_navigation_home = BitmapFactory.decodeResource(getContext().getResources(),
                R.drawable.keyguard_navigation_home);
        Bitmap keyguard_navigation_home_current = BitmapFactory.decodeResource(getContext()
                .getResources(),
                R.drawable.keyguard_navigation_home_current);

        NavigationHome = keyguard_navigation_home;
        NavigationHomeCurrent = keyguard_navigation_home_current;
        NavigationAdd = keyguard_navigation_add;
        NavigationAddCurrent = keyguard_navigation_add_current;
        Navigation = keyguard_navigation_normal;
        NavigationCurrent = keyguard_navigation_current;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawIndicators(canvas);
    }

    public void setSelectAndTotle(int select, int totle) {
        mSelectPage = select;
        mTotlePage = totle;
        computeXY();
    }

    private void drawIndicators(Canvas canvas) {
        for (int i = 0; i < mTotlePage; i++) {
            if (i == mSelectPage) {
                if (i == 0) {
                    canvas.drawBitmap(NavigationAddCurrent, mPosX[i], mPosY[i], null);
                } else if (i == mTotlePage - 2) {
                    canvas.drawBitmap(NavigationHomeCurrent, mPosX[i], mPosY[i], null);
                } else {
                    canvas.drawBitmap(NavigationCurrent, mPosX[i], mPosY[i], null);
                }
            } else {
                if (i == 0) {
                    canvas.drawBitmap(NavigationAdd, mPosX[i], mPosY[i], null);
                } else if (i == mTotlePage - 2) {
                    canvas.drawBitmap(NavigationHome, mPosX[i], mPosY[i], null);
                } else {
                    canvas.drawBitmap(Navigation, mPosX[i], mPosY[i], null);
                }
            }
        }
        return;
    }

}
