/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.huanghua.samsanglockscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.lang.ref.WeakReference;
import java.util.Calendar;

/**
 * Displays the time
 */
public class DigitalClock extends LinearLayout {

    private Context mContext;
    private Calendar mCalendar;
    private String mFormat;
    private ImageView mImgHour1;
    private ImageView mImgHour2;
    private ImageView mImgDot;
    private ImageView mImgMinute1;
    private ImageView mImgMinute2;
    private ImageView mImgAmPm;
    private ContentObserver mFormatChangeObserver;
    private int mAttached = 0; // for debugging - tells us whether attach/detach
                               // is unbalanced

    /* called by system on minute ticks */
    private final Handler mHandler = new Handler();
    private BroadcastReceiver mIntentReceiver;

    private static final int[] TIME_NUMBER_RESID = {
            R.drawable.keyguard_lockscreen_time_0,
            R.drawable.keyguard_lockscreen_time_1,
            R.drawable.keyguard_lockscreen_time_2,
            R.drawable.keyguard_lockscreen_time_3,
            R.drawable.keyguard_lockscreen_time_4,
            R.drawable.keyguard_lockscreen_time_5,
            R.drawable.keyguard_lockscreen_time_6,
            R.drawable.keyguard_lockscreen_time_7,
            R.drawable.keyguard_lockscreen_time_8,
            R.drawable.keyguard_lockscreen_time_9
    };

    private static class TimeChangedReceiver extends BroadcastReceiver {
        private WeakReference<DigitalClock> mClock;
        private Context mContext;

        public TimeChangedReceiver(DigitalClock clock) {
            mClock = new WeakReference<DigitalClock>(clock);
            mContext = clock.getContext();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            // Post a runnable to avoid blocking the broadcast.
            final boolean timezoneChanged =
                    intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED);
            final boolean localeChanged =
                    intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED);
            final DigitalClock clock = mClock.get();
            if (clock != null) {
                clock.mHandler.post(new Runnable() {
                    public void run() {
                        if (timezoneChanged) {
                            clock.mCalendar = Calendar.getInstance();
                        }
                        clock.updateTime();
                    }
                });
            } else {
                try {
                    mContext.unregisterReceiver(this);
                } catch (RuntimeException e) {
                    // Shouldn't happen
                }
            }
        }
    };

    private static class FormatChangeObserver extends ContentObserver {
        private WeakReference<DigitalClock> mClock;
        private Context mContext;

        public FormatChangeObserver(DigitalClock clock) {
            super(new Handler());
            mClock = new WeakReference<DigitalClock>(clock);
            mContext = clock.getContext();
        }

        @Override
        public void onChange(boolean selfChange) {
            DigitalClock digitalClock = mClock.get();
            if (digitalClock != null) {
                digitalClock.updateTime();
            } else {
                try {
                    mContext.getContentResolver().unregisterContentObserver(this);
                } catch (RuntimeException e) {
                    // Shouldn't happen
                }
            }
        }
    }

    public DigitalClock(Context context) {
        this(context, null);
    }

    public DigitalClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mCalendar = Calendar.getInstance();

        mImgHour1 = (ImageView) findViewById(R.id.ic_time_hour1);
        mImgHour2 = (ImageView) findViewById(R.id.ic_time_hour2);
        mImgDot = (ImageView) findViewById(R.id.ic_time_dot);
        mImgMinute1 = (ImageView) findViewById(R.id.ic_time_minute1);
        mImgMinute2 = (ImageView) findViewById(R.id.ic_time_minute2);
        mImgAmPm = (ImageView) findViewById(R.id.ic_time_ampm);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mAttached++;

        /* monitor time ticks, time changed, timezone */
        if (mIntentReceiver == null) {
            mIntentReceiver = new TimeChangedReceiver(this);
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            filter.addAction(Intent.ACTION_LOCALE_CHANGED);
            mContext.registerReceiver(mIntentReceiver, filter);
        }

        /* monitor 12/24-hour display preference */
        if (mFormatChangeObserver == null) {
            mFormatChangeObserver = new FormatChangeObserver(this);
            mContext.getContentResolver().registerContentObserver(
                    Settings.System.CONTENT_URI, true, mFormatChangeObserver);
        }

        updateTime();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        mAttached--;

        if (mIntentReceiver != null) {
            mContext.unregisterReceiver(mIntentReceiver);
        }
        if (mFormatChangeObserver != null) {
            mContext.getContentResolver().unregisterContentObserver(
                    mFormatChangeObserver);
        }

        mFormatChangeObserver = null;
        mIntentReceiver = null;
    }

    void updateTime(Calendar c) {
        mCalendar = c;
        updateTime();
    }

    private void updateTime() {
        mCalendar.setTimeInMillis(System.currentTimeMillis());

        boolean is24Format = android.text.format.DateFormat.is24HourFormat(getContext());
        int nCalendarHour = mCalendar.get(Calendar.HOUR);
        int nCalendarMinute = mCalendar.get(Calendar.MINUTE);
        int nCalendarAmPm = mCalendar.get(Calendar.AM_PM);
        int mHour1 = -1;
        int mHour2 = -1;
        int mMinute1 = -1;
        int mMinute2 = -1;

        if (is24Format) {
            if (nCalendarAmPm == 1) {
                nCalendarHour += 12;
            }

            mHour1 = nCalendarHour / 10;
            mHour2 = nCalendarHour % 10;

            mMinute1 = nCalendarMinute / 10;
            mMinute2 = nCalendarMinute % 10;

            mImgAmPm.setVisibility(View.INVISIBLE);

            if (mHour1 >= 0 && mHour1 <= 9) {
                mImgHour1.setImageResource(TIME_NUMBER_RESID[mHour1]);
            }

            if (mHour2 >= 0 && mHour2 <= 9) {
                mImgHour2.setImageResource(TIME_NUMBER_RESID[mHour2]);
            }

            if (mMinute1 >= 0 && mMinute1 <= 9) {
                mImgMinute1.setImageResource(TIME_NUMBER_RESID[mMinute1]);
            }

            if (mMinute2 >= 0 && mMinute2 <= 9) {
                mImgMinute2.setImageResource(TIME_NUMBER_RESID[mMinute2]);
            }
        } else {
            mHour1 = nCalendarHour / 10;
            mHour2 = nCalendarHour % 10;

            mMinute1 = nCalendarMinute / 10;
            mMinute2 = nCalendarMinute % 10;

            if (mHour1 >= 0 && mHour1 <= 2) {
                if (mHour1 == 0) {
                    mImgHour1.setVisibility(View.INVISIBLE);
                } else {
                    mImgHour1.setVisibility(View.VISIBLE);
                    mImgHour1.setImageResource(TIME_NUMBER_RESID[mHour1]);
                }
            }

            if (mHour2 >= 0 && mHour2 <= 9) {
                mImgHour2.setImageResource(TIME_NUMBER_RESID[mHour2]);
            }

            if (mMinute1 >= 0 && mMinute1 <= 9) {
                mImgMinute1.setImageResource(TIME_NUMBER_RESID[mMinute1]);
            }

            if (mMinute2 >= 0 && mMinute2 <= 9) {
                mImgMinute2.setImageResource(TIME_NUMBER_RESID[mMinute2]);
            }

            if (nCalendarAmPm == 1) {
                mImgAmPm.setImageResource(R.drawable.keyguard_lockscreen_time_pm);
            } else {
                mImgAmPm.setImageResource(R.drawable.keyguard_lockscreen_time_am);
            }
        }
    }
}
