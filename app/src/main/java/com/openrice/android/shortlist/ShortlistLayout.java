package com.openrice.android.shortlist;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.RelativeLayout;

import java.util.ArrayList;

/**
 * Created by raychum on 15/7/15.
 */
public class ShortlistLayout extends RelativeLayout implements View.OnClickListener {

    public static final int DEFAULT_EXPANDED_WIDTH = 300;
    public static final int DEFAULT_EXPANDED_HEIGHT = 300;
    public static final int DEFAULT_ROTATION_DURATION = 10;
    public static final int DEFAULT_TRANSLATION_DURATION = 100;
    public static final int DEFAULT_MAX_MENU_ITEM_SIZE = 4;

    private final SparseArray<ArrayList<View>> topMenus = new SparseArray<>();
    private final SparseArray<ArrayList<View>> bottomMenus = new SparseArray<>();
    private int expandedWidth;
    private int expandedHeight;
    private int rotationDuration;
    private int translationDuration;
    private int maxMenuItemSize;
    private int[] centerPoint = {0, 0};
    private int expandedTopMenuIndex = -1;
    private int expandedBottomMenuIndex = -1;
    private boolean isAnimating = false;
    private OnMenuItemsClickListener onMenuItemsClickListener;
    private Interpolator interpolator;
    private final Animator.AnimatorListener animatorListener = new AnimatorListenerAdapter(){
        @Override
        public void onAnimationEnd(Animator animation) {
            isAnimating = false;
        }
        @Override
        public void onAnimationStart(Animator animation) {
            isAnimating = true;
        }
    };
    public ShortlistLayout(Context context) {
        super(context);
    }

