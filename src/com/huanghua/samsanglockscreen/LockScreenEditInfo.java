
package com.huanghua.samsanglockscreen;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.huanghua.colorselecter.ColorPickerView;
import com.huanghua.colorselecter.SansumColorSelecter;

import java.util.Date;

public class LockScreenEditInfo extends Activity implements OnClickListener,
        OnCheckedChangeListener {

    private EditText mLogo;
    private Button mOK;
    private Button mCancel;
    private AutoScaleTextSizeWatcher mAutoScaleTextSizeWatcher;
    private SansumColorSelecter mColorSelecter;
    private ColorPickerView mColorPicker;
    private RadioButton mSetTextColor;
    private RadioButton mSetTextBGColor;
    private RadioGroup mSetGroup;
    private CheckBox mTimeCheck;
    private CheckBox mDateCheck;
    private int mSaveTextColor;
    private int mSaveTextBgColor;
    private TextView mDateView;
    private DigitalClock mTimeView;

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

        mTimeCheck = (CheckBox) findViewById(R.id.time_check);
        mDateCheck = (CheckBox) findViewById(R.id.date_check);
        mTimeCheck.setOnCheckedChangeListener(this);
        mDateCheck.setOnCheckedChangeListener(this);
        mDateView = (TextView) findViewById(R.id.date);
        mTimeView = (DigitalClock) findViewById(R.id.time);

        mSetTextColor = (RadioButton) findViewById(R.id.set_text_color);
        mSetTextBGColor = (RadioButton) findViewById(R.id.set_text_bg_color);
        mColorSelecter = (SansumColorSelecter) findViewById(R.id.color_seleter);
        mColorPicker = (ColorPickerView) mColorSelecter.findViewById(R.id.color_picker);
        mColorPicker.setOnColorChangeListenrer(new ColorPickerView.OnColorChangedListener() {
            @Override
            public void colorChanged(int color) {
                if (mSetTextColor.isChecked()) {
                    mSaveTextColor = color;
                    mLogo.setTextColor(color);
                } else if (mSetTextBGColor.isChecked()) {
                    mLogo.setBackgroundColor(color);
                    mSaveTextBgColor = color;
                }
            }
        });
        mColorSelecter.setColorSelecterLinstener(new SansumColorSelecter.ColorSelecterLinstener() {
            @Override
            public void onColorSeleter(int color) {
                if (mSetTextColor.isChecked()) {
                    mSaveTextColor = color;
                    mLogo.setTextColor(color);
                } else if (mSetTextBGColor.isChecked()) {
                    mSaveTextBgColor = color;
                    mLogo.setBackgroundColor(color);
                }
            }
        });
        mSetGroup = (RadioGroup) findViewById(R.id.set_group);
        mSetGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup arg0, int arg1) {
                changeLastColorByRadio(arg0.getCheckedRadioButtonId());
            }
        });
        changeLastColorByRadio(mSetGroup.getCheckedRadioButtonId());
    }

    private void changeLastColorByRadio(int id) {
        switch (id) {
            case R.id.set_text_color:
                mColorSelecter.setDIYColor(true, mColorPicker.getSelectColor());
                break;
            case R.id.set_text_bg_color:
                mColorSelecter.setDIYColor(false, 0);
                break;
        }
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
            mSaveTextColor = getLogoTextColor();
            mSaveTextBgColor = getLogoTextBgColor();
            mLogo.setTextColor(mSaveTextColor);
            mLogo.setBackgroundColor(mSaveTextBgColor);
            mLogo.setSelection(0, mLogo.getText().length());
            setViewAlpha(mDateView.isClickable(), mDateView);
            setViewAlpha(mTimeView.isClickable(), mTimeView);
        }
        refreshDate();
    }

    private void refreshDate() {
        mDateView.setText(DateFormat.format("E M 月 d 日", new Date()));
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

    private void setLogoTextBgColor(int color) {
        SharedPreferences sp = getSharedPreferences("samsung_lock", MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt("logo_text_bgcolor", color);
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

    private int getLogoTextBgColor() {
        SharedPreferences sp = getSharedPreferences("samsung_lock", MODE_PRIVATE);
        return sp.getInt("logo_text_bgcolor", Color.TRANSPARENT);
    }

    @Override
    public void onClick(View v) {
        if (v == mOK) {
            String text = mLogo.getText().toString();
            setLogoText(text);
            setLogoTextSize(mAutoScaleTextSizeWatcher.getCurrentTextSize());
            setLogoTextColor(mSaveTextColor);
            setLogoTextBgColor(mSaveTextBgColor);
            finish();
        } else if (v == mCancel) {
            finish();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == mDateCheck) {
            setViewAlpha(isChecked, mDateView);
        } else if (buttonView == mTimeCheck) {
            setViewAlpha(isChecked, mTimeView);
        }
    }

    private void setViewAlpha(boolean isChecked, View view) {
        if (isChecked) {
            view.setAlpha(1);
        } else {
            view.setAlpha(0.6f);
        }
    }

}
