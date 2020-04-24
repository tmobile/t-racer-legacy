package com.hariharan.arduinousb;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import android.widget.Toast;
import java.nio.ByteBuffer;
import io.github.controlwear.virtual.joystick.android.JoystickView;

public class MainActivity extends Activity {

    private TextView mTextViewAngleRight;
    private TextView mTextViewStrengthRight;
    private TextView mTextViewCoordinateRight;

    TextView textView;
    EditText editText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //THIS IS PROBABLY THE WRONG WAY TO GET AROUND RUNNING A NETWORK CALL ON THE MAIN THREAD
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        editText = (EditText) findViewById(R.id.editText);
        textView = (TextView) findViewById(R.id.textView);
        IntentFilter filter = new IntentFilter();


        mTextViewAngleRight = (TextView) findViewById(R.id.textView_angle_right);
        mTextViewStrengthRight = (TextView) findViewById(R.id.textView_strength_right);
        mTextViewCoordinateRight = findViewById(R.id.textView_coordinate_right);

        final JoystickView joystickRight = (JoystickView) findViewById(R.id.joystickView_right);
        joystickRight.setOnMoveListener(new JoystickView.OnMoveListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onMove(int angle, final int strength) {

                mTextViewAngleRight.setText(angle + "°");
                mTextViewStrengthRight.setText(strength + "%");
                String msg = String.format("x%03d:y%03d",
                        joystickRight.getNormalizedX(),
                        joystickRight.getNormalizedY());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        simpleSend(joystickRight.getNormalizedX(), strength);
                    }
                });

                mTextViewCoordinateRight.setText(msg);

            }
        });
    }

    public void simpleSend(int steering, int accel) {
        int steerMidPoint = 100;
        int steerRange = 40;
        int accForwardMin = 1600;
        int accNeutral = 1400;

        if (accel > 70)
            accel = (int)(100.0 * (accel - 70.0)/30.0) + 1500;  //This is to keep the speed of the vehicl
        else if (accel > 50)
            accel = 1500;
        else
            accel = accNeutral;

        if (steering > 50)
            steering = steerMidPoint + (steering -50) * 40 / 50;
        else if(steering < 50)
            steering = steerMidPoint - (50 - steering) * 40/50;
        else
            steering = steerMidPoint;

        try {
            DatagramSocket clientSocket = new DatagramSocket();
//            InetAddress IPAddress = InetAddress.getByName("192.168.1.137");
            InetAddress IPAddress = InetAddress.getByName("192.168.0.12");
//
            String combined = "s" + steering + "xa" + accel+ "x";
            byte[] bytes = combined.getBytes();

            DatagramPacket sendPacket = new DatagramPacket(bytes, bytes.length, IPAddress, 31337);
            clientSocket.send(sendPacket);
            clientSocket.close();
            tvSetText(textView, "\nData Sent : " + combined + "\n");
        } catch(Exception ex) {
            ex.printStackTrace();
        }

    }


    private void tvSetText(TextView tv, CharSequence text) {
        final TextView ftv = tv;
        final CharSequence ftext = text;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ftv.setText(ftext);
            }
        });
    }

}
