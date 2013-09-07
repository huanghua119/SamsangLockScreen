
package com.huanghua.samsanglockscreen;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.ActivityManager;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huanghua.rs.FallView;

import java.io.File;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;

class SamsangLockScreen extends LinearLayout {
    private static final int ON_RESUME_PING_DELAY = 500; // delay first ping
                                                         // until the screen is
                                                         // on
    private static final boolean DBG = false;
    private static final String TAG = "LockScreen";
    private static final String ENABLE_MENU_KEY_FILE = "/data/local/enable_menu_key";
    private static final int WAIT_FOR_ANIMATION_TIMEOUT = 0;
    private static final int STAY_ON_WHILE_GRABBED_TIMEOUT = 30000;
    private static final String ASSIST_ICON_METADATA_NAME =
            "com.android.systemui.action_assist_icon";
    private boolean mEnableRingSilenceFallback = false;

    // current configuration state of keyboard and display
    private int mCreationOrientation;
    private TextView mCarrierView;
    private TextView mCarrierGeminiView;
    private TextView mCarrierDivider;
    private CharSequence mCarrierText;
    private CharSequence mCarrierGeminiText;
    private CharSequence mCarrierHelpText;
    private CharSequence mPlmn;
    private CharSequence mSpn;
    private boolean mSilentMode;
    private AudioManager mAudioManager;
    private boolean mEnableMenuKeyInLockScreen;
    private boolean mUnlockDisabledDueToSimState;

    private TextView mDate;
    private TextView mStatusView;
    private TextView mAlarmStatusView;
    private boolean mShowingBatteryInfo = false;

    // last known plugged in state
    private boolean mPluggedIn = false;

    // last known battery level
    private int mBatteryLevel = 100;

    private String mNextAlarm = null;
    private Drawable mAlarmIcon = null;
    private String mCharging = null;
    private Drawable mChargingIcon = null;

    private String mDateFormatString;
    private java.text.DateFormat mTimeFormat;
    private CharSequence mOwnerInfoText;

    private TextView mUnlockText;
    private ImageView mFlareShadow;

    private boolean mTimeAreaTouched = false;
    private boolean mFlareShadowAreaTouched = false;
    private boolean mTouchedDown = false;
    private boolean mMoveUnlock = false;
    private Bitmap WallpaperBitmap;
    private Bitmap mBitmap;
    private int mIconTouchedIndex = -1;
    private float mDownX = 0;
    private float mDownY = 0;
    private int mUpX = 0;
    private int mUpY = 0;
    private int mStatusBarHeight = 38;
    private int mScreenWidth = 480;
    private int mScreenHeight = 800;

    private SoundPool soundPool = null;
    private HashMap hashMap = null;

    private static final int TOUCH_ICON_MISSED_PHONE = 0;
    private static final int TOUCH_ICON_MISSED_SMS = 1;
    private static final int TOUCH_FLARE_SHADOW = 2;

    private static final int[][] TOUCH_ICON_RECT = new int[3][4];

    private final Object sLock = new Object();
    private Handler sHandler;
    private boolean mIsRunningSMS = false;
    private boolean mNeedRequerySMS = false;
    private boolean mIsRunningMMS = false;
    private boolean mNeedRequeryMMS = false;

    private int missedPhoneMax = 5;
    private String missedPhoneNameString = null;
    private String missedPhoneNameArray[] = {
            null, null, null, null, null
    };
    private int mUnreadMsg = 0;
    private int mUnreadMMS = 0;
    private int mUnreadSMS = 0;

    // messages for the handler
    private static final int MSG_SMS_QUERY = 301;
    private static final int MSG_MMS_QUERY = 302;
    private static final int MSG_SMS_QUERY_END = 303;
    private static final int MSG_MMS_QUERY_END = 304;
    private static final int MSG_TIME_ROTATE = 305;
    private static final int MSG_UNLOCK_DELAYED = 306;
    private static final int MSG_FLARE_HIDE_DELAYED = 307;

    private static final int THREAD_TYPE_MMS = 1;
    private static final int THREAD_TYPE_SMS = 2;

    private FrameLayout mFlareFrameLayout;
    private FrameLayout mTimeFrameLayout;
    private LinearLayout mTimeLinearLayout;
    private LinearLayout mDateLayout;
    private LinearLayout mEventLinearLayout;
    private LinearLayout mEventMissedPhoneLinearLayout;
    private LinearLayout mEventMissedSmsLinearLayout;
    private LinearLayout mCarrierLinearLayout;
    private LinearLayout mLockLinearLayout;
    private ImageView mTimeFrameBg;
    private TextView mEventDate;
    private TextView mEventMissedPhoneTitle;
    private TextView mEventMissedPhoneBody;
    private TextView mEventMissedSmsBody;

    private float timeRotateAngle = 0.0f;
    private final float TIME_ROTATEANGLE_MAX = 45.0f;
    private final int HEXAGON_TOTAL = 6;
    private final int MAX_ALPHA_DISTANCE = 1500;
    private int X_OFFSET = 0;
    private int Y_OFFSET = 0;
    private float currentX;
    private float currentY;
    private double distance;
    private float distancePerMaxAlpha;
    private ValueAnimator fadeOutAnimator;
    private float fadeoutAnimationValue;
    private float fogAlpha;
    private float fogAnimationValue;
    private ValueAnimator fogOnAnimator;
    private ImageViewBlended[] hexagon;
    private float[] hexagonDistance;
    private int[] hexagonRotation;
    private float[] hexagonScale;
    private ImageViewBlended hoverLight;
    private float hoverLightAnimationValue;
    private ValueAnimator hoverLightInAnimator;
    private ValueAnimator hoverLightOutAnimator;
    private float hoverX;
    private float hoverY;
    private boolean isTouched = false;
    private ImageViewBlended mFlareLight;
    private FrameLayout lightObj;
    private FrameLayout lightTap;
    private ImageViewBlended mflareLong;
    private Context mContext;
    private double mMoveDistance = 0.0D;
    private float objAlpha;
    private float objAnimationValue;
    private ValueAnimator objAnimator;
    private ImageViewBlended mFlareParticle;
    private ImageViewBlended mFlareRainbow;
    private float randomRotation;
    private ImageViewBlended mFlareRing;
    private float showStartX;
    private float showStartY;
    private float tapAnimationValue;
    private ValueAnimator tapAnimator;
    private Point[] tapHexRandomPoint;
    private ImageViewBlended[] tapHexagon;
    private float[] tapHexagonScale;
    private float unlockAnimationValue;
    private ValueAnimator unlockAnimator;
    private ImageView mFlareVignetting;
    private float vignettingAlpha;
    private boolean mIsFallRs;
    private FallView mFallView;
    private FrameLayout mWaterlayout;
    private TextView mLogoText;

    public SamsangLockScreen(Context context) {
        this(context, null);
    }

