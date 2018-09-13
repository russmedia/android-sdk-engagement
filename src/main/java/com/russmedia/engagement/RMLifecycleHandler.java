package com.russmedia.engagement;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;


public class RMLifecycleHandler implements Application.ActivityLifecycleCallbacks {
    // I use four separate variables here. You can, of course, just use two and
    // increment/decrement them instead of using four and incrementing them all.
    private int resumed;
    private int paused;
    private int started;
    private int stopped;

    private boolean background = false;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {

        ++resumed;

        boolean foreground = resumed > paused;

        if (foreground && background) {
            background = false;

            EngagementEngine.getInstance().send_ping();
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        ++paused;

        boolean foreground = resumed > paused;

        if (!foreground && !background) {
            background = true;

            EngagementEngine.getInstance().send_ping();
        }

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        ++started;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        ++stopped;

    }

    // If you want a static function you can use to check if your application is
    // foreground/background, you can use the following:
    /*
    // Replace the four variables above with these four
    private static int resumed;
    private static int paused;
    private static int started;
    private static int stopped;

    // And these two public static functions
    public static boolean isApplicationVisible() {
        return started > stopped;
    }

    public static boolean isApplicationInForeground() {
        return resumed > paused;
    }
    */
}
