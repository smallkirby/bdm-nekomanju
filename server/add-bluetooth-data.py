#!/usr/bin/env python
#encoding: utf-8;

import firebase_admin
from firebase_admin import db
from firebase_admin import credentials
import time
import datetime
import serial
import schedule
import numpy as np
import json
import configparser

config = configparser.ConfigParser()
config.read('./config.ini')
# init app: no credential for now
cred = credentials.Certificate(config["nekomanju"]["certfile"])
app = firebase_admin.initialize_app(cred, {
  'databaseURL': config["nekomanju"]["url"],
})
data_ref = db.reference("data")
#backup file
backup_file=open(config["nekomanju"]["backup"], mode='a')

# serial communication.
readSer = serial.Serial('/dev/tty.RNBT-B032-RNI-SPP',115200, timeout=100)
data_queue = []

# update node. create new node if not exists.
#recieved_data = [35.897720,139.944625,26.90,591,18.5,27.2]
def send_to_firebase(recieved_data):
    global backup_file
    cur_lat= recieved_data[0]
    cur_lng= recieved_data[1]
    # check if there is near point in database
    #dict_keys(['35_897-139_945', '35_898-139_945'])
    database_data=data_ref.get()
    if database_data:
        epsilon=0.0003
        for loc in database_data.keys():
            if("-" not in loc):
                continue
            loc_lat=float(loc.split("-")[0].replace("_","."))
            loc_lng=float(loc.split("-")[1].replace("_","."))
            if(abs(loc_lat-cur_lat)**2 + abs(loc_lng-cur_lng)**2 < epsilon**2):
                cur_lat=loc_lat
                cur_lng=loc_lng
                epsilon=abs(loc_lat-cur_lat)**2 + abs(loc_lng-cur_lng)**2
    cur_position = f'{round(cur_lat,4):.4f}'.replace(".","_")+"-"+f'{round(cur_lng,4):.4f}'.replace(".","_")#"35_8975-139_9447"
    dt = datetime.datetime.now()
    cur_time = dt.isoformat(timespec="seconds").translate({ord(i): None for i in "-:"}) #"20210103T090210"
    cur_co2 = recieved_data[3]
    cur_temp = recieved_data[4]
    cur_humid = recieved_data[5]
    try:        
        data_ref.child(cur_position).update({
            cur_time: {
            "co2": cur_co2,
            "temperature": cur_temp,
            "humidity": cur_humid,
            }
        })
        print("send data:")
        print(cur_position,cur_time,cur_co2,cur_temp,cur_humid)
        #backup sent data
        send_data_dic={
            cur_position:{
                cur_time:{
                    "co2": cur_co2,
                    "temperature": cur_temp,
                    "humidity": cur_humid,
                }
            }
        }
        json.dump(send_data_dic, backup_file)
        backup_file.write('\n')
    except Exception as e:
        print(e)
        readSer.close()
        backup_file.close()
    #print(data_ref.get())

# caluculate average of minute.
def scheduled_job():
    global data_queue
    if(len(data_queue)>0):
        data_ave = np.average(np.array(data_queue), axis = 0)
        print("average:",data_ave)
        data_queue=[]
        send_to_firebase(data_ave)

schedule.every(1).minutes.do(scheduled_job)
#schedule.every(10).seconds.do(scheduled_job)

#常にbluetoothを受信する
try:
    while True:
        line = readSer.readline() # 1 line (upto '\n')
        data = list(map(lambda x:float(x),line.decode("UTF-8").strip().split(',')))
        data_queue.append(data)
        print(data)
        schedule.run_pending() #ジョブの実行
except Exception as e:
    print(e)
    readSer.close()
    backup_file.close()
