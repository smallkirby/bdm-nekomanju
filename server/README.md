## 実装要件
  - arduinoBTとは常にシリアル接続しておき、データは常に受信する
  - 受信したデータは特定のデータリミット(1回/min)でまとめてRealtimeDatabaseに送信する
    - データ形式はRealtimeDatabaseのスキーマに沿うように
      - data > 緯度経度 > 時刻ISO表示 > co2,temp,humid
    - 受審した全てのデータでなく、一部を送信?
    - 送信に成功したことを確認
## ファイル
### プログラム
  - add-bluetooth-data : シリアル通信で受信したデータをサーバーに送信
  - add-test-data : テストデータをサーバーに送信
  - send-backup-data : backup(config.iniで指定)ファイルの内容をサーバーに送信
### ファイル
  - config.ini : 鍵ファイルなどの指定
  - backup.json : 一行ごとに送信したデータが全て入っている