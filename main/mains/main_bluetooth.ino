void setup() {
  //11520bpsでポートを開く
  Serial.begin(115200);

}

void loop() {
  //シリアルポートに到着してるデータのバイト数が0より大きい場合
  if (Serial.available() > 0) {
    int input = Serial.read();
    //受信確認でログ吐かせてみた
    Serial.println(input);
    Serial.println("hello");
  }
}