    public SamsangLockScreen(Context context, AttributeSet attrs) {
        super(context, attrs);

        synchronized (sLock) {
            sHandler = new Handler() {
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case MSG_SMS_QUERY:
                            handleSmsQuery();
                            break;
                        case MSG_MMS_QUERY:
                            handleMmsQuery();
                            break;
                        case MSG_SMS_QUERY_END:
                            handleSmsQueryEnd(msg.arg1);
                            break;
                        case MSG_MMS_QUERY_END:
                            handleMmsQueryEnd(msg.arg1);
                            break;
                        case MSG_TIME_ROTATE:
                            if (!mTouchedDown) {
                                if (Math.abs(timeRotateAngle) <= 5.0f) {
                                    timeRotateAngle = 0.0f;
                                    mTimeFrameLayout.setRotationY(timeRotateAngle);
                                    new TouchAnimation(mTimeFrameBg).showAlphaAnimation(1.0f, 0.0f,
                                            500, null);
                                    mTimeFrameBg.setVisibility(View.INVISIBLE);
                                    mDateLayout.setDrawingCacheEnabled(false);
                                } else {
                                    if (timeRotateAngle > 0.0f) {
                                        timeRotateAngle = timeRotateAngle - 5.0f;
                                    } else {
                                        timeRotateAngle = timeRotateAngle + 5.0f;
                                    }
                                    mTimeFrameLayout
                                            .setPivotX(mTimeFrameLayout.getMeasuredWidth() * 0.5f);
                                    mTimeFrameLayout
                                            .setPivotY(mTimeFrameLayout.getMeasuredHeight() * 0.5f);
                                    mTimeFrameLayout.setRotationY(0 - timeRotateAngle);
                                    sendMessageDelayedToHandler(MSG_TIME_ROTATE, 50);
                                }
                            }
                            break;
                        case MSG_UNLOCK_DELAYED:
                            gotoUnlockAction(mIconTouchedIndex);
                            System.exit(0);
                            break;
                        case MSG_FLARE_HIDE_DELAYED:
                            if (mIsFallRs) {

                            } else {
                                flareHide();
                                hoverExit();
                            }
                            break;
                    }
                }
            };
        }
        mContext = context;
        mIsFallRs = getLockEffect() == 0 ? true : false;
        mEnableMenuKeyInLockScreen = shouldEnableMenuKey();

        final LayoutInflater inflater = LayoutInflater.from(context);

        inflater.inflate(R.layout.keyguard_screen_tab_unlock, this, true);

        /*
         * mStatusViewManager = new KeyguardStatusViewManager(this,
         * mUpdateMonitor, mLockPatternUtils, mCallback, false);
         */

        setFocusable(true);
        setFocusableInTouchMode(true);
        setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);

        /*
         * DisplayMetrics dm = getResources().getDisplayMetrics();
         * WallpaperManager wm = WallpaperManager.getInstance(context);
         * WallpaperBitmap = ((BitmapDrawable) wm.getDrawable()).getBitmap(); if
         * (WallpaperBitmap != null) { if (mBitmap != null) { mBitmap.recycle();
         * mBitmap = null; } mBitmap = Bitmap.createBitmap(dm.widthPixels,
         * dm.heightPixels, Bitmap.Config.ARGB_8888); Canvas localCanvas = new
         * Canvas(); localCanvas.setBitmap(mBitmap);
         * localCanvas.drawBitmap(WallpaperBitmap, 0, 0, null); }
         */
        mCarrierView = (TextView) findViewById(R.id.carrier);
        mCarrierGeminiView = (TextView) findViewById(R.id.carrierGemini);
        mCarrierDivider = (TextView) findViewById(R.id.carrierDivider);
        mCarrierDivider.setText("|");

        mDate = (TextView) findViewById(R.id.date);
        mStatusView = (TextView) findViewById(R.id.status1);
        mAlarmStatusView = (TextView) findViewById(R.id.alarm_status);

        mFlareFrameLayout = (FrameLayout) findViewById(R.id.flareframelayout);
        mTimeFrameLayout = (FrameLayout) findViewById(R.id.timeframelayout);
        mTimeLinearLayout = (LinearLayout) findViewById(R.id.timelayout);
        mDateLayout = (LinearLayout) findViewById(R.id.datelayout);
        mEventLinearLayout = (LinearLayout) findViewById(R.id.eventlayout);
        mEventMissedPhoneLinearLayout = (LinearLayout) findViewById(R.id.event_missedphone_layout);
        mEventMissedSmsLinearLayout = (LinearLayout) findViewById(R.id.event_missedsms_layout);
        mEventMissedPhoneTitle = (TextView) findViewById(R.id.event_missedphone_title);
        mEventMissedPhoneBody = (TextView) findViewById(R.id.event_missedphone_body);
        mEventMissedSmsBody = (TextView) findViewById(R.id.event_missedsms_body);
        mCarrierLinearLayout = (LinearLayout) findViewById(R.id.carrierlayout);
        mLockLinearLayout = (LinearLayout) findViewById(R.id.locklayout);
        mEventDate = (TextView) findViewById(R.id.event_date);
        mTimeFrameBg = (ImageView) findViewById(R.id.timeframe_bg);
        mUnlockText = (TextView) findViewById(R.id.unlock);
        mFlareShadow = (ImageView) findViewById(R.id.flare_shadow);

        sendMessageToHandler(MSG_SMS_QUERY);
        sendMessageToHandler(MSG_MMS_QUERY);
        registerObserver();

        mTouchedDown = false;
        mMoveUnlock = false;
        initTouchIconRect();
        initMissedPhone();
        initMissedMsg();
        playSoundsInit(context);
        flareInit();
        mWaterlayout = (FrameLayout) findViewById(R.id.waterlayout);
        mFallView = (FallView) mWaterlayout.findViewById(R.id.fall_view);
        if (mIsFallRs) {
            // mWaterlayout.setVisibility(View.VISIBLE);
        } else {
            // mWaterlayout.setVisibility(View.GONE);
        }
        mLogoText = (TextView) findViewById(R.id.logo);
        setBackgroundColor(0x00000000);
    }

    private boolean shouldEnableMenuKey() {
        final Resources res = getResources();
        final boolean configDisabled = false;
        final boolean isTestHarness = ActivityManager.isRunningInTestHarness();
        final boolean fileOverride = (new File(ENABLE_MENU_KEY_FILE)).exists();
        return !configDisabled || isTestHarness || fileOverride;
    }

    private void initTouchIconRect() {
        TOUCH_ICON_RECT[TOUCH_ICON_MISSED_PHONE][0] = 25;
        TOUCH_ICON_RECT[TOUCH_ICON_MISSED_PHONE][1] = 60;
        TOUCH_ICON_RECT[TOUCH_ICON_MISSED_PHONE][2] = mScreenWidth - 25;
        TOUCH_ICON_RECT[TOUCH_ICON_MISSED_PHONE][3] = 150;

        TOUCH_ICON_RECT[TOUCH_ICON_MISSED_SMS][0] = 25;
        TOUCH_ICON_RECT[TOUCH_ICON_MISSED_SMS][1] = 160;
        TOUCH_ICON_RECT[TOUCH_ICON_MISSED_SMS][2] = mScreenWidth - 25;
        TOUCH_ICON_RECT[TOUCH_ICON_MISSED_SMS][3] = 250;

        TOUCH_ICON_RECT[TOUCH_FLARE_SHADOW][0] = 0;
        TOUCH_ICON_RECT[TOUCH_FLARE_SHADOW][1] = 280;
        TOUCH_ICON_RECT[TOUCH_FLARE_SHADOW][2] = mScreenWidth;
        TOUCH_ICON_RECT[TOUCH_FLARE_SHADOW][3] = 700;
    }

    /*
     * @Override protected void onDraw(Canvas canvas) { super.onDraw(canvas); if
     * (mBitmap != null) { canvas.drawBitmap(mBitmap, 0, 0 - mStatusBarHeight,
     * null); postInvalidate(); } }
     */

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getRawX();
        float y = event.getRawY();
        int mMoveX = (int) (x - mDownX);
        int mMoveY = (int) (y - mDownY);

        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
                mDownX = x;
                mDownY = y;
                mTouchedDown = true;
                if (y < mTimeFrameLayout.getBottom()) {
                    if (mEventLinearLayout.getVisibility() == View.VISIBLE) {
                        if (x > TOUCH_ICON_RECT[TOUCH_ICON_MISSED_PHONE][0]
                                && x < TOUCH_ICON_RECT[TOUCH_ICON_MISSED_PHONE][2]
                                && y > TOUCH_ICON_RECT[TOUCH_ICON_MISSED_PHONE][1]
                                && y < TOUCH_ICON_RECT[TOUCH_ICON_MISSED_PHONE][3]) {
                            if (mEventMissedPhoneLinearLayout.getVisibility() == View.VISIBLE) {
                                mIconTouchedIndex = TOUCH_ICON_MISSED_PHONE;
                            } else {
                                mIconTouchedIndex = TOUCH_ICON_MISSED_SMS;
                            }
                        } else if (x > TOUCH_ICON_RECT[TOUCH_ICON_MISSED_SMS][0]
                                && x < TOUCH_ICON_RECT[TOUCH_ICON_MISSED_SMS][2]
                                && y > TOUCH_ICON_RECT[TOUCH_ICON_MISSED_SMS][1]
                                && y < TOUCH_ICON_RECT[TOUCH_ICON_MISSED_SMS][3]) {
                            if (mEventMissedSmsLinearLayout.getVisibility() == View.VISIBLE) {
                                mIconTouchedIndex = TOUCH_ICON_MISSED_SMS;
                            }
                        }
                    }
                    mTimeAreaTouched = true;
                } else {
                    mTimeAreaTouched = false;
                    mFlareFrameLayout.setVisibility(View.VISIBLE);
                    if (x > TOUCH_ICON_RECT[TOUCH_FLARE_SHADOW][0]
                            && x < TOUCH_ICON_RECT[TOUCH_FLARE_SHADOW][2]
                            && y > TOUCH_ICON_RECT[TOUCH_FLARE_SHADOW][1]
                            && y < TOUCH_ICON_RECT[TOUCH_FLARE_SHADOW][3]) {
                        if (mFlareShadow.getVisibility() == View.VISIBLE) {
                            mFlareShadowAreaTouched = true;
                        }
                    }
                }

                if (mTimeAreaTouched) {
                    if (mTimeLinearLayout.getVisibility() == View.VISIBLE) {
                        mTimeFrameLayout.setCameraDistance(0.0f);
                        mTimeFrameLayout.setRotationY(0.0f);
                        mTimeFrameBg.setVisibility(View.VISIBLE);
                        mStatusView.setVisibility(View.INVISIBLE);
                        mUnlockText.setVisibility(View.INVISIBLE);
                        mCarrierLinearLayout.setVisibility(View.INVISIBLE);
                        mLockLinearLayout.setVisibility(View.VISIBLE);
                    } else if (mFlareShadow.getVisibility() == View.VISIBLE) {
                        mIconTouchedIndex = -1;
                        showEventLayoutScaleAnimation(false);
                        mFlareShadow.setVisibility(View.INVISIBLE);
                        mStatusView.setVisibility(View.VISIBLE);
                        mCarrierLinearLayout.setVisibility(View.VISIBLE);
                    } else if (mIconTouchedIndex >= 0) {
                        showEventLayoutScaleAnimation(true);
                        mFlareShadow.setVisibility(View.VISIBLE);
                        mStatusView.setVisibility(View.INVISIBLE);
                        mCarrierLinearLayout.setVisibility(View.INVISIBLE);
                    }
                } else {
                    if (mIsFallRs) {
                        mFallView.onMyTouchEvent(event);
                        mMoveDistance = 0.0D;
                    } else {
                        mMoveDistance = 0.0D;
                        flareShow(mDownX, mDownY);
                        hoverEnter(mDownX, mDownY);
                        playSoundsTouchDown();
                    }
                }

                break;
            case MotionEvent.ACTION_MOVE:
                if (mTimeAreaTouched) {
                    if (mTimeLinearLayout.getVisibility() == View.VISIBLE) {
                        mTimeFrameLayout.setCameraDistance(6000.0f);
                        mTimeFrameLayout.setPivotX(mTimeFrameLayout.getMeasuredWidth() * 0.5f);
                        mTimeFrameLayout.setPivotY(mTimeFrameLayout.getMeasuredHeight() * 0.5f);
                        mDateLayout.setDrawingCacheEnabled(true);
                        timeRotateAngle = 2 * TIME_ROTATEANGLE_MAX * mMoveX
                                / mTimeFrameLayout.getMeasuredWidth();
                        if (Math.abs(timeRotateAngle) > TIME_ROTATEANGLE_MAX) {
                            if (timeRotateAngle > 0.0f) {
                                timeRotateAngle = TIME_ROTATEANGLE_MAX;
                            } else {
                                timeRotateAngle = 0 - TIME_ROTATEANGLE_MAX;
                            }
                        }
                        mTimeFrameLayout.setRotationY(0 - timeRotateAngle);
                    }
                } else {
                    mMoveDistance = Math.sqrt(Math.pow(mMoveX, 2.0D) + Math.pow(mMoveY, 2.0D));
                    if (mMoveDistance >= 300) {
                        flareUnlock();
                    } else if (mFlareShadowAreaTouched) {
                        if (!(x > TOUCH_ICON_RECT[TOUCH_FLARE_SHADOW][0]
                                && x < TOUCH_ICON_RECT[TOUCH_FLARE_SHADOW][2]
                                && y > TOUCH_ICON_RECT[TOUCH_FLARE_SHADOW][1] && y < TOUCH_ICON_RECT[TOUCH_FLARE_SHADOW][3])) {
                            flareUnlock();
                        } else {
                            if (mIsFallRs) {
                                mFallView.onMyTouchEvent(event);
                            } else {
                                flareMove(x, y);
                                hoverMove(x, y);
                            }
                        }
                    } else {
                        if (mIsFallRs) {
                            mFallView.onMyTouchEvent(event);
                        } else {
                            flareMove(x, y);
                            hoverMove(x, y);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                mTouchedDown = false;
                if (mTimeAreaTouched) {
                    if (mTimeLinearLayout.getVisibility() == View.VISIBLE) {
                        sendMessageDelayedToHandler(MSG_TIME_ROTATE, 100);
                        mLockLinearLayout.setVisibility(View.INVISIBLE);
                        mStatusView.setVisibility(View.VISIBLE);
                        mUnlockText.setVisibility(View.VISIBLE);
                        mCarrierLinearLayout.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (mIsFallRs) {
                        mFallView.onMyTouchEvent(event);
                    }
                    mMoveDistance = Math.sqrt(Math.pow(mMoveX, 2.0D) + Math.pow(mMoveY, 2.0D));
                    if (mMoveUnlock) {
                        sendMessageToHandler(MSG_FLARE_HIDE_DELAYED);
                    } else {
                        sendMessageDelayedToHandler(MSG_FLARE_HIDE_DELAYED, 100);
                    }
                }
                mMoveUnlock = false;
                break;
        }
        mLockLinearLayout.setVisibility(View.VISIBLE);
        mStatusView.setVisibility(View.GONE);
        mUnlockText.setVisibility(View.GONE);
        mCarrierLinearLayout.setVisibility(View.GONE);
        return true;
    }

    public void goToPhoneAction() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        getContext().startActivity(intent);
    }

    public void goToSmsAction() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        ComponentName comp = new ComponentName("com.android.mms",
                "com.android.mms.ui.ConversationList");
        intent.setComponent(comp);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        getContext().startActivity(intent);
    }

    public void goToBrowserAction() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        ComponentName comp = new ComponentName("com.android.browser",
                "com.android.browser.BrowserActivity");
        intent.setComponent(comp);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        getContext().startActivity(intent);
    }

    public void goToCameraAction() {
        Intent intent = new Intent("android.media.action.STILL_IMAGE_CAMERA");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        getContext().startActivity(intent);
    }

    public void goToMissedPhoneAction() {
        Intent intent = new Intent(Intent.ACTION_VIEW, null);
        intent.setType("vnd.android.cursor.dir/calls");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        getContext().startActivity(intent);
    }

    public void gotoUnlockAction(int mIndex) {
        switch (mIndex) {
            case TOUCH_ICON_MISSED_PHONE:
                goToMissedPhoneAction();
                break;

            case TOUCH_ICON_MISSED_SMS:
                goToSmsAction();
                break;
        }
    }

    private void playSoundsInit(Context paramContext)
    {
        if (soundPool == null) {
            soundPool = new SoundPool(4, 1, 100);
        }

        if (hashMap == null) {
            HashMap localHashMap = new HashMap();
            hashMap = localHashMap;
            hashMap.put(Integer.valueOf(1),
                    Integer.valueOf(soundPool.load(paramContext, R.raw.down, 1)));
            hashMap.put(Integer.valueOf(2),
                    Integer.valueOf(soundPool.load(paramContext, R.raw.up, 1)));
        }
    }

    private void playSoundsRelease()
    {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        if (hashMap != null) {
            hashMap.clear();
            hashMap = null;
        }
    }

    private void playSoundsTouchDown()
    {
        if ((soundPool != null) && (hashMap != null)) {
            soundPool.play(((Integer) hashMap.get(Integer.valueOf(1))).intValue(), 1.0F, 1.0F, 0,
                    0, 1.0F);
        }
    }

    private void playSoundsTouchUp() {
        if ((soundPool != null) && (hashMap != null)) {
            soundPool.play(((Integer) hashMap.get(Integer.valueOf(2))).intValue(), 1.0F, 1.0F, 0,
                    0, 1.0F);
        }
    }

    public void showEventLayoutScaleAnimation(final boolean enter) {
        mEventLinearLayout.setPivotX(mTimeFrameLayout.getMeasuredWidth() * 0.5f);
        mEventLinearLayout.setPivotY(mTimeFrameLayout.getMeasuredHeight() * 0.5f);

        float[] arrayOfFloat = new float[2];
        if (enter) {
            arrayOfFloat[0] = 1.0f;
            arrayOfFloat[1] = 0.75f;
        } else {
            arrayOfFloat[0] = 0.75f;
            arrayOfFloat[1] = 1.0f;
        }
        final ValueAnimator anim = ValueAnimator.ofFloat(arrayOfFloat);
        anim.setDuration(200);
        anim.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float scale = ((Float) animation.getAnimatedValue()).floatValue();
                mEventLinearLayout.setScaleX(scale);
                mEventLinearLayout.setScaleY(scale);
                mEventLinearLayout.setTranslationY(scale);
            }
        });
        anim.start();
    }

    private final ContentObserver sSMSObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            sendMessageToHandler(MSG_SMS_QUERY);
        }
    };

    private final ContentObserver sMMSObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            sendMessageToHandler(MSG_MMS_QUERY);
        }
    };

    private synchronized void handleSmsQuery() {
        if (!mIsRunningSMS) {
            mIsRunningSMS = true;
            getThread(THREAD_TYPE_SMS).start();
        } else {
            mNeedRequerySMS = true;
        }
    }

    private synchronized void handleSmsQueryEnd(int unreadSMS) {
        mUnreadSMS = unreadSMS;
        mUnreadMsg = mUnreadMMS + unreadSMS;
        refreshMissedMsg(mUnreadMsg);
        mIsRunningSMS = false;
        if (mNeedRequerySMS) {
            mNeedRequerySMS = false;
            getThread(THREAD_TYPE_SMS).start();
            mIsRunningSMS = true;
        }
    }

    private synchronized void handleMmsQuery() {
        if (!mIsRunningMMS) {
            mIsRunningMMS = true;
            getThread(THREAD_TYPE_MMS).start();
        } else {
            mNeedRequeryMMS = true;
        }
    }

    private synchronized void handleMmsQueryEnd(int unreadMMS) {
        mUnreadMMS = unreadMMS;
        mUnreadMsg = mUnreadSMS + unreadMMS;
        refreshMissedMsg(mUnreadMsg);
        mIsRunningMMS = false;
        if (mNeedRequeryMMS) {
            mNeedRequeryMMS = false;
            getThread(THREAD_TYPE_MMS).start();
            mIsRunningMMS = true;
        }
    }

    private void addMissedPhone(String callName) {
        String tempString = null;
        String[] tempArray = new String[missedPhoneMax];
        int mMissedPhone = 0;

        if (callName == null) {
            return;
        }

        for (int i = 0; i < missedPhoneMax; i++) {
            tempArray[i] = missedPhoneNameArray[i];
        }
        missedPhoneNameArray[0] = callName;
        missedPhoneNameString = callName;

        int j = 1;
        for (int i = 0; i < missedPhoneMax; i++) {
            if (callName.equals(tempArray[i])) {
            } else {
                missedPhoneNameArray[j] = tempArray[i];
                j++;
                if (j == missedPhoneMax) {
                    break;
                }
            }
        }

        for (int i = 1; i < missedPhoneMax; i++) {
            tempString = missedPhoneNameArray[i];
            if (tempString != null) {
                missedPhoneNameString = missedPhoneNameString + ", " + tempString;
            }
        }
    }

    private void initMissedPhone() {
        final String[] projection = null;
        final String selection = null;
        final String[] selectionArgs = null;
        final String sortOrder = android.provider.CallLog.Calls.DATE + " DESC";
        Cursor cursor = null;
        String tempString = null;
        int mCount = 0;
        int mTotalCount = 0;
        int mMissedPhone = 0;

        if (mMissedPhone <= 0) {
            refreshMissedPhone(mMissedPhone);
            return;
        }

        if (mCount > 1) {
            missedPhoneNameString = missedPhoneNameArray[0];
            for (int i = 1; i < mCount; i++) {
                tempString = missedPhoneNameArray[i];
                missedPhoneNameString = missedPhoneNameString + "," + tempString;
            }
        } else {
            missedPhoneNameString = missedPhoneNameArray[0];
        }
        refreshMissedPhone(mMissedPhone);
    }

    /**
     * update unread information
     */
    private synchronized void refreshMissedMsg(int mUnreadMsg) {
        if (mUnreadMsg > 0) {
            mTimeLinearLayout.setVisibility(View.INVISIBLE);
            mEventMissedSmsLinearLayout.setVisibility(View.VISIBLE);
            mEventLinearLayout.setVisibility(View.VISIBLE);
            if (mUnreadMsg == 1) {
                mEventMissedSmsBody.setText(R.string.lock_sms_new);
            } else {
                mEventMissedSmsBody.setText(getContext().getString(R.string.lock_sms_count,
                        mUnreadMsg));
            }
        } else {
            mEventMissedSmsBody.setText("");
            mEventMissedSmsLinearLayout.setVisibility(View.GONE);
            if (10 <= 0) {
                mEventLinearLayout.setVisibility(View.INVISIBLE);
                mTimeLinearLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private synchronized void refreshMissedPhone(int mMissedPhone) {
        if (mMissedPhone > 0) {
            mTimeLinearLayout.setVisibility(View.INVISIBLE);
            mEventMissedPhoneLinearLayout.setVisibility(View.VISIBLE);
            mEventLinearLayout.setVisibility(View.VISIBLE);
            if (mMissedPhone == 1) {
                mEventMissedPhoneTitle.setText(R.string.lock_call);
            } else {
                mEventMissedPhoneTitle.setText(getContext().getString(R.string.lock_call_count,
                        mMissedPhone));
            }
            mEventMissedPhoneBody.setText(missedPhoneNameString);
        } else {
            mEventMissedPhoneTitle.setText("");
            mEventMissedPhoneBody.setText("");
            mEventMissedPhoneLinearLayout.setVisibility(View.GONE);
            if (mUnreadMsg <= 0) {
                mEventLinearLayout.setVisibility(View.INVISIBLE);
                mTimeLinearLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private void sendMessageToHandler(int what) {
        synchronized (sLock) {
            if (sHandler != null) {
                sHandler.removeMessages(what);
                sHandler.sendMessage(sHandler.obtainMessage(what));
            }
        }
    }

    private void sendMessageToHandler(int what, int arg1, int arg2) {
        synchronized (sLock) {
            if (sHandler != null) {
                sHandler.removeMessages(what);
                sHandler.sendMessage(sHandler.obtainMessage(what, arg1, arg2));
            }
        }
    }

    private void sendMessageDelayedToHandler(int what, long delayMillis) {
        synchronized (sLock) {
            if (sHandler != null) {
                sHandler.removeMessages(what);
                sHandler.sendEmptyMessageDelayed(what, delayMillis);
            }
        }
    }

    private void registerObserver() {
        // we only need one observer!!
        unregisterObserver();

        getContext().getContentResolver().registerContentObserver(Uri.parse("content://sms"), true,
                sSMSObserver);

        getContext().getContentResolver().registerContentObserver(Uri.parse("content://mms-sms/"),
                true, sMMSObserver);
    }

    private synchronized void unregisterObserver() {
        try {
            if (sSMSObserver != null) {
                getContext().getContentResolver().unregisterContentObserver(sSMSObserver);
            }

            if (sMMSObserver != null) {
                getContext().getContentResolver().unregisterContentObserver(sMMSObserver);
            }
        } catch (Exception e) {
            Log.e(TAG, "unregisterObserver fail");
        }
    }

    private Thread getThread(int type) {
        Thread thread = null;
        if (type == THREAD_TYPE_MMS) {
            thread = new Thread(new Runnable() {
                public void run() {
                    updateMMSInfo();
                }
            });

        } else if (type == THREAD_TYPE_SMS) {
            thread = new Thread(new Runnable() {
                public void run() {
                    updateSMSInfo();
                }
            });

        }
        return thread;
    }

    private void updateSMSInfo() {
        int total = -1;
        int number = -1 == total ? 0 : total;
        sendMessageToHandler(MSG_SMS_QUERY_END, number, 0);
    }

    private void updateMMSInfo() {
        int total = -1;
        int number = -1 == total ? 0 : total;
        sendMessageToHandler(MSG_MMS_QUERY_END, number, 0);
    }

    private void initMissedMsg() {
        int smsTotal = -1;
        int mmsTotal = -1;
        mUnreadMMS = -1 == mmsTotal ? 0 : mmsTotal;

        refreshMissedMsg(mUnreadSMS + mUnreadMMS);
    }

    private class TouchAnimation extends Animation {
        private ImageView imageTarget;

        TouchAnimation(ImageView imageView) {
            imageTarget = imageView;
        }

        void showRotateAnimation(float fromDegrees, float toDegrees, long durationMillis,
                AnimationListener listener) {
            RotateAnimation rotateAnimation = new RotateAnimation(fromDegrees, toDegrees,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(durationMillis);
            rotateAnimation.setAnimationListener(listener);
            imageTarget.startAnimation(rotateAnimation);
        }

        void showScaleAnimation(float fromAlpha, float toAlpha, long durationMillis,
                AnimationListener listener) {
            ScaleAnimation scaleAnimation = new ScaleAnimation(fromAlpha, toAlpha, fromAlpha,
                    toAlpha,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            scaleAnimation.setDuration(durationMillis);
            scaleAnimation.setAnimationListener(listener);
            imageTarget.startAnimation(scaleAnimation);
        }

        void showAlphaAnimation(float fromAlpha, float toAlpha, long durationMillis,
                AnimationListener listener) {
            AlphaAnimation alphaAnim = new AlphaAnimation(fromAlpha, toAlpha);
            alphaAnim.setDuration(durationMillis);
            alphaAnim.setAnimationListener(listener);
            if (fromAlpha == 0.0f) {
                imageTarget.setVisibility(View.VISIBLE);
            }
            imageTarget.startAnimation(alphaAnim);
            if (toAlpha == 0.0f) {
                imageTarget.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void animatedDragAlpha() {
        fogAlpha = getCorrectAlpha(fogAnimationValue * (1.0f - distancePerMaxAlpha));
        objAlpha = getCorrectAlpha(3.0f * distancePerMaxAlpha);
        vignettingAlpha = getCorrectAlpha(1.3f * distancePerMaxAlpha);
        setAlphaAndVisibility(mFlareLight, fogAlpha);
        setAlphaAndVisibility(mFlareVignetting, vignettingAlpha);
        for (int i = 0; i < HEXAGON_TOTAL; i++) {
            setAlphaAndVisibility(hexagon[i], objAlpha);
        }
    }

    private void animatedDragPos() {
        float f1 = 1.0f + 1.0f * distancePerMaxAlpha;
        mFlareLight.setScaleX(f1);
        mFlareLight.setScaleY(f1);
        float f2 = 30.0f * -objAnimationValue - 160.0f * distancePerMaxAlpha;
        mFlareLight.setRotation(f2);
        setCenterPos(mFlareLight, showStartX, showStartY, currentX, currentY, 1.0f);
        for (int i = 0; i < HEXAGON_TOTAL; i++) {
            setCenterPos(hexagon[i], showStartX, showStartY, currentX, currentY,
                    hexagonDistance[i], hexagonScale[i], hexagonRotation[i]);
        }
    }

    private void animatedFadeOut() {
        setAlphaAndVisibility(mFlareLight, fogAlpha * fadeoutAnimationValue);
        setAlphaAndVisibility(mFlareVignetting, vignettingAlpha * fadeoutAnimationValue);
        for (int i = 0; i < HEXAGON_TOTAL; i++) {
            setAlphaAndVisibility(hexagon[i], objAlpha * fadeoutAnimationValue);
        }
    }

    private void animatedHoverLight() {
        hoverLight.setScaleX(hoverLightAnimationValue * 3);
        hoverLight.setScaleY(hoverLightAnimationValue * 3);
    }

    private void animatedTap() {
        float f1 = 1.0f;
        float f2 = 0.2f + 0.8f * tapAnimationValue;
        int i = 0;
        float f8;
        float f9;
        float f13;

        if (tapAnimationValue > 0.5f) {
            f1 = 1.0f - 2.0f * (tapAnimationValue - 0.5f);
        }

        while (true) {
            setAlphaAndVisibility(tapHexagon[i], f1);
            float f3 = tapHexagonScale[i] * (0.7f + 0.8f * tapAnimationValue);
            tapHexagon[i].setScaleX(f3);
            tapHexagon[i].setScaleY(f3);
            float f4 = showStartX + f2 * tapHexRandomPoint[i].x;
            float f5 = showStartY + f2 * tapHexRandomPoint[i].y;
            float f6 = f4 - tapHexagon[i].getWidth() / 2.0f;
            float f7 = f5 - tapHexagon[i].getHeight() / 2.0f;
            tapHexagon[i].setX(f6);
            tapHexagon[i].setY(f7);
            i++;

            if (i >= HEXAGON_TOTAL) {
                f8 = 1.8f * tapAnimationValue;
                if (f8 >= 0.5f) {
                    f9 = 1.0f - 2.0f * (f8 - 0.5f);
                } else {
                    f9 = 1.0f;
                }
                float f10 = getCorrectAlpha(f9);
                float f11 = 1.2f * tapAnimationValue;
                setAlphaAndVisibility(mFlareParticle, f10);
                mFlareParticle.setScaleX(f11);
                mFlareParticle.setScaleY(f11);
                float f12 = 1.4f * tapAnimationValue;
                if (f12 < 0.5f) {
                    f13 = 1.0f;

                } else {
                    f13 = 1.0f - 2.0f * (f12 - 0.5f);
                }

                float f14 = getCorrectAlpha(f13);
                setAlphaAndVisibility(mFlareRing, f14);
                float f15 = 0.5f + tapAnimationValue;
                mFlareRing.setScaleX(f15);
                mFlareRing.setScaleY(f15);
                setAlphaAndVisibility(mflareLong, f14);
                float f16 = 1.5f + 2.0f * tapAnimationValue;
                mflareLong.setScaleX(f16);
                mflareLong.setScaleY(f16);
                mflareLong.setRotation(randomRotation + 30.0f * tapAnimationValue);
                return;
            }
        }
    }

    private void animatedUnlock() {
        float f1 = 1.0f + 2.0f * unlockAnimationValue;
        float f2;
        if (unlockAnimationValue < 0.5f) {
            f2 = 2.0f * unlockAnimationValue;
        } else {
            f2 = 1.0f - 2.0f * (unlockAnimationValue - 0.5f);
        }

        setAlphaAndVisibility(mFlareRainbow, f2);
        setCenterPos(mFlareRainbow, showStartX, showStartY, currentX, currentY, 1.0f);
        mFlareRainbow.setScaleX(f1);
        mFlareRainbow.setScaleY(f1);
    }

    private void calculateDistance(float x, float y) {
        float f1 = x - showStartX;
        float f2 = y - showStartY;
        distance = Math.sqrt(Math.pow(f1, 2.0D) + Math.pow(f2, 2.0D));
        distancePerMaxAlpha = ((float) distance / MAX_ALPHA_DISTANCE);
    }

    private void cancelAnimator(Animator animator) {
        if ((animator != null) && (animator.isRunning())) {
            animator.cancel();
        }
    }

    private float getCorrectAlpha(float alpha) {
        if (alpha <= 0.0f) {
            alpha = 0.0f;
        }
        if (alpha >= 1.0f) {
            alpha = 1.0f;
        }

        return alpha;
    }

    private void hoverEnter(float x, float y) {
        hoverX = x + X_OFFSET;
        hoverY = y + Y_OFFSET;
        setAlphaAndVisibility(hoverLight, 1.0f);
        cancelAnimator(hoverLightInAnimator);
        cancelAnimator(hoverLightOutAnimator);
        hoverLightInAnimator.start();
    }

    private void hoverExit() {
        cancelAnimator(hoverLightInAnimator);
        cancelAnimator(hoverLightOutAnimator);
        hoverLightOutAnimator.start();
    }

    private void hoverMove(float x, float y) {
        hoverX = x + X_OFFSET;
        hoverY = y + Y_OFFSET;
        setCenterPos(hoverLight, showStartX, showStartY, hoverX, hoverY, 1.0f);
    }

    private void setAlphaAndVisibility(View view, float alpha) {
        float mAlpha = getCorrectAlpha(alpha);

        if (mAlpha == 0.0f) {
            view.setVisibility(View.INVISIBLE);
        } else {
            view.setVisibility(View.VISIBLE);
            view.setAlpha(mAlpha);
        }
    }

    private void setAnimator() {
        objAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        objAnimator.setInterpolator(new QuintEaseOut());
        objAnimator.setDuration(6000);
        objAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                objAnimationValue = ((Float) animation.getAnimatedValue()).floatValue();
                animatedDragPos();
            }
        });

        fogOnAnimator = ValueAnimator.ofFloat(0.0f, 0.6f);
        fogOnAnimator.setInterpolator(new QuintEaseOut());
        fogOnAnimator.setDuration(100);
        fogOnAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                fogAnimationValue = ((Float) animation.getAnimatedValue()).floatValue();
                animatedDragAlpha();
            }
        });

        tapAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        tapAnimator.setInterpolator(new QuintEaseOut());
        tapAnimator.setDuration(4000);
        tapAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                tapAnimationValue = ((Float) animation.getAnimatedValue()).floatValue();
                animatedTap();
            }
        });

        fadeOutAnimator = ValueAnimator.ofFloat(1.0f, 0.0f);
        fadeOutAnimator.setInterpolator(new LinearInterpolator());
        fadeOutAnimator.setDuration(500);
        fadeOutAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                fadeoutAnimationValue = ((Float) animation.getAnimatedValue()).floatValue();
                animatedFadeOut();
            }
        });

        unlockAnimator = ValueAnimator.ofFloat(0.0f, 0.5f);
        unlockAnimator.setInterpolator(new QuintEaseOut());
        unlockAnimator.setDuration(100);
        unlockAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                unlockAnimationValue = ((Float) animation.getAnimatedValue()).floatValue();
                if (mIsFallRs) {
                } else {
                    animatedUnlock();
                }
                if (unlockAnimationValue == 0.5f) {
                    sendMessageToHandler(MSG_UNLOCK_DELAYED);
                }
            }
        });

        hoverLightInAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        hoverLightInAnimator.setInterpolator(new BackInterpolator(EasingType.OUT, 8.0F));
        hoverLightInAnimator.setDuration(500);
        hoverLightInAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                hoverLightAnimationValue = ((Float) animation.getAnimatedValue()).floatValue();
                animatedHoverLight();
            }
        });

        hoverLightOutAnimator = ValueAnimator.ofFloat(1.0f, 0.0f);
        hoverLightOutAnimator.setInterpolator(new QuintEaseOut());
        hoverLightOutAnimator.setDuration(500);
        hoverLightOutAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                hoverLightAnimationValue = ((Float) animation.getAnimatedValue()).floatValue();
                animatedHoverLight();
                setAlphaAndVisibility(hoverLight, hoverLightAnimationValue);
            }
        });
    }

    private void setCenterPos(View view, float mStartX, float mStartY, float mCurrentX,
            float mCurrentY, float mScaleValue) {
        float f1 = mStartX + mScaleValue * (mCurrentX - mStartX);
        float f2 = mStartY + mScaleValue * (mCurrentY - mStartY);
        float f3 = f1 - view.getWidth() / 2.0f;
        float f4 = f2 - view.getHeight() / 2.0f;
        if (view.getWidth() != 0) {
            view.setX(f3);
            view.setY(f4);
        }
    }

    private void setCenterPos(View view, float mStartX, float mStartY, float mCurrentX,
            float mCurrentY, float mDistanceValue, float mScaleValue, int mRotateAngle) {
        float f1 = 0.5f + 0.5f * ((float) distance / 720.0f);
        float f2 = (0.5f + 0.5f * objAnimationValue) * (mScaleValue * f1);
        view.setScaleX(f2);
        view.setScaleY(f2);

        float f3 = 0.5f + 0.5f * objAnimationValue;
        float f4 = mStartX + f3 * (mDistanceValue * (mCurrentX - mStartX));
        float f5 = mStartY + f3 * (mDistanceValue * (mCurrentY - mStartY));
        if (mRotateAngle != 0) {
            float f8 = mScaleValue * 300.0f;
            float f9 = mScaleValue * (mScaleValue * ((float) distance / 1000.0f));
            float f10 = 1.0f * objAnimationValue;
            double d = 3.141592653589793D * mRotateAngle / 180.0D + f9 + f10;
            f4 = mCurrentX + (float) (f8 * Math.cos(d) + f8 * Math.sin(d));
            f5 = mCurrentY + (float) (f8 * -Math.sin(d) + f8 * Math.cos(d));
        }
        float f6 = f4 - view.getWidth() / 2.0f;
        float f7 = f5 - view.getHeight() / 2.0f;
        view.setX(f6);
        view.setY(f7);
    }

    private void setHexagon() {
        int mImageId = 0;

        hexagon = new ImageViewBlended[HEXAGON_TOTAL];
        hexagonRotation = new int[HEXAGON_TOTAL];
        hexagonDistance = new float[HEXAGON_TOTAL];
        hexagonScale = new float[HEXAGON_TOTAL];

        for (int i = 0; i < HEXAGON_TOTAL; i++) {
            int j = i % 3;
            switch (j)
            {
                case 0:
                    mImageId = R.drawable.keyguard_flare_hexagon_blue;
                    break;
                case 1:
                    mImageId = R.drawable.keyguard_flare_hexagon_green;
                    break;
                case 2:
                    mImageId = R.drawable.keyguard_flare_hexagon_orange;
                    break;
            }

            ImageViewBlended localImageViewBlended = new ImageViewBlended(mContext);
            localImageViewBlended.setImageResource(mImageId);
            float f = (float) (20.0D * Math.random());
            setAlphaAndVisibility(localImageViewBlended, 0.0f);
            localImageViewBlended.setRotation(f);
            lightObj.addView(localImageViewBlended, -2, -2);
            hexagon[i] = localImageViewBlended;
        }
    }

    private void setHexagonRandomTarget() {
        tapHexRandomPoint = new Point[HEXAGON_TOTAL];
        tapHexagonScale = new float[HEXAGON_TOTAL];
        randomRotation = (int) (360.0D * Math.random());

        for (int i = 0; i < HEXAGON_TOTAL; i++) {
            int j = (int) (600.0D * Math.random());
            int k = (int) (FloatMath.cos(randomRotation) * j);
            int m = (int) (FloatMath.sin(randomRotation) * j);
            tapHexRandomPoint[i] = new Point(k, m);
            tapHexagonScale[i] = (0.3F + (float) (0.800000011920929D * Math.random()));
            float f1 = 0.5f + 0.5f * (float) Math.random();
            setAlphaAndVisibility(tapHexagon[i], f1);

            float f2 = 0.4f * ((float) Math.random() - 0.5f);
            hexagonDistance[i] = (float) (f2 + (0.2f + 0.24f * i));
        }

        for (int i = 0; i < HEXAGON_TOTAL; i++) {
            hexagonRotation[i] = 0;
            hexagonScale[i] = (0.2f + hexagonDistance[i]);
            hexagon[i].setScaleX(hexagonScale[i]);
            hexagon[i].setScaleY(hexagonScale[i]);
        }
        mFlareParticle.setRotation(randomRotation);
    }

    private void setHover() {
        hoverLight = new ImageViewBlended(mContext);
        hoverLight.setImageResource(R.drawable.keyguard_flare_hoverlight);
        hoverLight.setScaleX(0.0f);
        hoverLight.setScaleY(0.0f);
        mFlareFrameLayout.addView(hoverLight, -2, -2);
    }

    private void setLayout() {
        mFlareVignetting = new ImageView(mContext);
        BitmapFactory.Options localOptions = new BitmapFactory.Options();
        localOptions.inPreferredConfig = Bitmap.Config.ALPHA_8;
        mFlareVignetting.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.keyguard_flare_vignetting, localOptions));
        mFlareVignetting.setScaleType(ImageView.ScaleType.FIT_XY);
        mFlareVignetting.setAlpha(0.0f);
        mFlareFrameLayout.addView(mFlareVignetting);

        mFlareLight = new ImageViewBlended(mContext);
        mFlareLight.setImageResource(R.drawable.keyguard_flare_light_00040);
        mFlareFrameLayout.addView(mFlareLight, -2, -2);
        setAlphaAndVisibility(mFlareLight, 0.0f);

        lightObj = new FrameLayout(mContext);
        mFlareFrameLayout.addView(lightObj);

        lightTap = new FrameLayout(mContext);
        mFlareFrameLayout.addView(lightTap);

        mFlareRing = new ImageViewBlended(mContext);
        mFlareRing.setImageResource(R.drawable.keyguard_flare_ring);
        mFlareFrameLayout.addView(mFlareRing, -2, -2);
        setAlphaAndVisibility(mFlareRing, 0.0f);

        mflareLong = new ImageViewBlended(mContext);
        mflareLong.setImageResource(R.drawable.keyguard_flare_long);
        mFlareFrameLayout.addView(mflareLong, -2, -2);
        setAlphaAndVisibility(mflareLong, 0.0f);

        mFlareParticle = new ImageViewBlended(mContext);
        mFlareParticle.setImageResource(R.drawable.keyguard_flare_particle);
        mFlareFrameLayout.addView(mFlareParticle, -2, -2);
        setAlphaAndVisibility(mFlareParticle, 0.0f);

        mFlareRainbow = new ImageViewBlended(mContext);
        mFlareRainbow.setImageResource(R.drawable.keyguard_flare_rainbow);
        mFlareFrameLayout.addView(mFlareRainbow, -2, -2);
        setAlphaAndVisibility(mFlareRainbow, 0.0f);
    }

    private void setTapHexagon() {
        int mImageId = 0;

        tapHexagon = new ImageViewBlended[HEXAGON_TOTAL];

        for (int i = 0; i < HEXAGON_TOTAL; i++) {
            int j = i % 3;
            switch (j)
            {
                case 0:
                    mImageId = R.drawable.keyguard_flare_hexagon_blue;
                    break;
                case 1:
                    mImageId = R.drawable.keyguard_flare_hexagon_green;
                    break;
                case 2:
                    mImageId = R.drawable.keyguard_flare_hexagon_orange;
                    break;
            }
            ImageViewBlended localImageViewBlended = new ImageViewBlended(mContext);
            localImageViewBlended.setImageResource(mImageId);
            setAlphaAndVisibility(localImageViewBlended, 0.0f);
            localImageViewBlended.setRotation((int) (360.0D * Math.random()));
            lightTap.addView(localImageViewBlended, -2, -2);
            tapHexagon[i] = localImageViewBlended;
        }
    }

    private void flareInit() {
        setLayout();
        setHover();
        setHexagon();
        setTapHexagon();
        setAnimator();
    }

    public void flareHide() {
        isTouched = false;
        cancelAnimator(fogOnAnimator);
        fadeOutAnimator.start();
    }

    public void flareMove(float x, float y) {
        if (!isTouched) {
            flareShow(x, y);
        }
        currentX = x + X_OFFSET;
        currentY = y + Y_OFFSET;
        calculateDistance(currentX, currentY);
        if (!fogOnAnimator.isRunning()) {
            animatedDragAlpha();
        }
        animatedDragPos();
    }

    public void flareShow(float x, float y) {
        isTouched = true;
        distance = 0.0D;
        distancePerMaxAlpha = 0.0f;
        showStartX = x + X_OFFSET;
        showStartY = y + Y_OFFSET;
        currentX = showStartX;
        currentY = showStartY;
        setHexagonRandomTarget();
        animatedDragPos();
        setCenterPos(hoverLight, showStartX, showStartY, showStartX, showStartY, 1.0f);
        setCenterPos(mFlareLight, showStartX, showStartY, showStartX, showStartY, 1.0f);
        setCenterPos(mFlareRing, showStartX, showStartY, showStartX, showStartY, 1.0f);
        setCenterPos(mflareLong, showStartX, showStartY, showStartX, showStartY, 1.0f);
        setCenterPos(mFlareParticle, showStartX, showStartY, showStartX, showStartY, 1.0f);
        cancelAnimator(fadeOutAnimator);
        cancelAnimator(objAnimator);
        cancelAnimator(fogOnAnimator);
        cancelAnimator(tapAnimator);
        objAnimator.start();
        fogOnAnimator.start();
        tapAnimator.start();
        playSoundsTouchDown();
    }

    public void flareUnlock() {
        if (!unlockAnimator.isRunning()) {
            float f = (float) (180.0D * Math.atan2(currentY - showStartY, currentX - showStartX) / 3.141592653589793D) - 40.0f;
            mFlareRainbow.setRotation(f);
            unlockAnimator.start();
        }
    }

    public class BackInterpolator implements Interpolator {
        private float overshot;
        private EasingType type;

        public BackInterpolator(EasingType paramFloat, float arg3) {
            type = paramFloat;
            overshot = arg3;
        }

        private float in(float paramFloat1, float paramFloat2) {
            if (paramFloat2 == 0.0f) {
                paramFloat2 = 1.70158f;
            }
            return paramFloat1 * paramFloat1 * (paramFloat1 * (1.0f + paramFloat2) - paramFloat2);
        }

        private float inout(float paramFloat1, float paramFloat2)
        {
            float f1 = paramFloat1 * 2.0f;
            float f5;
            if (paramFloat2 == 0.0f) {
                paramFloat2 = 1.70158f;
            }

            if (f1 < 1.0f) {
                float f6 = f1 * f1;
                float f7 = (float) (1.525D * paramFloat2);
                f5 = 0.5f * (f6 * (f1 * (f7 + 1.0f) - f7));
            } else {
                float f2 = f1 - 2.0f;
                float f3 = f2 * f2;
                float f4 = (float) (1.525D * paramFloat2);
                f5 = 0.5F * (2.0f + f3 * (f4 + f2 * (f4 + 1.0f)));
            }
            return f5;
        }

        private float out(float paramFloat1, float paramFloat2) {
            float f = paramFloat1 - 1.0f;
            if (paramFloat2 == 0.0f) {
                paramFloat2 = 1.70158f;
            }
            return 1.0f + f * f * (paramFloat2 + f * (paramFloat2 + 1.0f));
        }

        public float getInterpolation(float paramFloat) {
            float f = 0.0f;

            if (type == EasingType.IN) {
                f = in(paramFloat, overshot);
            } else if (type == EasingType.OUT) {
                f = out(paramFloat, overshot);
            }
            if (type == EasingType.INOUT) {
                f = inout(paramFloat, overshot);
            }

            return f;
        }
    }

    public class QuintEaseIn implements Interpolator {
        public QuintEaseIn() {
        }

        public float getInterpolation(float paramFloat) {
            return paramFloat * (paramFloat * (paramFloat * (paramFloat * paramFloat)));
        }
    }

    public class QuintEaseOut implements Interpolator {
        public QuintEaseOut() {
        }

        public float getInterpolation(float paramFloat) {
            float f = paramFloat / 1.0f - 1.0f;
            return 1.0f + f * (f * (f * (f * f)));
        }
    }

    public class ImageViewBlended extends ImageView
    {
        private Bitmap mBitmap;
        private Paint mPaint = new Paint();
        private Rect mRect;
        private PorterDuff.Mode mMode = PorterDuff.Mode.ADD;

        public ImageViewBlended(Context context)
        {
            super(context);
        }

        public ImageViewBlended(Context context, PorterDuff.Mode mode)
        {
            super(context);
            mMode = mode;
        }

        public ImageViewBlended(Context context, AttributeSet attributeSet)
        {
            super(context, attributeSet);
        }

        public ImageViewBlended(Context context, AttributeSet attributeSet, int param)
        {
            super(context, attributeSet, param);
        }

        protected void onDraw(Canvas canvas)
        {
            mPaint.setFlags(1);
            Paint localPaint = mPaint;
            canvas.drawBitmap(mBitmap, null, mRect, localPaint);
        }

        public void setImageResource(int mResId)
        {
            super.setImageResource(mResId);
            mBitmap = ((BitmapDrawable) getDrawable()).getBitmap();
            if (mMode != null) {
                mPaint.setXfermode(new PorterDuffXfermode(mMode));
            }
            mRect = new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
        }
    }

    public static enum EasingType {
        IN,
        OUT,
        INOUT
    }

    private int getLockEffect() {
        SharedPreferences sp = mContext.getSharedPreferences("samsung_lock", Context.MODE_PRIVATE);
        return sp.getInt("lock_effect", 1);
    }

    public void switchLockEffect(int effect) {
        mIsFallRs = effect == 0 ? true : false;
        if (mIsFallRs) {
            // mWaterlayout.setVisibility(View.VISIBLE);
        } else {
            // mWaterlayout.setVisibility(View.GONE);
        }
    }

    private String getLogoText() {
        SharedPreferences sp = mContext.getSharedPreferences("samsung_lock", Context.MODE_PRIVATE);
        return sp.getString("logo_text", getResources().getString(R.string.logo_text));
    }

    private float getLogoTextSize() {
        SharedPreferences sp = mContext.getSharedPreferences("samsung_lock", Context.MODE_PRIVATE);
        return sp.getFloat("logo_text_size",
                getResources().getDimensionPixelSize(R.dimen.keyguard_lockscreen_logo_textsize));
    }

    private int getLogoTextColor() {
        SharedPreferences sp = mContext.getSharedPreferences("samsung_lock", Context.MODE_PRIVATE);
        return sp.getInt("logo_text_color", Color.WHITE);
    }

    private int getLogoTextBgColor() {
        SharedPreferences sp = mContext.getSharedPreferences("samsung_lock", Context.MODE_PRIVATE);
        return sp.getInt("logo_text_bgcolor", Color.TRANSPARENT);
    }

    public void onResume() {
        if (mLogoText != null) {
            mLogoText.setText(getLogoText());
            mLogoText.setTextSize(TypedValue.COMPLEX_UNIT_SP, getLogoTextSize());
            mLogoText.setTextColor(getLogoTextColor());
            //mLogoText.setBackgroundColor(getLogoTextBgColor());
        }
    }
}
