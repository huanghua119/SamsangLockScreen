
package com.huanghua.samsanglockscreen;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.widget.CompoundButton;

public class LockScreenSetting extends PreferenceActivity {

    private static final String KEY_SHOW_KEYGUARD_SHORT_CUT = "lock_setting_edit_shortcut";
    private SamsungSwitchPreference mShowKeyguardShortCut;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.lockscreen_settings);
        mShowKeyguardShortCut = (SamsungSwitchPreference) getPreferenceScreen().findPreference(
                KEY_SHOW_KEYGUARD_SHORT_CUT);
        mShowKeyguardShortCut.setSwitchChangeListener(mShortCutSwitchListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mShowKeyguardShortCut != null) {
            mShowKeyguardShortCut.setChecked(getShortCutShow());
        }
    }

    private void setShortCutShow(boolean isShow) {
        SharedPreferences sp = getSharedPreferences("samsung_lock", Context.MODE_PRIVATE);
        SharedPreferences.Editor spd = sp.edit();
        spd.putInt("samsunglockscreen_shortcut_app_show", isShow ? 1 : 0);
        spd.commit();
    }

    private boolean getShortCutShow() {
        SharedPreferences sp = getSharedPreferences("samsung_lock", Context.MODE_PRIVATE);
        return sp.getInt("samsunglockscreen_shortcut_app_show", 0) == 1 ? true : false;
    }

    private CompoundButton.OnCheckedChangeListener mShortCutSwitchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            setShortCutShow(isChecked);
            return;
        }
    };
}
