
#include <Servo.h>
Servo ESC;     // create servo object to control the ESC
Servo myservo;

int potValue;  // value from the analog pin
int minSpeed = 1000;
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
  ESC.attach(9,minSpeed,maxSpeed); // (pin, min pulse width, max pulse width in microseconds) 
  
  // Attach the Servo on pin 10
  myservo.attach(10);
  
  inputString.reserve(200);

  pinMode(LED_BUILTIN, OUTPUT);

}

int pos = 1000;

int connectivity_counter = 0;

void loop() {

  // print the string when a newline arrives:
  if (stringComplete) {
    if (mode == 's') {
      servoVal = inputString.toInt();
      Serial.println(inputString);
      Serial.print("Steering:");
      Serial.println(servoVal);
      
      if (servoVal > 0 and servoVal < 180) {
        myservo.write(servoVal); 
      }
    } else if (mode == 'a') {
      steerVal = inputString.toInt();
      Serial.print("Acceleration:");
      Serial.println(steerVal);

      if (steerVal > minSpeed and steerVal < maxSpeed) {
        ESC.write(steerVal); 
      }
    }

    // clear the string:
    inputString = "";
    stringComplete = false;
    mode = '?';
  }
}

void serialEvent() {
  while (Serial.available()) {
    // get the new byte:
    char inChar = (char)Serial.read();

    // add it to the inputString:
    if (inChar != 'x' && inChar != 's' && inChar != 'a')
      inputString += inChar;
      
    if (inChar == 'a' || inChar == 's')
      mode = inChar;

    // if the incoming character is a newline, set a flag so the main loop can
    // do something about it:
    if (inChar == 'x') {
      stringComplete = true;
    }
  }
}
