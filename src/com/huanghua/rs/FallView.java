/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huanghua.rs;

import android.content.Context;
import android.renderscript.RSSurfaceView;
import android.renderscript.RenderScriptGL;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class FallView extends RSSurfaceView {
    private FallRS mRender;

    public FallView(Context context) {
        this(context, null);
    }

    public FallView(Context context, AttributeSet set) {
        super(context, set);
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    private RenderScriptGL mRS;

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        super.surfaceChanged(holder, format, w, h);
        if (mRS == null) {
            RenderScriptGL.SurfaceConfig sc = new RenderScriptGL.SurfaceConfig();
            mRS = createRenderScriptGL(sc);
        }

        mRS.setSurface(holder, w, h);
        mRender = new FallRS(w, h, getContext());
        mRender.init(mRS, getResources(), false);
        mRender.start();

    }

    @Override
    protected void onDetachedFromWindow() {
        if (mRS != null) {
            mRS = null;
            destroyRenderScriptGL();
        }
    }

    private float mPrevX;
    private float mPrevY;

/*    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float currX = event.getX();
        float currY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if ((event.getAction() == MotionEvent.ACTION_DOWN)
                        || ((currX - mPrevX) * (currX - mPrevX) + (currY - mPrevY)
                                * (currY - mPrevY) > 1000)) {
                    mPrevX = currX;
                    mPrevY = currY;
                    mRender.addDrop(mPrevX, mPrevY);
                    try {
                        Thread.sleep(16);
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                }
                break;
        }

        return super.onTouchEvent(event);
    }*/

    public void onMyTouchEvent(MotionEvent event) {
        float currX = event.getX();
        float currY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                if ((event.getAction() == MotionEvent.ACTION_DOWN)
                        || (event.getAction() == MotionEvent.ACTION_UP)
                        || (event.getAction() == MotionEvent.ACTION_CANCEL)
                        || ((currX - mPrevX) * (currX - mPrevX) + (currY - mPrevY)
                                * (currY - mPrevY) > 1000)) {
                    mPrevX = currX;
                    mPrevY = currY;
                    mRender.addDrop(mPrevX, mPrevY);
                    try {
                        Thread.sleep(16);
                    } catch (InterruptedException e) {
                    }
                }
                break;
        }
    }
}
