
package com.huanghua.apps;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huanghua.samsanglockscreen.R;

import java.net.URISyntaxException;

public class FavoriteApplicationsView extends RelativeLayout implements View.OnClickListener,
        OnItemClickListener {

    private LayoutInflater mInflater;
    private Context mContext;
    private GridView mAllApps;
    private PackageManager mPackageManager;
    private ImageView mEditView;
    private boolean mEditMode = false;

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

    private BaseAdapter mBaseAdapter = new BaseAdapter() {
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = mInflater.inflate(R.layout.grid_info, null);
            }
            ImageView delete = (ImageView) view.findViewById(R.id.port_delete_button);
            delete.setVisibility(mEditMode ? View.VISIBLE : View.GONE);
            ImageView appIcon = (ImageView) view.findViewById(R.id.port_grid_app_icon);
            TextView appName = (TextView) view.findViewById(R.id.port_grid_app_title);
            String uri = getSphoneCustomApp(position);
            if (uri == null || "".equals(uri)) {
                appIcon.setImageResource(R.drawable.add_selector);
                delete.setVisibility(View.GONE);
                appName.setText(R.string.add_apps);
                view.setTag(null);
            } else {
                try {
                    Intent intent;
                    ActivityInfo info = null;
                    if (null != uri) {
                        intent = Intent.parseUri(uri, 0);
                        info = intent
                                .resolveActivityInfo(mPackageManager,
                                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        appIcon.setImageDrawable(info.loadIcon(mPackageManager));
                        appName.setText(info.loadLabel(mPackageManager));
                        view.setTag(intent);
                    }
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
            return view;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return mSettingKeyStrings[position];
        }

        @Override
        public int getCount() {
            return mSettingKeyStrings.length;
        }
    };

    public FavoriteApplicationsView(Context context) {
        this(context, null);
    }

    public FavoriteApplicationsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mInflater = LayoutInflater.from(mContext);
        mAllApps = (GridView) findViewById(R.id.gridview);
        mEditView = (ImageView) findViewById(R.id.edit_icon);
        mEditView.setOnClickListener(this);
        mAllApps.setAdapter(mBaseAdapter);
        mAllApps.setOnItemClickListener(this);
        mPackageManager = mContext.getPackageManager();
    }

    private String getSphoneCustomApp(int position) {
        SharedPreferences sp = mContext.getSharedPreferences("samsung_lock", Context.MODE_PRIVATE);
        return sp.getString(mSettingKeyStrings[position], "");
    }

    private void setSphoneCustomApp(String uri, int position) {
        SharedPreferences sp = mContext.getSharedPreferences("samsung_lock", Context.MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        spe.putString(mSettingKeyStrings[position], uri);
        spe.commit();
    }

    @Override
    public void onClick(View v) {
        if (v == mEditView) {
            mEditMode = !mEditMode;
            mEditView.setImageResource(!mEditMode ? R.drawable.edit_selector
                    : R.drawable.check_selector);
            mBaseAdapter.notifyDataSetInvalidated();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        Object obj = arg1.getTag();
        if (obj == null) {
            Intent intent = new Intent();
            intent.setClass(mContext, AllApplications.class);
            intent.putExtra("sphone_custom_app", arg2);
            mContext.startActivity(intent);
        } else {
            if (mEditMode) {
                setSphoneCustomApp("", arg2);
                mBaseAdapter.notifyDataSetInvalidated();
            } else {
                Intent intent = (Intent) obj;
                mContext.startActivity(intent);
            }
        }
    }

    public void onResume() {
        if (mBaseAdapter != null) {
            mBaseAdapter.notifyDataSetInvalidated();
        }
    }
}
