#include <LiquidCrystal.h>

LiquidCrystal lcd(3,4,5,6,7,8,9);
void setup() {
  lcd.begin(16, 2);
}

void loop() {
  lcd.clear();
  lcd.setCursor(0,0);
  lcd.print("hello world");
  delay(3000);
}