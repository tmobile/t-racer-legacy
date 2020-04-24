package com.tmo.tr.sensor;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private Camera mCamera;
    public TrCameraView mPreview;
    public static String SERVERIP = "localhost";
    public static final int port_num = 31337;
    private static String host_name = "192.168.1.3";
    private String resolution = "720p";
    private Handler handler = new Handler();
    private SharedPreferences userDetails;
    private DisplayMetrics displayMetrics;


    public final String ACTION_USB_PERMISSION = "com.hariharan.arduinousb.USB_PERMISSION";

    UsbManager usbManager;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;

    public void initializeUSB() {

        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();

        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();

                if (deviceVID == 9025 || deviceVID == 1027) {//Arduino Vendor ID

                    PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, pi);
                    keep = false;
                } else {
                    connection = null;
                    device = null;
                }

                if (!keep)
                    break;
            }
        }
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = usbManager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {
                        if (serialPort.open()) { //Set Serial Connection Parameters.
                            serialPort.setBaudRate(9600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback);

                        } else {
                            Log.d("SERIAL", "PORT NOT OPEN");
                        }
                    } else {
                        Log.d("SERIAL", "PORT IS NULL");
                    }
                } else {
                    Log.d("SERIAL", "PERM NOT GRANTED");
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                initializeUSB();
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {

            }
        }
    };

    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            String data = null;
            try {
                data = new String(arg0, "UTF-8");
                data.concat("/n");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        usbManager = (UsbManager) getSystemService(USB_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        this.registerReceiver(broadcastReceiver, filter);
        initializeUSB();

        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        userDetails = this.getSharedPreferences("perferences", MODE_PRIVATE);
        host_name = userDetails.getString("host_name", host_name);
        resolution = userDetails.getString("resolution", resolution);

        setWindowOptions();

        setContentView(R.layout.activity_main);

        Button preferences = (Button) findViewById(R.id.preferences_button);

        preferences.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent preferencesIntent = new Intent(view.getContext(), Preferences.class);
                preferencesIntent.putExtra("ip_address", host_name);
                preferencesIntent.putExtra("resolution", resolution);
                startActivityForResult(preferencesIntent, 0);
            }

        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            host_name = data.getStringExtra("ip_address");
            resolution = data.getStringExtra("resolution");
            SharedPreferences.Editor edit = userDetails.edit();
            edit.putString("host_name", host_name);
            edit.putString("resolution", resolution);
            edit.apply();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setWindowOptions();
        SERVERIP = getLocalIpAddress();
        String titleBar = SERVERIP + " (" +resolution + ")";
        Button preferences = (Button) findViewById(R.id.preferences_button);
        preferences.setText(titleBar.toCharArray(), 0, titleBar.length());

        mCamera = Camera.open();
        mPreview = new TrCameraView(host_name, port_num, this, mCamera, resolution, displayMetrics, serialPort);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
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
