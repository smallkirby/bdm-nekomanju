#!/usr/bin/env python
#encoding: utf-8;

import firebase_admin
from firebase_admin import db
from firebase_admin import credentials
import configparser

config = configparser.ConfigParser()
config.read('./config.ini')
# init app: no credential for now
cred = credentials.Certificate(config["nekomanju"]["certfile"])
app = firebase_admin.initialize_app(cred, {
  'databaseURL': config["nekomanju"]["url"],
})
data_ref = db.reference("data")

'''
# update entire node: DO NOT USE
data_ref.set({
  "20_3:19_2":{
    "20210103T213705":{
      "co2": 10.2
    },
    "20210103T110310":{
      "co2": 20.2
    },
    "20210103T090210":{
      "co2": 9.23
    },
  },
  "21_2:32_4":{
    "20210103T213705":{
      "co2": 10.2
    },
    "20210103T110310":{
      "co2": 20.2
    },
    "20210103T090210":{
      "co2": 9.23
    },
  }
 })
 '''

# update node. create new node if not exists.
cur_position = "20_3:19_3"
cur_time = "20210103T213705"
cur_co2 = 99.9
cur_temp = 10
cur_humidity = 20.32
data_ref.update({
  "20_3-19_2":{
    "20210103T213705":{
      "co2": 10.2,
      "temperature": 23.0, 
      "humidity": 19.1,
    },
    "20210103T110310":{
      "co2": 30.2,
      "temperature": 25.0, 
      "humidity": 20.1,
    },
    "20210103T090210":{
      "co2": 9.23,
      "temperature": 11.0, 
      "humidity": 19.0,
    },
  },
  "21_2-32_4":{
    "20210103T213705":{
      "co2": 10.2,
      "temperature": 23.0, 
      "humidity": 19.1,
    },
    "20210103T110310":{
      "co2": 20.2,
      "temperature": 23.0, 
      "humidity": 19.1,
    },
    "20210103T090210":{
      "co2": 9.23,
      "temperature": 23.0, 
      "humidity": 19.1,
    },
  },
)

print(data_ref.get())
