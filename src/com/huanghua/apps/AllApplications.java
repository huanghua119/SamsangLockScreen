
package com.huanghua.apps;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.huanghua.samsanglockscreen.R;

import java.util.ArrayList;
import java.util.List;

public class AllApplications extends Activity implements OnItemClickListener {

    private ListView mListView;
    private LayoutInflater mInflater;
    private ArrayList<ApplicationInfo> mAppApps;
    private ArrayList<String> mFliter;

    private class ApplicationInfo {
        String name;
        Drawable icon;
        String uri;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.app_applications_black);
        mInflater = LayoutInflater.from(this);
        mListView = (ListView) findViewById(R.id.list);
        boolean hasFilter = getIntent().hasExtra("filter_list");
        if (hasFilter) {
            mFliter = getIntent().getStringArrayListExtra("filter_list");
        }
        loadAllAppsByBatch();
        mListView.setAdapter(new BaseAdapter() {
            @Override
            public View getView(int arg0, View arg1, ViewGroup arg2) {
                View view = mInflater.inflate(R.layout.app_list_black, null);
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

    private void loadAllAppsByBatch() {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final PackageManager packageManager = getPackageManager();
        List<ResolveInfo> apps = null;
        mAppApps = new ArrayList<ApplicationInfo>();

        apps = packageManager.queryIntentActivities(mainIntent, PackageManager.PERMISSION_GRANTED);

        if (apps == null) {
            return;
        }
        for (ResolveInfo info : apps) {
            ApplicationInfo aInfo = new ApplicationInfo();
            aInfo.name = info.activityInfo.loadLabel(getPackageManager()).toString();
            aInfo.icon = info.activityInfo.loadIcon(getPackageManager());

            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.setClassName(info.activityInfo.applicationInfo.packageName,
                    info.activityInfo.name);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            aInfo.uri = intent.toUri(0);
            if (mFliter != null && mFliter.contains(aInfo.uri)) {
                continue;
            } else {
                mAppApps.add(aInfo);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        Intent data = new Intent();
        data.putExtra("app_uri", (String) arg1.getTag());
        setResult(RESULT_OK, data);
        finish();
    }

}
