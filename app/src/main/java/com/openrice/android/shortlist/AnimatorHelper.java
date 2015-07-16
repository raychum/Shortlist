package com.openrice.android.shortlist;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Interpolator;

import java.util.ArrayList;

/**
 * Created by Ray on 9/7/15.
 */
public class AnimatorHelper {

    private AnimatorHelper() {
    }

    public static void startAnimators(ArrayList<Animator> animators, Interpolator interpolator, Animator.AnimatorListener listener) {
        final AnimatorSet translationAnimatorSet = new AnimatorSet();
        if (interpolator != null) {
            translationAnimatorSet.setInterpolator(interpolator);
        }
        translationAnimatorSet.playTogether(animators);
        if (listener != null) {
            translationAnimatorSet.addListener(listener);
        }
        translationAnimatorSet.start();
    }

    public static void startAnimators(ArrayList<Animator> animators, int duration, Interpolator interpolator, Animator.AnimatorListener... listeners) {
        final AnimatorSet translationAnimatorSet = AnimatorHelper.getAnimatorSet(duration, interpolator);
        translationAnimatorSet.playTogether(animators);
        if (listeners != null && listeners.length > 0) {
            for (Animator.AnimatorListener listener : listeners) {
                if (listener != null) {
                    translationAnimatorSet.addListener(listener);
                }
            }
        }
        translationAnimatorSet.start();
    }

    @SafeVarargs
    public static ArrayList<Animator> getCollapseAnimators(int[] center, ArrayList<View>... items) {
        final ArrayList<Animator> animators = new ArrayList<>();
        if (items != null && items.length > 0) {
            for (ArrayList<View> item : items) {
                if (item != null) {
                    for (View view : item) {
                        final ObjectAnimator translateX = ObjectAnimator.ofFloat(view, "X", view.getX(), center[0] - view.getWidth() / 2);
                        final ObjectAnimator translateY = ObjectAnimator.ofFloat(view, "Y", view.getY(), center[1] - view.getHeight() / 2);
                        animators.add(translateX);
                        animators.add(translateY);
                    }
                }
            }
        }
        return animators;
    }

    public static ValueAnimator getTranslationValueAnimator(int startX, int startY, int endX, int endY, int duration,
                                                            Interpolator interpolator, final ValueAnimator.AnimatorUpdateListener animatorUpdateListener,
                                                            final Animator.AnimatorListener animatorListener) {
        PropertyValuesHolder pvhX = PropertyValuesHolder.ofInt("x", startX, endX);
        PropertyValuesHolder pvyY = PropertyValuesHolder.ofInt("y", startY, endY);
        final ValueAnimator animator = ObjectAnimator.ofPropertyValuesHolder(pvhX, pvyY).setDuration(duration);
        if (interpolator != null) {
            animator.setInterpolator(interpolator);
        }
        if (animatorListener != null){
            animator.addListener(animatorListener);
        }
        if (animatorUpdateListener != null){
            animator.addUpdateListener(animatorUpdateListener);
        }
        return animator;
    }

    @NonNull
    @SafeVarargs
    public static ArrayList<Animator> getRotateAnimators(long duration, int repeatCount, ArrayList<View>... items) {
        final ArrayList<Animator> animators = new ArrayList<>();
        if (items != null && items.length > 0) {
            for (ArrayList<View> item : items) {
                if (item != null) {
                    for (View view : item) {
                        animators.add(getRotateAnimator(view, duration, repeatCount));
                    }
                }
            }
        }
        return animators;
    }

