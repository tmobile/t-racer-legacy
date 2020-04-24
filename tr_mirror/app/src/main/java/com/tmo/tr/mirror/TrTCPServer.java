package com.tmo.tr.mirror;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import static android.content.ContentValues.TAG;

public class TrTCPServer implements Runnable {
    private int mServerPort;
    private Context mContext;
    private Handler mHandler;
    private MainActivity mActivityInstance;
    private BitmapFactory.Options bitmap_options = new BitmapFactory.Options();
    private Thread currentThread;
    private TCPSocket ts;
    private boolean runFlag;

    public TrTCPServer(Context context, int serverport, Handler handler){
        super();
        mContext=context;
        mServerPort = serverport;
        mHandler = handler;
        mActivityInstance = (MainActivity)mContext;
        ts = null;
        runFlag = true;
    }


    //Logic for only one server thread to run at a time.  Otherwise, multiple threads will update
    // the cameraview and flicker between the images.
    public void run() {
        try {
            ServerSocket ss = new ServerSocket(mServerPort);

            while (true){
                Socket s = ss.accept();
                if (ts != null) {
                    ts.shutdownNow();
                }
                if (currentThread != null && currentThread.isAlive()) {
                    currentThread.interrupt();
                    currentThread = null;
                }

                ts = new TCPSocket(s);
                currentThread = new Thread(ts);
                currentThread.start();
            }
        } catch(Exception e) {
            Log.d("TrTCPServer", "run: error");
        }
    }



    public class TCPSocket implements Runnable {

        Socket socket;
        boolean runFlag = true;

        public TCPSocket(Socket socket) {
            this.socket = socket;
        }

        public void shutdownNow() {
            runFlag = false;
        }

        @Override
        public void run() {
            if(socket !=null){

                System.out.println("Client   IP: " + socket.getInetAddress().toString().replace("/", ""));
                System.out.println("Client Port: " + socket.getPort());

                try {
                    InputStream inStream = socket.getInputStream();
                    DataInputStream is = new DataInputStream(inStream);

                    while (runFlag) {
                        try {
                            if (is.available() != 0) {
                                int preamble = is.readInt();
                                if (preamble == 11111) {
                                    //TODO: search for a better preamble in case the stream is off.
                                    // how to skip ahead?
                                    int imgLength = is.readInt();
                                    System.out.println("getLength:" + imgLength);
                                    byte[] buffer = new byte[imgLength];
                                    int len = 0;
                                    while (len < imgLength) {
                                        len += is.read(buffer, len, imgLength - len);
                                    }
                                    Message m = mHandler.obtainMessage();
                                    m.obj = BitmapFactory.decodeByteArray(buffer, 0, buffer.length, bitmap_options);
                                    if (m.obj != null) {
                                        mHandler.sendMessage(m);
                                    } else {
                                        Log.d(TAG,"Decode Failed");
                                    }
                                } else {
                                    Log.d(TAG, "Incorrect header" + preamble);

                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
