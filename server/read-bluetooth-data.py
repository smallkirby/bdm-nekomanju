import serial
import time
import schedule
import numpy as np

readSer = serial.Serial('/dev/tty.RNBT-B032-RNI-SPP',115200, timeout=100)
data_queue = []
def scheduled_job():
    #1分経つごとに平均を送信する
    global data_queue
    if(len(data_queue)>0):
        data_ave = np.average(np.array(data_queue), axis = 0)
        print("average:",data_ave)
        data_queue=[]

schedule.every(1).minutes.do(scheduled_job)

#常にbluetoothを受信する
while True:
    line = readSer.readline() # 1 line (upto '\n')
    data = list(map(lambda x:float(x),line.decode("UTF-8").strip().split(',')))
    data_queue.append(data)
    print(data)
    schedule.run_pending() #ジョブの実行
    
readSer.close()