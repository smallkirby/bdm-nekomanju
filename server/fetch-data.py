#!/usr/bin/env python
#encoding: utf-8;

###
# RealtimeDatabaseからJSON形式でデータを取得するサンプルスクリプト
###

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

print(data_ref.get())
