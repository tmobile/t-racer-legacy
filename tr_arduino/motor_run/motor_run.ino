
#include <Servo.h>
Servo ESC;     // create servo object to control the ESC
Servo myservo;
int potValue;  // value from the analog pin
int minSpeed = 1100;
int maxSpeed = 1500;
int servoVal = 0;
int steerVal = minSpeed;
char mode = '?';
String inputString = "";         // a String to hold incoming data
bool stringComplete = false;  // whether the string is complete

void setup() {
  // Attach the ESC on pin 9
  Serial.begin(9600);
  Serial.println("T-Racer GO!!");
  ESC.attach(9,1000,1200); // (pin, min pulse width, max pulse width in microseconds) 
  myservo.attach(10);
  inputString.reserve(200);
}

int pos = 1000;

void loop() {
//  potValue = map(potValue, 0, 1023, 0, 180);   // scale it to use it with the servo library (value between 0 and 180)
  
//  ESC.write(potValue);    // Send the signal to the ESC
//
//  for (pos = minSpeed; pos <= maxSpeed; pos += 1) { // goes from 0 degrees to 180 degrees
//    // in steps of 1 degree
//    ESC.write(pos);              // tell servo to go to position in variable 'pos'
//    delay(15);                       // waits 15ms for the servo to reach the position
//  }
//  
//  for (pos = maxSpeed; pos >= minSpeed; pos -= 1) { // goes from 180 degrees to 0 degrees
//    ESC.write(pos);              // tell servo to go to position in variable 'pos'
//    delay(15);                       // waits 15ms for the servo to reach the position
//  }
//
//  for (pos = 90; pos <= 180; pos += 1) { // goes from 0 degrees to 180 degrees
//    // in steps of 1 degree
//    myservo.write(pos);              // tell servo to go to position in variable 'pos'
//    delay(15);                       // waits 15ms for the servo to reach the position
//  }
//  
//  for (pos = 180; pos >= 0; pos -= 1) { // goes from 180 degrees to 0 degrees
//    myservo.write(pos);              // tell servo to go to position in variable 'pos'
//    delay(15);                       // waits 15ms for the servo to reach the position
//  }
//
//  for (pos = 0; pos <= 90; pos += 1) { // goes from 0 degrees to 180 degrees
//    // in steps of 1 degree
//    myservo.write(pos);              // tell servo to go to position in variable 'pos'
//    delay(15);                       // waits 15ms for the servo to reach the position
//  }
//  


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
