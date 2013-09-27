package com.huanghua.samsanglockscreen;

import android.content.Context;
import android.location.LocationManager;
import android.preference.Preference;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

public class SamsungSwitchPreference extends Preference {

    private boolean mChecked = true;
    private CompoundButton.OnCheckedChangeListener mSwitchChangeListener = new Listener();

    private class Listener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            setChecked(isChecked);
            return;
        }
    }

    public SamsungSwitchPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutResource(R.layout.samsung_switch_preference);
    }

    public SamsungSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.samsung_switch_preference);

    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        Switch switchbutton = (Switch) view.findViewById(R.id.switch_);
        if (mSwitchChangeListener != null && switchbutton != null) {
            switchbutton.setClickable(true);
            switchbutton.setChecked(mChecked);
            switchbutton.setOnCheckedChangeListener(mSwitchChangeListener);
            switchbutton.setEnabled(true);
        }
    }

    public void setSwitchChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        this.mSwitchChangeListener = listener;
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
        notifyChanged();
    }

    public boolean isChecked() {
        return mChecked;
    }

}