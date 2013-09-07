
package com.huanghua.samsanglockscreen;

import android.content.Context;
import android.graphics.Paint;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.widget.EditText;

public class AutoScaleTextSizeWatcher implements TextWatcher, OnKeyListener {
    private static final String TAG = "AutoScaleTextSizeWatcher";

    protected Context mContext;
    protected EditText mTarget;

    protected int mMaxTextSize;
    protected int mMinTextSize;
    protected int mDeltaTextSize;
    protected int mCurrentTextSize;
    protected int mDigitsWidth;

    protected DisplayMetrics mDisplayMetrics;

    protected Paint mPaint;

    protected int mPreviousDigitsLength;
    private boolean mIsDelete;
    private String mOldText;
    private int mOldLength;

    public AutoScaleTextSizeWatcher(EditText editText) {
        mContext = editText.getContext();
        mTarget = editText;
        mPaint = new Paint();
        mPaint.set(editText.getPaint());
        mTarget.addTextChangedListener(this);
        mTarget.setOnKeyListener(this);

        mDisplayMetrics = new DisplayMetrics();
        final WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(mDisplayMetrics);
    }

    public void setAutoScaleParameters(int minTextSize, int maxTextSize, int deltaTextSize,
            int digitsWidth) {
        mMaxTextSize = maxTextSize;
        mMinTextSize = minTextSize;
        mDeltaTextSize = deltaTextSize;
        mDigitsWidth = digitsWidth;

        mCurrentTextSize = mMaxTextSize;
        mTarget.setTextSize(TypedValue.COMPLEX_UNIT_SP, mCurrentTextSize);
    }

    public void trigger(boolean delete) {
        autoScaleTextSize(true);
    }

    public void afterTextChanged(Editable s) {
        int length = mTarget.getText().length();
        if (mOldLength > length) {
            mIsDelete = true;
        } else {
            mIsDelete = false;
        }
        autoScaleTextSize(mIsDelete);
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //
        mOldLength = s.length();
        mOldText = mTarget.getText().toString();
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //
    }

    void log(String msg) {
        Log.d(TAG, msg);
    }

    protected void autoScaleTextSize(boolean delete) {
        mDigitsWidth = mTarget.getWidth();
        if (mDigitsWidth == 0) {
            return;
        }
        final String digits = mTarget.getText().toString();

        if (digits.length() == 0) {
            mCurrentTextSize = mMaxTextSize;
            mTarget.setTextSize(TypedValue.COMPLEX_UNIT_SP, mMaxTextSize);
            return;
        }

        final int max = mMaxTextSize;
        final int min = mMinTextSize;
        final int digitsWidth = mDigitsWidth;
        final int delta = mDeltaTextSize;

        int inputWidth;
        int current = mCurrentTextSize;
        int precurrent = current;
        TextPaint mTextPaint = mTarget.getPaint();
        inputWidth = (int) mTextPaint.measureText(digits);
        
        
        if (getCurrentTextSize() <= mMinTextSize && inputWidth > mDigitsWidth ) {
            mTarget.setText(mOldText);
            mTarget.setSelection(mTarget.getText().length());
            return;
        }

        log("inputWidth = " + inputWidth + " current = " + current + " digitsWidth = "
                + digitsWidth);

        if (!delete) {
            while ((current > min) && inputWidth > digitsWidth) {
                current -= delta;
                if (current < min) {
                    current = min;
                    break;
                }
                mTarget.setTextSize(TypedValue.COMPLEX_UNIT_SP, current);
                mTextPaint = mTarget.getPaint();
                //mTextPaint.setTextSize(current);
                inputWidth = (int) mTextPaint.measureText(digits);
                log("inputWidth:" + inputWidth);
            }
        } else {
            while ((inputWidth < digitsWidth) && (current < max)) {
                precurrent = current;
                current += delta;
                if (current > max) {
                    current = max;
                    break;
                }
                mTarget.setTextSize(TypedValue.COMPLEX_UNIT_SP, current);
                mTextPaint = mTarget.getPaint();
                //mTextPaint.setTextSize(current);
                inputWidth = (int) mTextPaint.measureText(digits);
                log("inputWidth: " + inputWidth + " digitsWidth: " + digitsWidth);
                break;
            }
            //mTextPaint.setTextSize(current);
            mTarget.setTextSize(TypedValue.COMPLEX_UNIT_SP, current);
            mTextPaint = mTarget.getPaint();
            inputWidth = (int) mTextPaint.measureText(digits);
            log("inputWidth2: " + inputWidth + " digitsWidth: " + digitsWidth);
            if (inputWidth > digitsWidth) {
                current = precurrent;
            }
        }

        mCurrentTextSize = current;
        mTarget.setTextSize(TypedValue.COMPLEX_UNIT_SP, current);

    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        mIsDelete = false;
        if (keyCode == KeyEvent.KEYCODE_DEL) {
            mIsDelete = true;
        }
        return false;
    }

    public int getCurrentTextSize() {
        return mCurrentTextSize;
    }
}
