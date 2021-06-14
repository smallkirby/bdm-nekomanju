#!/bin/sh
#nmcli dev wifi
# nmcli device wifi connect <SSID> password <password>
sudo rfcomm connect 0 <target device addr> &
echo "Now, waiting to be connected..."
sleep 5
python3 ./add-bluetooth-data.py
