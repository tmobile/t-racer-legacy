#include <Servo.h>
Servo Motor1;
Servo Motor2; 
 
const long MF = 20;  // angle that moves motor forward
const long MB = 138; // angle that moves motor backward
const long MS = 91; // angle that stops the motor
 
void setup()
{
 // The white pin of the VEX motor controller 29
 // connects to pin 11 (or any other PWM pin)
  Motor1.attach(9);
  Motor2.attach(10);
  Serial.begin(9600);
  Serial.println("T-Racerv2");
  
}

// 79 == 110 
//31 / 2 = 15.5
//94.5 mid point
int mid_point = 96;
void loop() 
{
//  Serial.println("Forward");

  for (int i = 1; i < 80 ; i++ )
  {
      Motor1.write(mid_point - i); // move forward
      Motor2.write(mid_point + i);
      Serial.println(i);

      delay(50);     
  }
  delay(2000);

//  Motor.write(MF); // move forward
//  delay(2000);     
//
//  Serial.println("Stop");
//  Motor.write(MS); // stop motor
//  delay(2000);  
//  
//  Serial.println("Backward");
//  Motor.write(MB); // move backward
//  delay(2000);
//  
//  Serial.println("Stop");
//  Motor.write(MS); // stop motor
//  delay(2000);   
}
