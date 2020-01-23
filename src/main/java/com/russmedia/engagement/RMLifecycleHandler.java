package com.russmedia.engagement;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.russmedia.engagement.listener.LifecycleListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RMLifecycleHandler implements Application.ActivityLifecycleCallbacks {

    public static final long CHECK_DELAY = 500;
    public static final String TAG = RMLifecycleHandler.class.getName();

    private boolean foreground = false, paused = true;
    private Handler handler = new Handler();
    private List<LifecycleListener> listeners = new CopyOnWriteArrayList<>();
    private Runnable check;

    public boolean isForeground(){
        return foreground;
    }

    public boolean isBackground(){
        return !foreground;
    }

    public void addListener(LifecycleListener listener){
        listeners.add(listener);
    }

    public void removeListener(LifecycleListener listener){
        listeners.remove(listener);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        paused = false;
        boolean wasBackground = !foreground;
        foreground = true;

        if (check != null)
            handler.removeCallbacks(check);

        if (wasBackground){
            Log.i(TAG, "went foreground");
            for (LifecycleListener l : listeners) {
                try {
                    l.onBecameForeground();
                } catch (Exception exc) {
                    Log.e(TAG, "Listener threw exception!", exc);
                }
            }
        } else {
            Log.i(TAG, "still foreground");
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        paused = true;

        if (check != null)
            handler.removeCallbacks(check);

        handler.postDelayed(check = new Runnable(){
            @Override
            public void run() {
                if (foreground && paused) {
                    foreground = false;
                    Log.i(TAG, "went background");
                    for (LifecycleListener l : listeners) {
                        try {
                            l.onBecameBackground();
                        } catch (Exception exc) {
                            Log.e(TAG, "Listener threw exception!", exc);
                        }
                    }
                } else {
                    Log.i(TAG, "still foreground");
                }
            }
        }, CHECK_DELAY);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(Activity activity) {}

    @Override
    public void onActivityStopped(Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    @Override
    public void onActivityDestroyed(Activity activity) {}
}