    @NonNull
    public static ValueAnimator getResizeAndTranslationAnimator(View view, long duration, float scale, float startX, float startY, float endX, float endY,
                                                                Interpolator interpolator, final ValueAnimator.AnimatorUpdateListener animatorUpdateListener,
                                                                final Animator.AnimatorListener animatorListener) {
        float currentScale = view.getScaleX();
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", currentScale, scale);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", currentScale, scale);
        PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("x", startX, endX);
        PropertyValuesHolder pvyY = PropertyValuesHolder.ofFloat("y", startY, endY);
        final ValueAnimator animator = ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY, pvhX, pvyY).setDuration(duration);
        if (interpolator != null) {
            animator.setInterpolator(interpolator);
        }
        if (animatorListener != null){
            animator.addListener(animatorListener);
        }
        if (animatorUpdateListener != null){
            animator.addUpdateListener(animatorUpdateListener);
        }
        return animator;
    }

    @NonNull
    public static ObjectAnimator getRotateAnimator(View view, long duration, int repeatCount) {
        final ObjectAnimator rotate = ObjectAnimator.ofFloat(view, "rotation", 0, 360);
        rotate.setDuration(duration);
        rotate.setRepeatCount(repeatCount);
        rotate.setRepeatMode(Animation.INFINITE);
        return rotate;
    }

    public static ArrayList<Animator> getExpandTopAnimators(int[] center, int expandedWidth, int expandedHeight, ArrayList<View> items) {
        final ArrayList<Animator> animators = new ArrayList<>();
        if (items != null) {
            final float y = center[1] - expandedHeight / 2;

            for (int i = 0; i < items.size(); i++) {
                final float x = getDestinationX(center[0], expandedWidth, items.get(i).getWidth(), items.size(), i);
                final ObjectAnimator translateX = ObjectAnimator.ofFloat(items.get(i), "X", items.get(i).getX(), x);
                final ObjectAnimator translateY = ObjectAnimator.ofFloat(items.get(i), "Y", items.get(i).getY(), y - items.get(i).getHeight() / 2);
                animators.add(translateX);
                animators.add(translateY);
            }
        }
        return animators;
    }

    public static ArrayList<Animator> getExpandBottomAnimators(int[] center, int expandedWidth, int expandedHeight, ArrayList<View> items) {
        final ArrayList<Animator> animators = new ArrayList<>();
        if (items != null) {
            final float y = center[1] + expandedHeight / 2;
            for (int i = 0; i < items.size(); i++) {
                final float x = getDestinationX(center[0], expandedWidth, items.get(i).getWidth(), items.size(), i);
                final ObjectAnimator translateX = ObjectAnimator.ofFloat(items.get(i), "X", items.get(i).getX(), x);
                final ObjectAnimator translateY = ObjectAnimator.ofFloat(items.get(i), "Y", items.get(i).getY(), y - items.get(i).getHeight() / 2);
                animators.add(translateX);
                animators.add(translateY);
            }
        }
        return animators;
    }

    @NonNull
    public static AnimatorSet getAnimatorSet(long duration, Interpolator interpolator) {
        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(duration);
        if (interpolator != null) {
            animatorSet.setInterpolator(interpolator);
        }
        return animatorSet;
    }

    public static float getDestinationX(float centerX, int expandedWidth, int itemWidth, int itemSize, int item) {
        float x = (centerX - expandedWidth / 2) + (expandedWidth / itemSize * item + (expandedWidth / itemSize * (item + 1) - expandedWidth / itemSize * item) / 2) - itemWidth / 2;
        return x;
    }

    @SafeVarargs
    public static ArrayList<Animator> getAnimators(ArrayList<Animator>... animator) {
        final ArrayList<Animator> result = new ArrayList<>();
        if (animator != null && animator.length > 0) {
            for (ArrayList<Animator> animators : animator) {
                result.addAll(animators);
            }
        }
        return result;
    }

    @NonNull
    public static Point getWindowSize(Context context) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static Bitmap getBitmapFromView(View v) {
        v.clearFocus();
        v.setPressed(false);

        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);

        // Reset the drawing cache background color to fully transparent
        // for the duration of this operation
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);

        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache();
        Bitmap cacheBitmap = v.getDrawingCache();
        if (cacheBitmap == null) {
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

        // Restore the view
        v.destroyDrawingCache();
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);

        return bitmap;
    }

    public static float[] getTargetXY(View view) {
        final float[] target = new float[2];
        target[0] = view.getX();
        target[1] = view.getY();
        return target;
    }

    public static float[] getCenterXY(View view) {
        final float[] center = new float[2];
        center[0] = view.getX() + view.getWidth() / 2;
        center[1] = view.getY() + view.getHeight() / 2;
        return center;
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
