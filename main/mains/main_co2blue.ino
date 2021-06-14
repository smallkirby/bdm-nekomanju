#include <Wire.h>
#include <SoftwareSerial.h>
#include "./src/SparkFun_SCD30_Arduino_Library.h" //Click here to get the library: http://librarymanager/All#SparkFun_SCD30
SCD30 airSensor;

SoftwareSerial mySerial(10, 11); // RX, TX

void setup() {
  //11520bpsでポートを開く
  mySerial.begin(115200);
  mySerial.println("SCD30 Example");
  Wire.begin();
  if (airSensor.begin() == false)
  {
    mySerial.println("Air sensor not detected. Please check wiring. Freezing...");
    while (1)
      ;
  }
}

void loop() {
  //シリアルポートに到着してるデータのバイト数が0より大きい場合
  if (Serial.available() > 0) {
    int input = Serial.read();
    //受信確認でログ吐かせてみた
    mySerial.println(input);
    mySerial.println("hello");
    
  }
  if (airSensor.dataAvailable()){
    mySerial.print("co2(ppm):");
    mySerial.print(airSensor.getCO2());
    mySerial.println();
  }
}
