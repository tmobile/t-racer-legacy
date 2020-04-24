using UnityEngine;
using UnityEngine.UI;

using System;
using System.Text;
using System.Net;
using System.Net.Sockets;
using System.Collections;


public class DetectUnknownControllerMappings : MonoBehaviour {

    //axes
    public Text axis1Value;
    public Text axis2Value;
    public Text axis3Value;
    public Text axis4Value;

    //buttons
    public Text button0Value;
    public Text button1Value;
    public Text button2Value;
    public Text button3Value;

    public Text steeringValue;
    public Text accelValue;

    public InputField ipAddress;
    static public UdpClient client;
    static public IPAddress serverIp;
    static public string hostIp = "";
    static public int hostPort = 31337;
    static private IPEndPoint hostEndPoint;
    float trim_steering = 100;
    float trim_accel = 1400;

    int BASE_STEERING = 100;
    int RANGE_STEERING = -40;

    int BASE_ACCEL = 1400;
    int RANGE_ACCEL = -400;

    const int TRIM_STEERING_MAX = 120;
    const int TRIM_STEERING_MIN = 80;


    const int TRIM_ACCEL_MAX = 1500;
    const int TRIM_ACCEL_MIN = 1300;

    const float BUTTON_DELTA = 0.1f;
    string prev_control_value = "";
    void SetupIpAddress()
    {
        if (ipAddress.text != null && ipAddress.text.Length > 7 && ipAddress.text != hostIp)
        {
            serverIp = IPAddress.Parse(ipAddress.text);
            hostEndPoint = new IPEndPoint(serverIp, hostPort);
            client = new UdpClient();
            client.Client.Blocking = false;
            client.Connect(hostEndPoint);

            hostIp = ipAddress.text;
        }
    }

    void Start()
    {
        Debug.Log("Do Startup stuff here.");
    }


    public void SendDgram(string msg)
    {
        SetupIpAddress();
        byte[] dgram = Encoding.UTF8.GetBytes(msg);
        client.Send(dgram, dgram.Length);
    }

    // Update is called once per frame
    void Update () {
        //axis

        axis1Value.text = Input.GetAxis("Axis 1").ToString();  //left axis left(neg) and right (pos) -- not used
        axis4Value.text = Input.GetAxis("Axis 4").ToString();
        axis2Value.text = Input.GetAxis("Axis 2").ToString();
        axis3Value.text = Input.GetAxis("Axis 3").ToString();

        int accel = (int)(RANGE_ACCEL * Input.GetAxis("Axis 2") + trim_accel);  //left axis up(neg) and down (pos) -- acceleration
        int steering = (int)(RANGE_STEERING * Input.GetAxis("Axis 3") + trim_steering); //right axis left(neg) and right (pos) -- steering

        accelValue.text = accel.ToString();
        steeringValue.text = steering.ToString();

        string control_value = String.Format("s{0}xa{1}x", steering, accel);

        if (prev_control_value != control_value)
        {
            Debug.Log(control_value);
            SendDgram(control_value);
            prev_control_value = control_value;
        }

        //buttons
        if (Input.GetKey(KeyCode.JoystickButton0) == true)
        {
            if (trim_steering > TRIM_STEERING_MIN)
            {
                trim_steering -= BUTTON_DELTA;
            }
            button0Value.text = trim_steering.ToString();

        }

        if (Input.GetKey(KeyCode.JoystickButton1) == true)
        {
            if (trim_steering < TRIM_STEERING_MAX)
            {
                trim_steering += BUTTON_DELTA;
            }

            button0Value.text = trim_steering.ToString();
        }


        if (Input.GetKey(KeyCode.JoystickButton2) == true)
        {
            if (trim_accel > TRIM_ACCEL_MIN)
            {
                trim_accel -= BUTTON_DELTA;
            }

            button2Value.text = trim_accel.ToString();
        }

        if (Input.GetKey(KeyCode.JoystickButton3) == true)
        {
            if (trim_accel < TRIM_ACCEL_MAX)
            {
                trim_accel += BUTTON_DELTA;
            }

            button2Value.text = trim_accel.ToString();
        }
    }
}
