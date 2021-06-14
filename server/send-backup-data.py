#!/usr/bin/env python
#encoding: utf-8;

import firebase_admin
from firebase_admin import db
from firebase_admin import credentials
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

with open("backup.json") as backup_file:
    for line in backup_file:
        data=json.loads(line)
        #print(data)
        cur_position=list(data.keys())[0]
        cur_time=list(data[cur_position].keys())[0]
        cur_co2=data[cur_position][cur_time]["co2"]
        cur_temp=data[cur_position][cur_time]["temperature"]
        cur_humid=data[cur_position][cur_time]["humidity"]
        try:        
            data_ref.child(cur_position).update({
                cur_time: {
                "co2": cur_co2,
                "temperature": cur_temp,
                "humidity": cur_humid,
                }
            })
            print("send data:")
            print(data)
        except Exception as e:
            print(e)
