
package com.huanghua.apps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class SamsungShortCutIcon extends ImageView {

    static final int HOVER_OVER_DELETE_DROP_TARGET_OVERLAY_COLOR = 0x99FF0000;
    private boolean mIsHoveringOverDeleteDropTarget = false;
    private Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private boolean mIsCanSort = false;

    public SamsungShortCutIcon(Context paramContext) {
        super(paramContext);
    }

    public SamsungShortCutIcon(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        super.dispatchDraw(canvas);
        drawHoveringOverDeleteOverlay(canvas);
        canvas.restore();
    }

    private void drawHoveringOverDeleteOverlay(Canvas c) {
        if (mIsHoveringOverDeleteDropTarget) {
            Bitmap mCrossFadeBitmap = ((BitmapDrawable) getDrawable()).getBitmap();
            c.drawBitmap(mCrossFadeBitmap, 0.0f, 0.0f, mPaint);
        }
    }

    public boolean isEnterDeleteTarget() {
        return mIsHoveringOverDeleteDropTarget;
    }

    public boolean isCanSort() {
        return mIsCanSort;
    }

    public void setCanSort(boolean sort) {
        this.mIsCanSort = sort;
    }

    public void isHoveringOverDeleteDropTarget(boolean isHovering) {
        if (mIsHoveringOverDeleteDropTarget != isHovering) {
            mIsHoveringOverDeleteDropTarget = isHovering;
            if (mPaint == null) {
                mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
            }
            if (mIsHoveringOverDeleteDropTarget) {
                mPaint.setColorFilter(new PorterDuffColorFilter(
                        HOVER_OVER_DELETE_DROP_TARGET_OVERLAY_COLOR, PorterDuff.Mode.SRC_ATOP));
            } else {
                mPaint.setColorFilter(null);
            }
            invalidate();
        }
    }
}
