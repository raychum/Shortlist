package com.openrice.android.shortlist;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.view.GravityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;

public class ShortlistService extends Service {
    private static final String TAG = ShortlistService.class.getSimpleName();
    private static final int TRANSLATION_DURATION = 100;
    private WindowManager windowManager;
    private View floatingView;
    private ShortlistLayout shortlistLayout;
    private WindowManager.LayoutParams floatingViewParams;
    private WindowManager.LayoutParams shortlistParams;
    private boolean isExpanded = false;
    private ValueAnimator translationValueAnimator;
    private IBinder mBinder = new ShortlistBinder();

    public View getFloatingView() {
        return floatingView;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    public ViewGroup getShortlistLayout() {
        return shortlistLayout;
    }

    public WindowManager.LayoutParams getFloatingViewParams() {
        return floatingViewParams;
    }

    public void addViewToMenu(ShortlistLayout.MenuPosition position, View view, int menuIndex) {
        if (shortlistLayout != null) {
            shortlistLayout.addViewToMenu(position, view, menuIndex);
        }
    }

    public View initFloatingView(int layoutId) {
        if (floatingView != null) {
            windowManager.removeView(floatingView);
        }
        final LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        floatingView = layoutInflater.inflate(layoutId, null);
        final Point size = AnimatorHelper.getWindowSize(this);
        final int[] centerPoint = new int[]{size.x / 2, size.y / 2};
        floatingViewParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PRIORITY_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        floatingViewParams.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
        floatingViewParams.gravity = Gravity.TOP | GravityCompat.START;
        floatingViewParams.x = (int) (centerPoint[0] - getResources().getDimension(R.dimen.shortlist_floating_view_width) / 2);
        floatingViewParams.y = (int) (centerPoint[1] - getResources().getDimension(R.dimen.shortlist_floating_view_height) / 2);

        floatingView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        if (isExpanded && event.getAction() == KeyEvent.ACTION_UP) {
                            show(isExpanded);
                        }
                        return true;
                    default:
                        return false;
                }
            }
        });
        floatingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                show(isExpanded);
            }
        });
        floatingView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private long tsDown;
            private long tsUp;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!isExpanded) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            tsDown = System.currentTimeMillis();
                            initialX = floatingViewParams.x;
                            initialY = floatingViewParams.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_UP:
                            tsUp = System.currentTimeMillis();
                            if ((tsUp - tsDown) < ViewConfiguration.getTapTimeout()) {
                                return false;
                            }
                            if (initialX != floatingViewParams.x || initialY != floatingViewParams.y) {
                                return true;
                            }
                            break;
                        case MotionEvent.ACTION_MOVE:
                            floatingViewParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                            floatingViewParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                            windowManager.updateViewLayout(floatingView, floatingViewParams);
                            break;
                    }
                }
                return false;
            }
        });
        windowManager.addView(floatingView, floatingViewParams);
        return floatingView;
    }

    public ShortlistLayout initShortlistLayout(int layoutId) {
        final LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        shortlistLayout = (ShortlistLayout) layoutInflater.inflate(layoutId, null);
        shortlistLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExpanded) {
                    show(isExpanded);
                }
            }
        });
        shortlistParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PRIORITY_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        windowManager.addView(shortlistLayout, shortlistParams);
        return shortlistLayout;
    }

    public void initMoveAnimation(final View view2animate, int startX, int startY, int endX, int endY) {
        final ValueAnimator.AnimatorUpdateListener animatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {

            final int[] position = new int[2];

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                position[0] = (Integer) valueAnimator.getAnimatedValue("x");
                position[1] = (Integer) valueAnimator.getAnimatedValue("y");
                updateViewLayout(view2animate, position[0], position[1], null, null);
            }
        };
        final Animator.AnimatorListener animatorListener = new AnimatorListenerAdapter() {
            private boolean isReversing = false;

            @Override
            public void onAnimationStart(Animator animation) {
                if (!isReversing) {
                    floatingViewParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
                    windowManager.updateViewLayout(floatingView, floatingViewParams);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isReversing) {
                    shortlistParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
                    shortlistParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                    shortlistParams.height = WindowManager.LayoutParams.MATCH_PARENT;
                    windowManager.updateViewLayout(shortlistLayout, shortlistParams);
                    shortlistLayout.setVisibility(View.VISIBLE);
                    shortlistLayout.expandMenus(0, 0);
                }
                isReversing = !isReversing;
            }
        };
        translationValueAnimator = AnimatorHelper.getTranslationValueAnimator(startX, startY, endX, endY, TRANSLATION_DURATION,
                new AccelerateInterpolator(), animatorUpdateListener, animatorListener);
    }

    private void updateViewLayout(View view, Integer x, Integer y, Integer w, Integer h) {
        if (view != null) {
            WindowManager.LayoutParams lp = (WindowManager.LayoutParams) view.getLayoutParams();

            if (x != null) lp.x = x;
            if (y != null) lp.y = y;
            if (w != null && w > 0) lp.width = w;
            if (h != null && h > 0) lp.height = h;

            windowManager.updateViewLayout(view, lp);
        }
    }

    private void show(boolean isExpanded) {
        if (!shortlistLayout.isAnimating()) {
            if (isExpanded) {
                floatingViewParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                windowManager.updateViewLayout(floatingView, floatingViewParams);
                shortlistLayout.collapseAllMenus(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        shortlistParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                        shortlistParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                        shortlistParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                        windowManager.updateViewLayout(shortlistLayout, shortlistParams);
                        shortlistLayout.setVisibility(View.GONE);
                        translationValueAnimator.reverse();
                    }
                });
            } else {
                final Point size = AnimatorHelper.getWindowSize(this);
                initMoveAnimation(floatingView, floatingViewParams.x, floatingViewParams.y, size.x / 2 - floatingView.getWidth() / 2, size.y / 2 - floatingView.getHeight() / 2);
                translationValueAnimator.start();
            }
            this.isExpanded = !isExpanded;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (shortlistLayout != null) windowManager.removeView(shortlistLayout);
        if (floatingView != null) windowManager.removeView(floatingView);
    }

    public class ShortlistBinder extends Binder {
        public ShortlistService getService() {
            return ShortlistService.this;
        }
    }
}
