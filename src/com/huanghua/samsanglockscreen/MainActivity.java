
package com.huanghua.samsanglockscreen;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.huanghua.apps.FavoriteApplications;

public class MainActivity extends Activity {

    private static final int MENU_SETTINGS = 0;
    private static final int MENU_LOCK_SWITCH = 1;
    private static final int MENU_ADD_FAVORITE = 2;
    private SamsangLockScreen mSamaungLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSamaungLock = new SamsangLockScreen(this);
        setContentView(mSamaungLock);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_SETTINGS, 0, R.string.lock_settings);
        menu.add(0, MENU_LOCK_SWITCH, 0, getLockEffect() == 1 ? R.string.lock_switch_fall
                : R.string.lock_switch_sun);
        menu.add(0, MENU_ADD_FAVORITE, 0, R.string.samsung_apps);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        Intent intent = new Intent();
        switch (itemId) {
            case MENU_SETTINGS:
                intent.setClass(this, LockScreenSetting.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
            case MENU_LOCK_SWITCH:
                switchLock(getLockEffect() == 0 ? 1 : 0);
                break;
            case MENU_ADD_FAVORITE:
                intent.setClass(this, FavoriteApplications.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem mSwitchItem = menu.getItem(MENU_LOCK_SWITCH);
        mSwitchItem.setTitle(getLockEffect() == 1 ? R.string.lock_switch_fall
                : R.string.lock_switch_sun);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSamaungLock.onResume();
    }

    private void switchLock(int effect) {
        SharedPreferences sp = getSharedPreferences("samsung_lock", MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt("lock_effect", effect);
        spe.commit();
        mSamaungLock.switchLockEffect(effect);
    }

    private int getLockEffect() {
        SharedPreferences sp = getSharedPreferences("samsung_lock", MODE_PRIVATE);
        return sp.getInt("lock_effect", 1);
    }
}
