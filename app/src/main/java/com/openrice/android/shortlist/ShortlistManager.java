package com.openrice.android.shortlist;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnticipateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by raychum on 15/7/15.
 */
public class ShortlistManager {

    public static final int MAX_RESTAURANT_COUNT = 4;
    private static ShortlistManager mInstance = null;
    private final List<ShortlistModel> shortlistModels = new ArrayList<>();
    private final ShortlistServiceConnection serviceConnection = new ShortlistServiceConnection();
    private ShortlistService shortlistService = null;
    private boolean isBoundService = false;

    private ShortlistManager() {
    }

    public static ShortlistManager getInstance() {
        if (mInstance == null)
            mInstance = new ShortlistManager();
        return mInstance;
    }

    public void startService(@NonNull final Context context, Callback<Boolean> connectionStatusCallback) {
        Intent intent = new Intent(context.getApplicationContext(), ShortlistService.class);
        if (connectionStatusCallback != null) {
            serviceConnection.addCallback(connectionStatusCallback);
        }
        context.getApplicationContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @NonNull
    public void stopService(@NonNull final Context context, Callback<Boolean> connectionStatusCallback) {
        if (isBoundService) {
            isBoundService = false;
            context.getApplicationContext().unbindService(serviceConnection);
            if (connectionStatusCallback != null) {
                connectionStatusCallback.onCallback(isBoundService);
                serviceConnection.removeCallback(connectionStatusCallback);
            }
            shortlistModels.clear();
        }
    }

    public void addShortlistItem(final Context context, int id, final String img, boolean isRMS, final ImageView screenView) {
        if (!isContainsPoi(id)) {
            if (shortlistModels.size() >= MAX_RESTAURANT_COUNT) {
                shortlistModels.remove(0);
            }
            shortlistModels.add(new ShortlistModel(id, img, isRMS));
            final Callback<Boolean> callback = new Callback<Boolean>() {
                @Override
                public void onCallback(Boolean isConnected) {
                    if (isConnected && shortlistService != null) {
                        final RoundedImageView imageButton = (RoundedImageView) LayoutInflater.from(context).inflate(R.layout.view_shortlist_round_image_item, shortlistService.getShortlistLayout(), false);
                        Picasso.with(context).load(img).fit().centerCrop().into(imageButton);
                        shortlistService.addViewToMenu(ShortlistLayout.MenuPosition.Top, imageButton, 0);
                        startAnimation(context, screenView);
                    }
                }
            };
            if (isBoundService) {
                callback.onCallback(true);
            } else {
                startService(context, callback);
            }
        }
    }

    private boolean isContainsPoi(int id) {
        for (ShortlistModel shortlistModel : shortlistModels) {
            if (shortlistModel.id == id) {
                return true;
            }
        }
        return false;
    }

    private void startAnimation(final Context context, final ImageView screenView) {
        View root = ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);
        Bitmap bmp = AnimatorHelper.getBitmapFromView(root);
        screenView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        screenView.setVisibility(View.VISIBLE);
        screenView.setImageBitmap(bmp);
        screenView.postDelayed(new Runnable() {
            @Override
            public void run() {
                WindowManager.LayoutParams params = shortlistService.getFloatingViewParams();
                final ValueAnimator valueAnimator = AnimatorHelper.getResizeAndTranslationAnimator(screenView, 400,
                        0, screenView.getX(), screenView.getY(), -1 * screenView.getWidth() / 2 + params.x + shortlistService.getFloatingView().getWidth() / 2,
                        -1 * screenView.getHeight() / 2 + params.y - shortlistService.getFloatingView().getHeight() / 2,
                        new AnticipateInterpolator(), null, new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animator) {
                                updateFloatingViewCount(String.valueOf(shortlistModels.size()));
                                animator.removeListener(this);
                                screenView.setVisibility(View.INVISIBLE);
                                ((ValueAnimator) animator).reverse();
                            }
                        });
                valueAnimator.start();
            }
        }, 1);
    }

    private void updateFloatingViewCount(String text) {
        final TextView textViewCount = (TextView) shortlistService.getFloatingView().findViewById(R.id.textview_count);
        textViewCount.setText(text);
    }

    private void initFloatingView() {
        shortlistService.initFloatingView(R.layout.view_floating);
    }

    private void initShortlistLayout() {
        final ShortlistLayout shortlistLayout = shortlistService.initShortlistLayout(R.layout.view_shortlist_container);
        final LayoutInflater layoutInflater = LayoutInflater.from(shortlistLayout.getContext());
        ArrayList<View> views = new ArrayList<>();
        ImageView imageButton;

        int[] drawables = new int[]{R.drawable.short_list_modal_share, R.drawable.short_list_modal_invite, R.drawable.short_list_modal_clear};
        for (int drawable : drawables) {
            imageButton = (ImageView) layoutInflater.inflate(R.layout.view_shortlist_item, shortlistLayout, false);
            imageButton.setImageResource(drawable);
            views.add(imageButton);
        }
        shortlistLayout.addMenu(ShortlistLayout.MenuPosition.Bottom, views);
        views = new ArrayList<>();

        drawables = new int[]{R.drawable.short_list_modal_facebook, R.drawable.short_list_modal_whatsapp,
                R.drawable.short_list_modal_line, R.drawable.short_list_modal_wechat};
        for (int drawable : drawables) {
            imageButton = (ImageView) layoutInflater.inflate(R.layout.view_shortlist_item, shortlistLayout, false);
            imageButton.setImageResource(drawable);
            views.add(imageButton);
        }
        shortlistLayout.addMenu(ShortlistLayout.MenuPosition.Bottom, views);
        shortlistLayout.setOnMenuItemsClickListener(new ShortlistLayout.OnMenuItemsClickListener() {
            @Override
            public void onMenuItemsClick(ShortlistLayout.MenuPosition position, int menuIndex, int viewIndex, View view) {
                if (position == ShortlistLayout.MenuPosition.Bottom && menuIndex == 0) {
                    switch (viewIndex) {
                        case 0:
                            shortlistLayout.expandMenu(ShortlistLayout.MenuPosition.Bottom, 1);
                            break;
                        case 2:
                            updateFloatingViewCount(String.valueOf(0));
                            shortlistLayout.collapseAllMenus(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    stopService(shortlistService, null);
                                }
                            });
                            break;
                    }
                }
            }
        });
    }

    private class ShortlistServiceConnection implements ServiceConnection {
        private final ArrayList<Callback<Boolean>> callbacks = new ArrayList<>();

        public void addCallback(Callback<Boolean> callback) {
            if (!callbacks.contains(callback)) {
                callbacks.add(callback);
            }
        }

        public void removeCallback(Callback<Boolean> callback) {
            if (callbacks.contains(callback)) {
                callbacks.contains(callback);
            }
        }

        public synchronized void removeAllCallback() {
            callbacks.clear();
        }

        @Override
        public synchronized void onServiceConnected(ComponentName name, IBinder service) {
            shortlistService = ((ShortlistService.ShortlistBinder) service).getService();
            initShortlistLayout();
            initFloatingView();
            isBoundService = true;
            for (Callback<Boolean> callback : callbacks) {
                callback.onCallback(isBoundService);
            }
            removeAllCallback();
        }

        @Override
        public synchronized void onServiceDisconnected(ComponentName name) {
        }
    }
}
