
package com.huanghua.samsanglockscreen;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class LockScreenSetting extends PreferenceActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.lockscreen_settings);
    }
}
