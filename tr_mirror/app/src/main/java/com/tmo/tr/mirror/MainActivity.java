package com.tmo.tr.mirror;

import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {
    private ImageView mCameraView;
    public Thread mTCPMirrorServerThread;
    public Bitmap mLastFrame;

    private int mPort = 31337;

    private final Handler handler = new MainActivityHandler(this);

    private DisplayMetrics displayMetrics;
    private boolean scale_image = true;
    private Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mLastFrame!=null){
                            if (scale_image) {
                                long time1 = System.currentTimeMillis();
                                Bitmap tmp = Bitmap.createScaledBitmap(mLastFrame, displayMetrics.widthPixels, displayMetrics.heightPixels, true);
                                long total = System.currentTimeMillis() - time1;
                                Log.d("status", "" + total);

                                mCameraView.setImageBitmap(tmp);
                            } else {
                                mCameraView.setImageBitmap(mLastFrame);
                            }
                        }

                    }
                }); //this function can change value of mInterval.
            } finally {
                handler.postDelayed(mStatusChecker, 1000/15);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowOptions();
        setContentView(R.layout.activity_main);

        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        TextView ip_address = (TextView) findViewById(R.id.ip_address);
        String current_ip_address = getLocalIpAddress();
        ip_address.setText(current_ip_address.toCharArray(), 0, current_ip_address.length());

        mCameraView = (ImageView) findViewById(R.id.camera_preview);

        mTCPMirrorServerThread = new Thread(new TrTCPServer(this, mPort, handler));
        mTCPMirrorServerThread.start();
        mStatusChecker.run();
    }


    @Override
    protected void onResume() {
        super.onResume();
        setWindowOptions();
    }


    private void setWindowOptions() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()&& inetAddress instanceof Inet4Address) { return inetAddress.getHostAddress().toString(); }
                }
            }
        } catch (SocketException ex) {
            Log.e("getLocalIpAddress", ex.toString());
        }
        return null;
    }
}
