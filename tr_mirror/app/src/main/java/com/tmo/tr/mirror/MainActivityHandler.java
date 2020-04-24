package com.tmo.tr.mirror;


import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

class MainActivityHandler extends Handler {
    private final WeakReference<MainActivity> mActivity;

    public MainActivityHandler(MainActivity activity) {
        mActivity = new WeakReference<MainActivity>(activity);
    }

    @Override
    public void handleMessage(Message msg) {
        MainActivity activity = mActivity.get();
        if (activity != null) {
            try {
                activity.mLastFrame = (Bitmap) msg.obj;
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.handleMessage(msg);
        }
    }
}