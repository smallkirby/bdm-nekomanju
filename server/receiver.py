#! /usr/bin/env python
# coding:utf-8

import socket
import threading
import re
import csv

csv_file_name = "data"

def check_http(request):
  if "HTML" in request:
    return True
  return False

# check format of received message.
# iff format is valid, save data in format of CSV and return True
def check_format_and_save(msg):
  # ここになにか処理を書く

  # write to csv via append mode
  with open(csv_file_name, "a") as f:
    pass # 個々になにか処理を書く

  return True


bind_ip = 'xx.xx.xx.xx'
bind_port = 49498

server = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
server.bind((bind_ip,bind_port))
server.listen(5)
print("[*] listen {}:{}".format(bind_ip,str(bind_port)))

def handle_client(client_socket):
  bufsize=1024
  request = client_socket.recv(bufsize).decode("utf-8")
  print("[*] recv:")
  print(request)
  if(check_format_and_save(request)):
    print("[O] sending OK")
    client_socket.send(b"OK\r\n")
  else:
    if check_http(request):
      global csv_file_name
      data_size = 0
      #with open(csv_file_name, "r") as f:
      #  data_size = len(f.readlines())
      message = b"HTTP/1.1 200 OK\r\n"
      message += b"Connection: Keep-Alive\r\n"
      message += b"Content-Type: text/html; charset=utf-8\r\n"
      message += b"\r\n"
      message += b"Hello.\r\n"
      message += bytes("We have now {} lines of data...".format(hex(data_size)), "utf-8")
      client_socket.send(message)
    else:
      print("[!] sending ERR")
      client_socket.send(b"ERROR\r\n")


while True:
  client,addr = server.accept()
  print("[*] connected from: {}:{}".format(addr[0],str(addr[1])))
  client_handler = threading.Thread(target=handle_client,args=(client,))
  client_handler.start()

