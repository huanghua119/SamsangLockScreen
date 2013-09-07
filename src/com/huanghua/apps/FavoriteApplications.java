
package com.huanghua.apps;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.huanghua.samsanglockscreen.R;

public class FavoriteApplications extends Activity {

    private View mRoot;
    private FavoriteApplicationsView mFavoriteView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater mInflater = LayoutInflater.from(this);
        mRoot = mInflater.inflate(R.layout.favorite_grid_main, null);
        mFavoriteView = (FavoriteApplicationsView) mRoot.findViewById(R.id.favorite_view);
        setContentView(mRoot);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFavoriteView.onResume();
    }
}
