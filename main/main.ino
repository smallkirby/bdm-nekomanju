#include <Wire.h>
#include <SoftwareSerial.h>
#include "./src/SparkFun_SCD30_Arduino_Library.h" //Click here to get the library: http://librarymanager/All#SparkFun_SCD30
#include "./src/TinyGPS++.h"
#include <LiquidCrystal.h>

LiquidCrystal lcd(3,4,5,6,7,8,9);

SCD30 airSensor;

SoftwareSerial mySerial(10, 11); // RX, TX
TinyGPSPlus gps;

void setup() {
  //11520bpsでポートを開く
  Serial.begin(115200);
  Serial.println("SCD30 Example");
  Wire.begin();
  if (airSensor.begin() == false)
  {
    Serial.println("Air sensor not detected. Please check wiring. Freezing...");
    while (1)
      ;
  }
  Serial.println("Air sensor detected.");
  //gps
  mySerial.begin(9600);
  while(!mySerial){
    Serial.print(".");//waug for serial port to connect.
  }
  mySerial.print("$PMTK104*37\r\n");
  Serial.println("gps detected.");

  lcd.begin(16, 2);
}

void loop() {
  while(mySerial.available() >0){
    char c=mySerial.read();
    //Serial.write(c);
    gps.encode(c);
  }
  if(airSensor.dataAvailable()){ 
    lcd.clear();
    lcd.setCursor(0,1);
    lcd.print("temp:");
    lcd.print(airSensor.getTemperature(), 1);
    lcd.print("c");
    lcd.setCursor(0,0);
    lcd.print("co2:");
    lcd.print(airSensor.getCO2());
    lcd.print("ppm ");
    if(airSensor.getCO2()>=1000){
      char kanki[]= {0xB6,0xDD,0xB7,0x00};
      lcd.print(kanki);
      lcd.blink();
    }else{
      lcd.noBlink();
    }
    if(gps.location.isValid()){ 
      //Serial.println("gps.location.isValid()==FALSE");
      //mySerial.print("$PMTK183*38\r\n");//gps状況の確認
      //Serial.println(mySerial.readStringUntil('\n'));
      Serial.print(gps.location.lat(), 6);
      Serial.print(",");
      Serial.print(gps.location.lng(), 6);
      Serial.print(",");
      Serial.print(gps.altitude.meters());
      Serial.print(",");
      Serial.print(airSensor.getCO2());
      Serial.print(",");
      Serial.print(airSensor.getTemperature(), 1);
      Serial.print(",");
      Serial.print(airSensor.getHumidity(), 1);
      Serial.println();
      }  
  }//else{}
}
 
