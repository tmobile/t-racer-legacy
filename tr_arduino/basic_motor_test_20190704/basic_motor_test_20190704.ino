
#include <Servo.h>
Servo ESC1;     // create servo object to control the ESC
Servo ESC2;     // create servo object to control the ESC

int potValue;  // value from the analog pin
int minSpeed = 500;
int maxSpeed = 1800;
int servoVal = 0;
int steerVal = minSpeed;
char mode = '?';
String inputString = "";         // a String to hold incoming data
bool stringComplete = false;  // whether the string is complete

void setup() {
  Serial.begin(9600);
  Serial.println("T-Racer GO!!");
  
  // Attach the ESC on pin 9
  ESC1.attach(9,minSpeed,maxSpeed); // (pin, min pulse width, max pulse width in microseconds) 
  ESC2.attach(10,minSpeed,maxSpeed); // (pin, min pulse width, max pulse width in microseconds) 

  pinMode(LED_BUILTIN, OUTPUT);

}

int pos = 1000;

int connectivity_counter = 0;

void loop() {
  Serial.println("starting");

  ESC1.write(0); 
  ESC2.write(0);
  delay(2000); 
  for (int i = 0; i < 2000; i++)
  {
    ESC1.write(i); 
    ESC2.write(i); 
    delay(1000);
    Serial.println(i);
  }
    ESC1.write(0); 
    ESC2.write(0); 
  Serial.println("stopping");
  delay(2000);
  // print the string when a newline arrives:
 
}
