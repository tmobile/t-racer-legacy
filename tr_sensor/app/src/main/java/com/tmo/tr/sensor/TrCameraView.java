package com.tmo.tr.sensor;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import com.felhr.usbserial.UsbSerialDevice;



public class TrCameraView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Boolean mFrameProcessed = true;
    private DisplayMetrics displayMetrics;

    public ByteArrayOutputStream mFrameBuffer;
    private Context mContext;

    private int FRAMES_PER_SECOND = 30;
    private int WIDTH = 1920;
    private int HEIGHT = 1080;

    private Thread mirrorThread;
    private Thread udpThread;

    UsbSerialDevice mSerialPort;
    Long previousSerialPortTime = System.currentTimeMillis();

    public TrCameraView(String host_name, int port_num, Context context, Camera camera, String resolution, DisplayMetrics dm, UsbSerialDevice serialPort) {
        super(context);
        mContext = context;
        mCamera = camera;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        displayMetrics = dm;
        mSerialPort = serialPort;
        setResolution(resolution);

        mirrorThread = new Thread(new TCPSocket(host_name, port_num));
        mirrorThread.start();

        udpThread = new Thread(new UDPSocket("192.168.1.137", 31337));
        udpThread.start();
    }

    private void setResolution(String resolution) {
        switch (resolution) {
            case "1080p":
                WIDTH = 1920;
                HEIGHT = 1080;
                break;
            case "720p":
                WIDTH = 1280;
                HEIGHT = 720;
                break;
            case "800":
                WIDTH = 800;
                HEIGHT = 600;
                break;
            case "640":
                WIDTH = 640;
                HEIGHT = 480;
                break;
            case "320":
                WIDTH = 320;
                HEIGHT = 240;
                break;
            case "176":
                WIDTH = 176;
                HEIGHT = 144;
                break;
            default:
                break;
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (mCamera != null)
                mCamera.setPreviewDisplay(holder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera!= null) {
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
            mirrorThread = null;
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        try {
            if (mCamera != null) {
                mCamera.stopPreview();
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setPreviewSize(WIDTH, HEIGHT);
                parameters.setPreviewFormat(ImageFormat.NV21);
                mCamera.setParameters(parameters);
                mCamera.setPreviewCallback(this);
                mCamera.startPreview();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onPreviewFrame(byte[] data, Camera camera) {
        try {

            if (!mFrameProcessed) return;

            long start = System.currentTimeMillis();
            YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, WIDTH, HEIGHT, null);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            //QoS -- change width and height dynamically
            yuvimage.compressToJpeg(new Rect(0, 0, WIDTH, HEIGHT), 100, outputStream);
            mFrameBuffer = outputStream;
            mFrameProcessed = false;

            Log.d("parse", "Difference is : " + (System.currentTimeMillis() - start));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class TCPSocket implements Runnable {

        Socket socket = null;
        OutputStream outputStream = null;
        public String hostName = "192.168.1.4";

        public int portNo = 31337;

        public TCPSocket(String hostName, int portNo) {

            if (hostName != null && hostName != "")
                this.hostName = hostName;

            if (portNo != 0)
                this.portNo = portNo;

            clientConnect();

            if (socket != null) {
                System.out.println("Client   IP: " + socket.getInetAddress().toString().replace("/", ""));
                System.out.println("Client Port: " + socket.getPort());
            }
        }

        public void clientConnect() {
            try {
                if (hostName == null || hostName == "" || portNo == 0) {
                    Log.d("clientConnect", "Host Name or Port Number is not provided");
                    Thread.sleep(2000);
                    return;
                }

                if (socket != null) {
                    if (outputStream != null)
                        outputStream.close();
                    socket.close();
                }
                socket = new Socket(hostName, portNo);
                socket.setKeepAlive(true);
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    Thread.sleep(500);
                } catch (Exception ex) {
                    Log.d("socket_connection", "Sleep threw an error");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(500);
                } catch (Exception ex) {
                    Log.d("socket_connection", "Sleep threw an error");
                }
            }
        }

        @Override
        public void run() {
            while (true) {
                try {
                    if (socket == null)
                        clientConnect();
                    else {
                        if (mFrameBuffer != null) {
                            if (!socket.isClosed()) {
                                DataOutputStream dataOutputStream = new DataOutputStream(this.outputStream);
                                dataOutputStream.writeInt(11111);
                                dataOutputStream.writeInt(mFrameBuffer.size());
                                byte[] tmp = mFrameBuffer.toByteArray();
                                dataOutputStream.flush();
                                dataOutputStream.write(tmp);
                                dataOutputStream.flush();
                                mFrameProcessed = true;
                            }
                        }
                    }
                    Thread.sleep(1000 / FRAMES_PER_SECOND / 2);
                } catch (SocketException ex) {
                    ex.printStackTrace();
                    try {
                        if (socket != null)
                            socket.close();
                    } catch (Exception ex2) {
                        ex2.printStackTrace();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    try {
                        if (outputStream != null)
                            outputStream.close();
                        Thread.sleep(1000);

                    } catch (Exception ex2) {
                        ex2.printStackTrace();
                    }
                }
            }
        }
    }

    public class UDPSocket implements Runnable {

        Socket socket = null;
        OutputStream outputStream = null;
        public String hostName = "192.168.1.137";

        public int portNo = 31337;
        private InetAddress IPAddress;

        public UDPSocket(String hostName, int portNo) {

            if (hostName != null && hostName != "")
                this.hostName = hostName;

            if (portNo != 0)
                this.portNo = portNo;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    if (mFrameBuffer != null) {

                        byte[] bytes = mFrameBuffer.toByteArray();

                        if (bytes.length > 65000) {

                        }

                        byte[] receiveData = new byte[1000];
                        long start = System.currentTimeMillis();
                        DatagramSocket clientSocket = new DatagramSocket();
                        IPAddress = InetAddress.getByName(hostName);
                        DatagramPacket sendPacket = new DatagramPacket(bytes, bytes.length, IPAddress, portNo);

                        clientSocket.send(sendPacket);

                        clientSocket.setSoTimeout(500);
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        clientSocket.receive(receivePacket);
                        String driveCommand = new String(receiveData, 0, receivePacket.getLength()).trim();

                        clientSocket.close();

                        if (mSerialPort != null)
                            simpleSend(driveCommand);

                        Thread.sleep(1000 / FRAMES_PER_SECOND / 2);
                    }
                } catch (SocketException ex) {
                    ex.printStackTrace();
                    try {
                        if (socket != null)
                            socket.close();
                    } catch (Exception ex2) {
                        ex2.printStackTrace();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    try {
                        if (outputStream != null)
                            outputStream.close();
                        Thread.sleep(1000);

                    } catch (Exception ex2) {
                        ex2.printStackTrace();
                    }
                }
            }
        }

        public void simpleSend(String command) {
            if (mSerialPort == null)
                return;

            Long currentSerialPortTime = System.currentTimeMillis();

            if (( currentSerialPortTime - previousSerialPortTime ) > 33) {
                previousSerialPortTime = currentSerialPortTime;
                mSerialPort.write(command.getBytes());
            }

        }
    }

}

