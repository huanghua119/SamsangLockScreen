
package com.huanghua.samsanglockscreen;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.huanghua.colorselecter.ColorPickerView;
import com.huanghua.colorselecter.SansumColorSelecter;

public class LockScreenEditInfo extends Activity implements OnClickListener {

    private EditText mLogo;
    private Button mOK;
    private Button mCancel;
    private AutoScaleTextSizeWatcher mAutoScaleTextSizeWatcher;
    private SansumColorSelecter mColorSelecter;
    private ColorPickerView mColorPicker;
    private int mSaveTextColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.keyguard_screen_edit_user_info);
        mLogo = (EditText) findViewById(R.id.logo);
        mAutoScaleTextSizeWatcher = new AutoScaleTextSizeWatcher(mLogo);

        Resources r = getResources();
        mAutoScaleTextSizeWatcher.setAutoScaleParameters(r
                .getDimensionPixelSize(R.dimen.keyguard_lockscreen_logo_textsize_min), r
                .getDimensionPixelSize(R.dimen.keyguard_lockscreen_logo_textsize), r
                .getDimensionPixelSize(R.dimen.keyguard_lockscreen_logo_textsize_delta),
                mLogo.getWidth());
        mOK = (Button) findViewById(R.id.ok);
        mOK.setOnClickListener(this);
        mCancel = (Button) findViewById(R.id.cancel);
        mCancel.setOnClickListener(this);

        mColorSelecter = (SansumColorSelecter) findViewById(R.id.color_seleter);
        mColorPicker = (ColorPickerView) mColorSelecter.findViewById(R.id.color_picker);
        mColorPicker.setOnColorChangeListenrer(new ColorPickerView.OnColorChangedListener() {
            @Override
            public void colorChanged(int color) {
                mSaveTextColor = color;
                mLogo.setTextColor(color);
            }
        });
        mColorSelecter.setColorSelecterLinstener(new SansumColorSelecter.ColorSelecterLinstener() {
            @Override
            public void onColorSeleter(int color) {
                mSaveTextColor = color;
                mLogo.setTextColor(color);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mLogo != null) {
            mLogo.setText(getLogoText());
            mLogo.setTextSize(TypedValue.COMPLEX_UNIT_SP, getLogoTextSize());
            mSaveTextColor =getLogoTextColor();
            mLogo.setTextColor(mSaveTextColor);
            mLogo.setSelection(0, mLogo.getText().length());
        }
    }

    private void setLogoText(String text) {
        SharedPreferences sp = getSharedPreferences("samsung_lock", MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        spe.putString("logo_text", text);
        spe.commit();
    }

    private void setLogoTextSize(float size) {
        SharedPreferences sp = getSharedPreferences("samsung_lock", MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        spe.putFloat("logo_text_size", size);
        spe.commit();
    }

    private void setLogoTextColor(int color) {
        SharedPreferences sp = getSharedPreferences("samsung_lock", MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt("logo_text_color", color);
        spe.commit();
    }

    private String getLogoText() {
        SharedPreferences sp = getSharedPreferences("samsung_lock", MODE_PRIVATE);
        return sp.getString("logo_text", getString(R.string.logo_text));
    }

    private float getLogoTextSize() {
        SharedPreferences sp = getSharedPreferences("samsung_lock", MODE_PRIVATE);
        return sp.getFloat("logo_text_size",
                getResources().getDimensionPixelSize(R.dimen.keyguard_lockscreen_logo_textsize));
    }

    private int getLogoTextColor() {
        SharedPreferences sp = getSharedPreferences("samsung_lock", MODE_PRIVATE);
        return sp.getInt("logo_text_color", Color.WHITE);
    }

    @Override
    public void onClick(View v) {
        if (v == mOK) {
            String text = mLogo.getText().toString();
            setLogoText(text);
            setLogoTextSize(mAutoScaleTextSizeWatcher.getCurrentTextSize());
            setLogoTextColor(mSaveTextColor);
            finish();
        } else if (v == mCancel) {
            finish();
        }
    }

}
