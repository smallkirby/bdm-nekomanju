#!/bin/bash
cat <<ECHOEOF > /etc/bluetooth/rfcomm.conf
rfcomm0 {
        bind no;
        device <target IP>;
        channel 1;
        comment Serial Port;
        }
ECHOEOF

sudo rfcomm connect 0 <target addr> &
echo "Now, waiting to be connected..."
sleep 5
sudo screen /dev/rfcomm0
