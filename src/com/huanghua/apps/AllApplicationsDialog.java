
package com.huanghua.apps;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.huanghua.samsanglockscreen.R;

import java.util.ArrayList;
import java.util.List;

public class AllApplicationsDialog extends Dialog implements OnItemClickListener {

    private Context mContext;

    private ListView mListView;
    private LayoutInflater mInflater;
    private ArrayList<ApplicationInfo> mAppApps;
    private int mSphoneCustomApp = -1;
    final String mSettingKeyStrings[] = {
            "SPHONELOCKSCREEN_CUSTOM_APP_ACTIVITY_1",
            "SPHONELOCKSCREEN_CUSTOM_APP_ACTIVITY_2",
            "SPHONELOCKSCREEN_CUSTOM_APP_ACTIVITY_3",
            "SPHONELOCKSCREEN_CUSTOM_APP_ACTIVITY_4",
            "SPHONELOCKSCREEN_CUSTOM_APP_ACTIVITY_5",
            "SPHONELOCKSCREEN_CUSTOM_APP_ACTIVITY_6",
            "SPHONELOCKSCREEN_CUSTOM_APP_ACTIVITY_7",
            "SPHONELOCKSCREEN_CUSTOM_APP_ACTIVITY_8",
            "SPHONELOCKSCREEN_CUSTOM_APP_ACTIVITY_9",
            "SPHONELOCKSCREEN_CUSTOM_APP_ACTIVITY_10",
            "SPHONELOCKSCREEN_CUSTOM_APP_ACTIVITY_11",
            "SPHONELOCKSCREEN_CUSTOM_APP_ACTIVITY_12",
    };

    private class ApplicationInfo {
        String name;
        Drawable icon;
        String uri;
    }

    public AllApplicationsDialog(Context context) {
        super(context);
        //super(context, com.android.internal.R.style.Theme_Dialog_RecentApplications);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        //window.setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        window.setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        window.setTitle("AllApps");

        setContentView(R.layout.app_applications);

        final WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(params);
        window.setFlags(0, WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        mInflater = LayoutInflater.from(mContext);
        mListView = (ListView) findViewById(R.id.list);
        loadAllAppsByBatch();
        mListView.setAdapter(new BaseAdapter() {
            @Override
            public View getView(int arg0, View arg1, ViewGroup arg2) {
                View view = mInflater.inflate(R.layout.app_list, null);
                TextView mApp = (TextView) view.findViewById(R.id.digits);
                ApplicationInfo info = mAppApps.get(arg0);
                mApp.setText(info.name);
                Drawable drawable = info.icon;
                drawable.setBounds(0, 0, 72, 72);
                mApp.setCompoundDrawables(drawable, null, null, null);
                view.setTag(info.uri);
                return view;
            }

            @Override
            public long getItemId(int arg0) {
                return arg0;
            }

            @Override
            public Object getItem(int arg0) {
                return mAppApps.get(arg0);
            }

            @Override
            public int getCount() {
                return mAppApps.size();
            }
        });
        mListView.setOnItemClickListener(this);
    }
    
    public void setSphoneCustomApp(int posistion) {
        mSphoneCustomApp = posistion;
    }

    private void loadAllAppsByBatch() {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final PackageManager packageManager = mContext.getPackageManager();
        List<ResolveInfo> apps = null;
        mAppApps = new ArrayList<ApplicationInfo>();

        apps = packageManager.queryIntentActivities(mainIntent, PackageManager.MATCH_DEFAULT_ONLY);

        if (apps == null) {
            return;
        }
        for (ResolveInfo info : apps) {
            ApplicationInfo aInfo = new ApplicationInfo();
            aInfo.name = info.activityInfo.loadLabel(mContext.getPackageManager()).toString();
            aInfo.icon = info.activityInfo.loadIcon(mContext.getPackageManager());

            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.setClassName(info.activityInfo.applicationInfo.packageName,
                    info.activityInfo.name);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            aInfo.uri = intent.toUri(0);
            mAppApps.add(aInfo);
        }

    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        if (mSphoneCustomApp != -1) {
            setSphoneCustomApp((String) arg1.getTag(), mSphoneCustomApp);
        }
        dismiss();
    }
    private void setSphoneCustomApp(String uri, int position) {
        SharedPreferences sp = mContext.getSharedPreferences("samsung_lock", Context.MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        spe.putString(mSettingKeyStrings[position], uri);
        spe.commit();
    }
}