    public ShortlistLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttributes(attrs);
    }

    public ShortlistLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttributes(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ShortlistLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAttributes(attrs);
    }

    private void initAttributes(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ShortlistLayout);
        expandedHeight = a.getDimensionPixelSize(R.styleable.ShortlistLayout_expandedHeight, DEFAULT_EXPANDED_HEIGHT);
        expandedWidth = a.getDimensionPixelSize(R.styleable.ShortlistLayout_expandedWidth, DEFAULT_EXPANDED_WIDTH);
        rotationDuration = a.getInt(R.styleable.ShortlistLayout_rotationDuration, DEFAULT_ROTATION_DURATION);
        translationDuration = a.getInt(R.styleable.ShortlistLayout_translationDuration, DEFAULT_TRANSLATION_DURATION);
        maxMenuItemSize = a.getInt(R.styleable.ShortlistLayout_maxMenuItemSize, DEFAULT_MAX_MENU_ITEM_SIZE);
        a.recycle();
        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        centerPoint = new int[]{size.x / 2, size.y / 2};
        interpolator = new OvershootInterpolator();
    }

    public int[] getCenterPoint() {
        return centerPoint;
    }

    public void setCenterPoint(int[] centerPoint) {
        this.centerPoint = centerPoint;
    }

    public boolean isAnimating() {
        return isAnimating;
    }

    public int getExpandedWidth() {
        return expandedWidth;
    }

    public void setExpandedWidth(int expandedWidth) {
        this.expandedWidth = expandedWidth;
    }

    public int getExpandedHeight() {
        return expandedHeight;
    }

    public void setExpandedHeight(int expandedHeight) {
        this.expandedHeight = expandedHeight;
    }

    public Interpolator getInterpolator() {
        return interpolator;
    }

    public void setInterpolator(Interpolator interpolator) {
        this.interpolator = interpolator;
    }

    public OnMenuItemsClickListener getOnMenuItemsClickListener() {
        return onMenuItemsClickListener;
    }

    public void setOnMenuItemsClickListener(OnMenuItemsClickListener onMenuItemsClickListener) {
        this.onMenuItemsClickListener = onMenuItemsClickListener;
    }

    public int getExpandedTopMenuIndex() {
        return expandedTopMenuIndex;
    }

    public int getExpandedBottomMenuIndex() {
        return expandedBottomMenuIndex;
    }

    public synchronized SparseArray<ArrayList<View>> getMenus(MenuPosition position) {
        switch (position) {
            case Top:
                return topMenus;
            case Bottom:
            default:
                return bottomMenus;
        }
    }

    public synchronized ArrayList<View> getMenu(MenuPosition position, int menuIndex) {
        switch (position) {
            case Top:
                return topMenus.get(menuIndex);
            case Bottom:
            default:
                return bottomMenus.get(menuIndex);
        }
    }

    public synchronized void addMenu(MenuPosition position, ArrayList<View> views) {
        if (views.size() > maxMenuItemSize){
            for (int i = 0; i < views.size() - maxMenuItemSize; i++){
                views.remove(i);
            }
        }
        switch (position) {
            case Top:
                topMenus.put(topMenus.size(), views);
                break;
            case Bottom:
            default:
                bottomMenus.put(bottomMenus.size(), views);
                break;
        }
        addMenuViews(views);
    }

    public synchronized void addViewToMenu(MenuPosition position, View view, int menuIndex) {
        if (getMenu(position, menuIndex) == null){
            for (int i = 0; i <=menuIndex; i++){
                addMenu(ShortlistLayout.MenuPosition.Top, new ArrayList<View>());
            }
        }
        switch (position) {
            case Top:
                if (topMenus.size() > menuIndex) {
                    if (topMenus.get(menuIndex).size() >= maxMenuItemSize){
                        removeViewFromMenu(position, menuIndex, 0);
                    }
                    topMenus.get(menuIndex).add(view);
                }
                break;
            case Bottom:
            default:
                if (bottomMenus.size() > menuIndex) {
                    if (bottomMenus.get(menuIndex).size() > maxMenuItemSize){
                        removeViewFromMenu(position, menuIndex, 0);
                    }
                    bottomMenus.get(menuIndex).add(view);
                }
                break;
        }
        addMenuView(view);
    }

    private synchronized void addMenuViews(ArrayList<View> views) {
        for (View view : views) {
            addMenuView(view);
        }
    }

    private synchronized void addMenuView(View view){
        view.setX(centerPoint[0] - view.getLayoutParams().width / 2);
        view.setY(centerPoint[1] - view.getLayoutParams().height / 2);
        view.setOnClickListener(this);
        addView(view);
    }

    public synchronized void clearMenus(MenuPosition position) {
        switch (position) {
            case Top:
                for (int i = 0; i < topMenus.size(); i++) {
                    for (int j = 0; j < topMenus.get(i).size(); j++){
                        removeViewFromMenu(MenuPosition.Top, i, j);
                    }
                }
                break;
            case Bottom:
            default:
                for (int i = 0; i < bottomMenus.size(); i++) {
                    for (int j = 0; j < bottomMenus.get(i).size(); j++){
                        removeViewFromMenu(MenuPosition.Bottom, i, j);
                    }
                }
                break;
        }
    }

    public synchronized void clearAllMenus() {
        removeAllViews();
        topMenus.clear();
        bottomMenus.clear();
    }

    public synchronized void removeMenu(MenuPosition position, int menuIndex) {
        switch (position) {
            case Top:
                for (int j = 0; j < topMenus.get(menuIndex).size(); j++){
                    removeViewFromMenu(MenuPosition.Top, menuIndex, j);
                }
                break;
            case Bottom:
            default:
                for (int j = 0; j < bottomMenus.get(menuIndex).size(); j++){
                    removeViewFromMenu(MenuPosition.Bottom, menuIndex, j);
                }
                break;
        }
    }

    public synchronized void removeViewFromMenu(MenuPosition position, View view, int menuIndex) {
        removeView(view);
        switch (position) {
            case Top:
                topMenus.get(menuIndex).remove(view);
                break;
            case Bottom:
            default:
                bottomMenus.get(menuIndex).remove(view);
                break;
        }
    }

    public synchronized void removeViewFromMenu(MenuPosition position, int viewIndex, int menuIndex) {
        switch (position) {
            case Top:
                removeView(topMenus.get(menuIndex).get(viewIndex));
                topMenus.get(menuIndex).remove(viewIndex);
                break;
            case Bottom:
            default:
                removeView(bottomMenus.get(menuIndex).get(viewIndex));
                bottomMenus.get(menuIndex).remove(viewIndex);
                break;
        }
    }


    public void expandMenu(MenuPosition position, int menuIndex) {
        expandMenu(position, menuIndex, null);
    }

    public void expandMenu(final MenuPosition position, final int menuIndex, final Animator.AnimatorListener listener) {
        final ArrayList<Animator> rotateAnimators = AnimatorHelper.getRotateAnimators(rotationDuration, translationDuration / rotationDuration, getMenu(position, menuIndex));
        final ArrayList<Animator> translationAnimators;
        switch (position) {
            case Top:
                translationAnimators = AnimatorHelper.getAnimators(AnimatorHelper.getExpandTopAnimators(centerPoint, expandedWidth, expandedHeight, getMenu(position, menuIndex)));
                if (expandedTopMenuIndex >= 0) {
                    collapseMenu(position, expandedTopMenuIndex, new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            expandedTopMenuIndex = -1;
                            expandMenu(position, menuIndex, listener);
                        }
                    });
                    return;
                }
                expandedTopMenuIndex = menuIndex;
                break;
            case Bottom:
            default:
                translationAnimators = AnimatorHelper.getAnimators(AnimatorHelper.getExpandBottomAnimators(centerPoint, expandedWidth, expandedHeight, getMenu(position, menuIndex)));
                if (expandedBottomMenuIndex >= 0) {
                    collapseMenu(position, expandedBottomMenuIndex, new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            expandedBottomMenuIndex = -1;
                            expandMenu(position, menuIndex, listener);
                        }
                    });
                    return;
                }
                expandedBottomMenuIndex = menuIndex;
                break;
        }
        AnimatorHelper.startAnimators(translationAnimators, translationDuration, interpolator, animatorListener, listener);
        AnimatorHelper.startAnimators(rotateAnimators, null, null);
    }

    public void expandMenus(final int topMenuIndex, final int bottomMenuIndex) {
        expandMenus(topMenuIndex, bottomMenuIndex, null);
    }

    public void expandMenus(final int topMenuIndex, final int bottomMenuIndex, final Animator.AnimatorListener listener) {
        if ((expandedTopMenuIndex >= 0) && expandedBottomMenuIndex < 0) {
            collapseMenu(MenuPosition.Top, expandedTopMenuIndex, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    expandedTopMenuIndex = -1;
                    expandMenus(topMenuIndex, bottomMenuIndex, listener);
                }
            });
            return;
        } else if (expandedTopMenuIndex < 0 && expandedBottomMenuIndex >= 0) {
            collapseMenu(MenuPosition.Bottom, expandedBottomMenuIndex, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    expandedBottomMenuIndex = -1;
                    expandMenus(topMenuIndex, bottomMenuIndex, listener);
                }
            });
            return;
        } else if (expandedTopMenuIndex >= 0 && expandedBottomMenuIndex >= 0) {
            collapseAllMenus(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    expandedTopMenuIndex = -1;
                    expandedBottomMenuIndex = -1;
                    expandMenus(topMenuIndex, bottomMenuIndex, listener);
                }
            });
            return;
        }
        expandedTopMenuIndex = topMenuIndex;
        expandedBottomMenuIndex = bottomMenuIndex;
        final ArrayList<Animator> rotateAnimators = AnimatorHelper.getRotateAnimators(rotationDuration, translationDuration / rotationDuration,
                getMenu(MenuPosition.Top, expandedTopMenuIndex), getMenu(MenuPosition.Bottom, expandedBottomMenuIndex));
        final ArrayList<Animator> translationAnimators = AnimatorHelper.getAnimators(
                AnimatorHelper.getExpandTopAnimators(centerPoint, expandedWidth, expandedHeight, getMenu(MenuPosition.Top, expandedTopMenuIndex)),
                AnimatorHelper.getExpandBottomAnimators(centerPoint, expandedWidth, expandedHeight, getMenu(MenuPosition.Bottom, expandedBottomMenuIndex)));
        AnimatorHelper.startAnimators(translationAnimators, translationDuration, interpolator, animatorListener, listener);
        AnimatorHelper.startAnimators(rotateAnimators, null, null);
    }

    public void collapseMenu(MenuPosition position, int menuIndex) {
        collapseMenu(position, menuIndex, null);
    }

    public void collapseMenu(MenuPosition position, int menuIndex, Animator.AnimatorListener listener) {
        final ArrayList<Animator> rotateAnimators = AnimatorHelper.getRotateAnimators(rotationDuration, translationDuration / rotationDuration, getMenu(position, menuIndex));
        final ArrayList<Animator> translationAnimators = AnimatorHelper.getCollapseAnimators(centerPoint, getMenu(position, menuIndex));
        switch (position) {
            case Top:
                expandedTopMenuIndex = -1;
                break;
            case Bottom:
            default:
                expandedBottomMenuIndex = -1;
                break;
        }
        AnimatorHelper.startAnimators(translationAnimators, translationDuration, interpolator, animatorListener, listener);
        AnimatorHelper.startAnimators(rotateAnimators, null, null);
    }

    public void collapseAllMenus(Animator.AnimatorListener listener) {
        final ArrayList<Animator> rotateAnimators = AnimatorHelper.getRotateAnimators(rotationDuration, translationDuration / rotationDuration,
                getMenu(MenuPosition.Top, expandedTopMenuIndex), getMenu(MenuPosition.Bottom, expandedBottomMenuIndex));
        final ArrayList<Animator> translationAnimators = AnimatorHelper.getCollapseAnimators(centerPoint,
                getMenu(MenuPosition.Top, expandedTopMenuIndex), getMenu(MenuPosition.Bottom, expandedBottomMenuIndex));
        AnimatorHelper.startAnimators(translationAnimators, translationDuration, interpolator, animatorListener, listener);
        AnimatorHelper.startAnimators(rotateAnimators, null, null);
        expandedTopMenuIndex = -1;
        expandedBottomMenuIndex = -1;
    }

    @Override
    public void onClick(View v) {
        if (onMenuItemsClickListener != null) {
            for (int i = 0; i < topMenus.size(); i++) {
                if (topMenus.get(i).contains(v)) {
                    onMenuItemsClickListener.onMenuItemsClick(MenuPosition.Top, i, topMenus.get(i).indexOf(v), v);
                    break;
                }
            }
            for (int i = 0; i < bottomMenus.size(); i++) {
                if (bottomMenus.get(i).contains(v)) {
                    onMenuItemsClickListener.onMenuItemsClick(MenuPosition.Bottom, i, bottomMenus.get(i).indexOf(v), v);
                    break;
                }
            }
        }
    }

    public enum MenuPosition {Top, Bottom}

    public interface OnMenuItemsClickListener {

        void onMenuItemsClick(MenuPosition position, int menuIndex, int viewIndex, View view);
    }
}
