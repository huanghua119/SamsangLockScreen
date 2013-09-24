
package com.huanghua.samsanglockscreen;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.GpsStatus.NmeaListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.huanghua.apps.AllApplications;
import com.huanghua.apps.SamsungShortCutIcon;

import java.net.URISyntaxException;
import java.util.ArrayList;

public class LockScreenEditShortCutApp extends Activity implements OnClickListener,
        OnCheckedChangeListener {

    private FrameLayout mShortCutAppLayout = null;
    private SamsungShortCutIcon mLockShortcutApps[];
    private ImageView mAddShortCut;
    private ImageView mSortCursor;
    private TextView mTrash;
    private Switch mShortSwitch;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mNumOfIcons = 5;
    private int mLongClickShortIndex = -1;
    private int mIconsBackSize;
    private int mCursorX;
    private int mCursorShowX;
    private int mIconMargins = 30;
    private float mDownX;
    private float mDownY;
    private float[] mOldLocations = new float[2];
    private boolean mIsLongClick = false;
    private static final int SEND_MESSAGE_CHANGE_LONG_CLICK = 100;
    private static final int SEND_MESSAGE_SHORT_CUT_ICON_SORT = 101;
    private static final int REQUEST_CODE_ADD_APP = 1;
    private static final int REQUEST_CODE_UPDATE_APP = 2;
    private int[] mShortcutAppsIds = {
            R.id.lock_app_1,
            R.id.lock_app_2,
            R.id.lock_app_3,
            R.id.lock_app_4,
            R.id.lock_app_5,
    };
    final String mSettingKeyStrings[] = {
            "samsunglockscreen_shortcut_app_activity",
            "samsunglockscreen_shortcut_app_activity2",
            "samsunglockscreen_shortcut_app_activity3",
            "samsunglockscreen_shortcut_app_activity4",
            "samsunglockscreen_shortcut_app_activity5",
    };
    final String mSettingDefaultStrings[] = {
            "com.android.contacts", "com.android.mms", "com.android.email", "com.android.browser",
            "com.huanghua.samsanglockscreen",
    };

    final String mSettingDefaultStrings2[] = {
            "com.android.contacts.activities.DialtactsActivity", "com.android.mms.ui.BootActivity",
            "com.android.email.activity.Welcome",
            "com.android.browser.BrowserActivity", "com.huanghua.samsanglockscreen.MainActivity",
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int message = msg.what;
            switch (message) {
                case SEND_MESSAGE_CHANGE_LONG_CLICK:
                    mIsLongClick = true;
                    break;
                case SEND_MESSAGE_SHORT_CUT_ICON_SORT:
                    setIconLocation();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.keyguard_screen_edit_short_cut);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        mIconsBackSize = getResources().getDimensionPixelSize(
                R.dimen.lock_icon_back_size);
        mTrash = (TextView) findViewById(R.id.short_cut_trash);
        mLockShortcutApps = new SamsungShortCutIcon[5];
        mAddShortCut = (ImageView) findViewById(R.id.lock_app_add);
        mAddShortCut.setOnClickListener(this);
        mShortSwitch = (Switch) findViewById(R.id.short_cut_switch);
        mShortSwitch.setOnCheckedChangeListener(this);
        mSortCursor = (ImageView) findViewById(R.id.lock_app_sort_cursor);
        for (int i = 0; i < 5; i++) {
            mLockShortcutApps[i] = (SamsungShortCutIcon) findViewById(mShortcutAppsIds[i]);
            mLockShortcutApps[i].setOnTouchListener(mLockShortCutTouchListener);
        }
        setIconLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean shortCutShow = getShortCutShow();
        if (mShortSwitch != null) {
            mShortSwitch.setChecked(shortCutShow);
        }
        if (!shortCutShow) {
            enabledShortCut(shortCutShow);
        }
    }

    private void setIconLocation() {
        mNumOfIcons = getShortCutNum();
        int left = 0;
        ViewGroup.MarginLayoutParams mlp;
        String uri;
        final PackageManager pm = getPackageManager();
        Drawable d2;
        mAddShortCut.setVisibility(mNumOfIcons == 5 ? View.GONE : View.VISIBLE);
        for (int i = 5; i > mNumOfIcons; i--) {
            mLockShortcutApps[i - 1].setVisibility(View.GONE);
        }
        for (int i = 0; i < getShowIconNum(); i++) {
            left = left + (i == 0 ? 0 : mIconsBackSize) + getLeftMargins(i, getShowIconNum());
            if (i == mNumOfIcons && mNumOfIcons < 5) {
                mlp = (ViewGroup.MarginLayoutParams) mAddShortCut.getLayoutParams();
                mlp.setMargins(left, mScreenHeight / 2 - mIconsBackSize, 0, 0);
            } else {
                mlp = (ViewGroup.MarginLayoutParams) mLockShortcutApps[i].getLayoutParams();
                mlp.setMargins(left, mScreenHeight / 2 - mIconsBackSize, 0, 0);
                mLockShortcutApps[i].setVisibility(View.VISIBLE);
                uri = getShortCutAppUri(mSettingKeyStrings[i]);
                try {
                    Intent intent;
                    if (null == uri || uri.equals("")) {
                        uri = mSettingDefaultStrings[i];
                        intent = new Intent(Intent.ACTION_MAIN, null);
                        intent.setClassName(mSettingDefaultStrings[i], mSettingDefaultStrings2[i]);
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        uri = intent.toUri(0);
                        setShortCutAppUri(mSettingKeyStrings[i],
                                uri);
                    } else {
                        intent = Intent.parseUri(uri, 0);
                    }
                    ActivityInfo info = intent.resolveActivityInfo(pm,
                            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    if (info != null) {
                        d2 = info.loadIcon(pm);
                        mLockShortcutApps[i].setTag(intent);
                        mLockShortcutApps[i].setImageDrawable(d2);
                    }
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int getLeftMargins(int index, int num) {
        int result = mIconMargins;
        if (index == 0) {
            if (num == 5) {
                result = (mScreenWidth - mIconsBackSize * num - mIconMargins * (num - 1)) / 2;
            } else {
                result = (mScreenWidth - mIconsBackSize * num - (mIconMargins + 15) * (num - 1)) / 2;
            }
        } else {
            if (num == 5) {
                result = mIconMargins;
            } else {
                result = mIconMargins + 15;
            }
        }
        return result;
    }

    private int getShowIconNum() {
        return (mNumOfIcons < 5 ? mNumOfIcons + 1 : mNumOfIcons);
    }

    private String getShortCutAppUri(String id) {
        SharedPreferences sp = getSharedPreferences("samsung_lock", Context.MODE_PRIVATE);
        return sp.getString(id, "");
    }

    private void setShortCutAppUri(String id, String uri) {
        Log.i("hh", "id:" + id + " uri:" + uri);
        SharedPreferences sp = getSharedPreferences("samsung_lock", Context.MODE_PRIVATE);
        SharedPreferences.Editor spd = sp.edit();
        spd.putString(id, uri);
        spd.commit();
    }

    private int getShortCutNum() {
        SharedPreferences sp = getSharedPreferences("samsung_lock", Context.MODE_PRIVATE);
        return sp.getInt("samsunglockscreen_shortcut_app_num", 5);
    }

    private void setShortCutNum(int num) {
        SharedPreferences sp = getSharedPreferences("samsung_lock", Context.MODE_PRIVATE);
        SharedPreferences.Editor spd = sp.edit();
        spd.putInt("samsunglockscreen_shortcut_app_num", num);
        spd.commit();
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

    @Override
    public void onClick(View arg0) {
        if (mAddShortCut == arg0) {
            // setIconLocation();
            Intent intent = new Intent();
            intent.setClass(this, AllApplications.class);
            if (mNumOfIcons > 0) {
                intent.putStringArrayListExtra("filter_list", getFilterList());
            }
            startActivityForResult(intent, REQUEST_CODE_ADD_APP);
        }
    }

    private void performClickShortIcon() {
        Intent intent = new Intent();
        intent.setClass(this, AllApplications.class);
        if (mNumOfIcons > 0) {
            intent.putStringArrayListExtra("filter_list", getFilterList());
        }
        startActivityForResult(intent, REQUEST_CODE_UPDATE_APP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String uri = "";
            switch (requestCode) {
                case REQUEST_CODE_ADD_APP:
                    uri = data.getStringExtra("app_uri");
                    setShortCutAppUri(mSettingKeyStrings[getShowIconNum() - 1], uri);
                    setShortCutNum(getShortCutNum() + 1);
                    setIconLocation();
                    break;
                case REQUEST_CODE_UPDATE_APP:
                    uri = data.getStringExtra("app_uri");
                    setShortCutAppUri(mSettingKeyStrings[mLongClickShortIndex], uri);
                    setIconLocation();
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private OnTouchListener mLockShortCutTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            float x = event.getRawX();
            float y = event.getRawY();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mLongClickShortIndex = -1;
                    mIsLongClick = false;
                    mOldLocations[0] = v.getX();
                    mOldLocations[1] = v.getY();
                    mDownX = x;
                    mDownY = y;
                    setClickShortIndex(v);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!mIsLongClick) {
                        if (Math.abs(mDownX - x) < v.getWidth() / 3
                                && Math.abs(mDownY - y) < v.getHeight() / 3) {
                            mHandler.sendEmptyMessageDelayed(SEND_MESSAGE_CHANGE_LONG_CLICK, 1000);
                        } else {
                            mHandler.removeMessages(SEND_MESSAGE_CHANGE_LONG_CLICK);
                        }
                    } else {
                        mLockShortcutApps[mLongClickShortIndex].bringToFront();
                        mLockShortcutApps[mLongClickShortIndex].setX(x);
                        mLockShortcutApps[mLongClickShortIndex].setY(y - v.getHeight() / 2 * 3);
                        boolean isHoveringOverDelete = isEnterTrash((int) x, (int) y);
                        mTrash.setVisibility(View.VISIBLE);
                        if (isHoveringOverDelete) {
                            mTrash.setBackgroundResource(R.drawable.kg_widget_delete_drop_rollover_bg);
                            Drawable drawable = getResources().getDrawable(
                                    R.drawable.kg_widget_delete_drop_rollover);
                            drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                                    drawable.getMinimumHeight());
                            mTrash.setCompoundDrawables(null, drawable,
                                    null, null);
                        } else {
                            mTrash.setBackgroundResource(R.drawable.kg_widget_delete_drop_target_bg);
                            Drawable drawable = getResources().getDrawable(
                                    R.drawable.kg_widget_delete_drop_target_trashcan);
                            drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                                    drawable.getMinimumHeight());
                            mTrash.setCompoundDrawables(null, drawable,
                                    null, null);
                        }
                        mLockShortcutApps[mLongClickShortIndex]
                                .isHoveringOverDeleteDropTarget(isHoveringOverDelete);
                        boolean isCanSortIcon = isCanSortIcon((int) x, (int) y);
                        if (isCanSortIcon) {
                            mSortCursor.setX(mCursorX);
                            mSortCursor.setY(mScreenHeight / 2 - mIconsBackSize - 4);
                            mSortCursor.setVisibility(View.VISIBLE);
                            boolean iconCanSort = (mCursorShowX == mLongClickShortIndex)
                                    || ((mCursorShowX - 1) == mLongClickShortIndex);
                            mLockShortcutApps[mLongClickShortIndex].setCanSort(!iconCanSort);
                        } else {
                            mSortCursor.setVisibility(View.GONE);
                        }
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    mHandler.removeMessages(SEND_MESSAGE_CHANGE_LONG_CLICK);
                    mLockShortcutApps[mLongClickShortIndex].setX(mOldLocations[0]);
                    mLockShortcutApps[mLongClickShortIndex].setY(mOldLocations[1]);
                    mSortCursor.setVisibility(View.GONE);
                    if (mLockShortcutApps[mLongClickShortIndex].isEnterDeleteTarget()) {
                        reorderIconForDelete();
                    } else if (mLockShortcutApps[mLongClickShortIndex].isCanSort()) {
                        reorderIconForSort();
                    } else {
                        if (!mIsLongClick) {
                            if (Math.abs(mDownX - x) < v.getWidth() / 3
                                    && Math.abs(mDownY - y) < v.getHeight() / 3) {
                                performClickShortIcon();
                            }
                        } else {
                            resetForTouchUp();
                        }
                    }
                    break;
            }
            return true;
        }
    };

    private void resetForTouchUp() {
        mTrash.setBackgroundResource(R.drawable.kg_widget_delete_drop_target_bg);
        Drawable drawable = getResources().getDrawable(
                R.drawable.kg_widget_delete_drop_target_trashcan);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                drawable.getMinimumHeight());
        mTrash.setCompoundDrawables(null, drawable,
                null, null);
        mTrash.setVisibility(View.INVISIBLE);
        for (int i = 0; i < mNumOfIcons; i++) {
            mLockShortcutApps[i].isHoveringOverDeleteDropTarget(false);
            mLockShortcutApps[i].setCanSort(false);
        }
        mLongClickShortIndex = -1;
        mCursorShowX = -1;
        mIsLongClick = false;
    }

    private boolean isEnterTrash(int x, int y) {
        if (mTrash != null) {
            Rect mAltTmpRect = new Rect();
            Rect mTmpRect = new Rect();
            mAltTmpRect.set(0, 0, 0, 0);
            View parent = (View) mTrash.getParent();
            if (parent != null) {
                parent.getGlobalVisibleRect(mAltTmpRect);
            }
            mTrash.getGlobalVisibleRect(mTmpRect);
            // mTmpRect.offset(-mAltTmpRect.left, -mAltTmpRect.top);
            return mTmpRect.contains(x, y);
        }
        return false;
    }

    private boolean isCanSortIcon(int x, int y) {
        int start = 0;
        int end = mNumOfIcons;
        if (mLongClickShortIndex == 0) {
            start = 1;
        }
        if (mLongClickShortIndex == mNumOfIcons - 1) {
            end = mNumOfIcons - 1;
        }
        if (y < (mScreenHeight / 2 - mIconsBackSize - 4)
                || y > (mScreenHeight / 2 + mIconsBackSize - 4)) {
            return false;
        }
        for (int i = start; i < end + 1; i++) {
            int left, right;
            if (i == 0) {
                right = mLockShortcutApps[i].getLeft();
                left = right - 20;
            } else if (i == mNumOfIcons) {
                if (mNumOfIcons != 5) {
                    left = mLockShortcutApps[mNumOfIcons - 1].getRight();
                    right = mAddShortCut.getLeft();
                } else {
                    left = mLockShortcutApps[mNumOfIcons - 1].getRight();
                    right = left + 20;
                }
            } else {
                left = mLockShortcutApps[i - 1].getRight();
                right = mLockShortcutApps[i].getLeft();
            }
            if (x >= left && x <= right) {
                mCursorX = (left + right) / 2;
                mCursorShowX = i;
                Log.i("hh", "isCanSortIcon:" + i);
                return true;
            }
        }
        return false;
    }

    private void reorderIconForDelete() {
        setShortCutNum(getShortCutNum() - 1);
        mLockShortcutApps[mLongClickShortIndex].setVisibility(View.INVISIBLE);
        if (mLongClickShortIndex != 4 && mLongClickShortIndex != mNumOfIcons - 1) {
            for (int i = mLongClickShortIndex; i < mNumOfIcons; i++) {
                if (i == 4) {
                    break;
                }
                setShortCutAppUri(mSettingKeyStrings[i],
                        getShortCutAppUri(mSettingKeyStrings[i + 1]));
            }
        }
        resetForTouchUp();
        mHandler.sendEmptyMessage(SEND_MESSAGE_SHORT_CUT_ICON_SORT);
    }

    private void reorderIconForSort() {
        mLockShortcutApps[mLongClickShortIndex].setVisibility(View.INVISIBLE);
        String clickUri = getShortCutAppUri(mSettingKeyStrings[mLongClickShortIndex]);
        if (mCursorShowX > mLongClickShortIndex) {
            for (int i = mLongClickShortIndex; i < mCursorShowX; i++) {
                if (i == mCursorShowX - 1) {
                    setShortCutAppUri(mSettingKeyStrings[i], clickUri);
                } else {
                    setShortCutAppUri(mSettingKeyStrings[i],
                            getShortCutAppUri(mSettingKeyStrings[i + 1]));
                }
            }
        } else {
            for (int i = mLongClickShortIndex; i >= mCursorShowX; i--) {
                if (i == mCursorShowX) {
                    setShortCutAppUri(mSettingKeyStrings[i], clickUri);
                } else {
                    setShortCutAppUri(mSettingKeyStrings[i],
                            getShortCutAppUri(mSettingKeyStrings[i - 1]));
                }
            }
        }
        resetForTouchUp();
        mHandler.sendEmptyMessage(SEND_MESSAGE_SHORT_CUT_ICON_SORT);
    }

    private void enabledShortCut(boolean enabled) {
        for (int i = 0; i < 5; i++) {
            mLockShortcutApps[i].setEnabled(enabled);
            mLockShortcutApps[i].setAlpha(enabled ? 1 : 0.6f);
        }
        mAddShortCut.setEnabled(enabled);
        mAddShortCut.setAlpha(enabled ? 1 : 0.6f);
    }

    private void setClickShortIndex(View v) {
        for (int i = 0; i < 5; i++) {
            if (v == mLockShortcutApps[i]) {
                mLongClickShortIndex = i;
                break;
            }
        }
    }

    private ArrayList<String> getFilterList() {
        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < mNumOfIcons; i++) {
            result.add(getShortCutAppUri(mSettingKeyStrings[i]));
        }
        return result;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        setShortCutShow(isChecked);
        enabledShortCut(isChecked);
    }
}
