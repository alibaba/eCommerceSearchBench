#!/bin/python
# -*- coding: utf-8 -*-
'''
Copyright
Author:     liuxiaoli (sally.lxl@alibaba.com)
Description:
    perform monitor tool for disk, net,cpu,mem and so on
History:
    2019/1/18 @liuxiaoli:   integration tools for monitoring disk IO and net IO by process (iotop, nethogs)
'''

import sys
import getopt
import argparse
import os
import re
import time
#import numpy as np
#import pandas as pd
import datetime
import socket
import subprocess
from collections import OrderedDict
import argparse
from multiprocessing.dummy import Pool as ThreadPool

hostname=socket.gethostname()
ip=socket.gethostbyname(hostname)
hostname=ip
host_ip=ip.split('.')[-1]
basepath = os.path.dirname(os.path.abspath(sys.argv[0]))
start_timestamp=0
metricsFormat=OrderedDict(
    [("timeStamp",0),
     ("ifaceName",1),
     ("bytesOutPerS",2),
     ("bytesInPerS",3),
     ("bytesTotalPerS",4),
     ("bytesIn",5),
     ("bytesOut",6),
     ("packetsOutPerS",7),
     ("packetsInPerS",8),
     ("packetsTotalPerS",9),
     ("packetsIn",10),
     ("packetsOut",11),
     ("errorsOutPerS",12),
     ("errorsInPerS",13),
     ("errorsIn",14),
     ("errorsOut",15)]
    )

topFileFormat={
    "pid":0,
    "user":1,
    "pr":2,
    "ni":3,
    "virtualMem":4,
    "residentMem":5,
    "shareMem":6,
    "state":7,
    "cpuUtil":8,
    "memUtil":9,
    "runTime":10,
    "command":11
}

iotopFileFormat={
        "read_data":3,
        "write_data":9
        }

nethogsFileFormat={
        "processname":1,
        "pid":2,
        "sent":4,
        "received":5
        }

def parser_add_argument():
    usage="%(prog)s [-h] monitor_tool{bwm-ng,iotop,nethogs,top,perf} [-h] [-t {disk,net,process,thread}] [-d | -p] [-i] [-n] [-c] "
    parser = argparse.ArgumentParser(usage=usage,description="Monitor for Disk/Network I/O,CPU and Memory")
    parser.add_argument('monitor_name', choices=['bwm-ng','iotop','nethogs','top', 'perf'],help="choose monitor tool") 

    parser.add_argument(
        "-t","--type",
        choices = ['disk','net','process','thread','perf'],
        dest="mon_type",
        help="set the type to monitor"
    )

   # group = parser.add_mutually_exclusive_group(required=True)
    parser.add_argument(
        "-d","--device",
        dest="io_device",
        default="sda",
        help="set the IO device to monitor"
    )

    parser.add_argument(
        "-p","--processname",
        default="mychain",
        help="set the process name to monitor"
    )
            
    parser.add_argument(
        '-i',"--interval",
        action="store", #指示当解析到一个命令行参数时该如何处理
        dest="interval",    #存储的变量
        type=int,     #变量类型,默认string
        default = 1,
        help="set the time interval of monitor refresh, the unit is second"
    )

    parser.add_argument(
        "-n","--number",
        action="store", #指示当解析到一个命令行参数时该如何处理
        dest="number",    #存储的变量
        type=int,     #变量类型,默认string
        default = 0,
        help="set the number to monitor, 0 represents there is no limit"
    )

    parser.add_argument(
        "-c","--casename",
        action="store", #指示当解析到一个命令行参数时该如何处理
        dest="casename",    #存储的变量
        default="case1",
        help="set the casename in which monitor runs"
    )
    return parser

def get_timestamp():
    """获取19位时间戳，单位ps"""
    ct = time.time()
    timeStamp=ct*1000000000
    return np.int(timeStamp)

def write_influxdb_txt(metrics,value,timestamp):
    global start_timestamp
    if start_timestamp==0:
        start_timestamp=timestamp
    delta_timestamp=timestamp-start_timestamp
    timestamp=init_timestamp+delta_timestamp

#    with open(outputFile_txt,'a') as filep:
#        filep.write(metrics+",host="+hostname+",case="+casename+" value="+str(value)+" "+str(timestamp)+"\n")
#    return

def dict2csv(RecordDict,refColumn,outputFile_csv):
    if os.path.isfile(outputFile_csv):
        os.remove(outputFile_csv)
    df=pd.DataFrame()
    df=pd.DataFrame(RecordDict[refColumn])
    df.columns=['ref']
    for key in RecordDict:
        df[key]=pd.DataFrame(RecordDict[key])
    df=df.drop(columns=['ref'])
    df.to_csv(outputFile_csv)

def GetIOstat_bwm(mon_type_i):
    ret=subprocess.check_output(["bwm-ng", "-i", mon_type_i, "-o", "csv", "-c", "1"])
    ret_line=ret.split()
    for entry in ret_line:
        if io_device in entry:
            entry_split=re.split(r";",entry)
            txbw_KBps=float(entry_split[metricsFormat["bytesOutPerS"]])/1000
            rxbw_KBps=float(entry_split[metricsFormat["bytesInPerS"]])/1000
            #write_influxdb_txt(mon_type+"_txbw_KBps",txbw_KBps,get_timestamp())
            #write_influxdb_txt(mon_type+"_rxbw_KBps",rxbw_KBps,get_timestamp())
            #entry.replace(';',',')
            #with open(outputFile_csv,'a') as filep:
            #    filep.write(entry)
            #break
            if mon_type+"_txbw_KBps" not in RecordDict:
                RecordDict[mon_type+"_txbw_KBps"]=[]
            RecordDict[mon_type+"_txbw_KBps"].append(txbw_KBps)

            if mon_type+"_rxbw_KBps" not in RecordDict:
                RecordDict[mon_type+"_rxbw_KBps"]=[]
            RecordDict[mon_type+"_rxbw_KBps"].append(rxbw_KBps)

def IOMonitor_bwm(mon_type_i):
    #print("In IOMonitor_bwm which mon_type="+mon_type+" at host="+hostname)
    #outputfile=outputdir+'/'+casename+'_'+hostname[6:9]+'_'+io_device+"_"+mon_type+".csv"
    outputfile=outputdir+'/'+casename+'_'+host_ip+'_'+io_device+"_"+mon_type+".csv"
    if number==0:
        while 1:
            GetIOstat_bwm(mon_type_i)
            dict2csv(RecordDict,mon_type+"_rxbw_KBps",outputfile)
            time.sleep(interval)
    else:
    	count=0
    	while count<number: 
            GetIOstat_bwm(mon_type_i)
            dict2csv(RecordDict,mon_type+"_rxbw_KBps",outputfile)
            time.sleep(interval)
            count+=1 
        #dict2csv(RecordDict,mon_type+"_rxbw_KBps",outputfile)
    #print("Finished monitor and saved to "+outputfile)

def SaveDiskIOstat_iotop(pid,outputfile):
    RecordDict[pid]={}
    logfile=abspath+"/data/iotop_"+str(pid)+".log"
    with open(logfile) as filep:
        while 1:
            line=filep.readline()
            if not line:
                break;
            if "Actual DISK WRITE" not in line:
                continue
            line=line.strip()
            lineList=line.split()
            key=lineList[nethogsFileFormat["processname"]]+lineList[nethogsFileFormat["pid"]]
            rdbw_Kbps=lineList[iotopFileFormat["read_data"]]
            wrbw_Kbps=lineList[iotopFileFormat["write_data"]]
            #print "wrbw_Kbps=", wrbw_Kbps, "rdbw_Kbps=",rdbw_Kbps 
            if "disk_rdbw_KBps" not in RecordDict[pid]:
                RecordDict[pid]["disk_rdbw_KBps"]=[]
            RecordDict[pid]["disk_rdbw_KBps"].append(rdbw_Kbps)

            if "disk_wrbw_KBps" not in RecordDict[pid]:
                RecordDict[pid]["disk_wrbw_KBps"]=[]
            RecordDict[pid]["disk_wrbw_KBps"].append(wrbw_Kbps)

    dict2csv(RecordDict[pid],"disk_wrbw_KBps",outputfile)
    #print("Finished monitor and saved to "+outputfile)

def GetDiskIOstat_iotop(pid):
    #print("In iotop Monitor which pid="+pid+" at host="+hostname)
    ret=subprocess.call(["sh",iotop_sh_dir,str(pid),str(number),str(interval)])
    if ret != 0:
        raise Exception("GetDiskIOstat_iotop FAILD for process="+pid)
    #outputfile=outputdir+'/'+casename+'_'+hostname[6:9]+'_'+processname+str(pid)+"_disk.csv"
    outputfile=outputdir+'/'+casename+'_'+host_ip+'_'+processname+str(pid)+"_disk.csv"
    #print "GetDiskIOstat_iotop finished"
    SaveDiskIOstat_iotop(pid,outputfile)


def SaveNetIOstat_nethogs(logfile):
    RecordDicts={}
    with open(logfile) as filep:
        while 1:
            line=filep.readline()
            if not line:
                break;
            if processname not in line:
                continue
            line=line.strip()
            lineList=re.split('/| |\t',line)
            keyname=lineList[nethogsFileFormat["processname"]]+lineList[nethogsFileFormat["pid"]]
            txbw_KBps=lineList[nethogsFileFormat["sent"]]
            rxbw_KBps=lineList[nethogsFileFormat["received"]]

            if keyname not in RecordDicts:
                RecordDicts[keyname]={}
            if "net_txbw_KBps" not in RecordDicts[keyname]:
                RecordDicts[keyname]["net_txbw_KBps"]=[]
            RecordDicts[keyname]["net_txbw_KBps"].append(txbw_KBps)
            if "net_rxbw_KBps" not in RecordDicts[keyname]:
                RecordDicts[keyname]["net_rxbw_KBps"]=[]
            RecordDicts[keyname]["net_rxbw_KBps"].append(rxbw_KBps)
            
    for key in RecordDicts:
        #outputfile=outputdir+'/'+casename+'_'+hostname[6:9]+'_'+key+"_net.csv"
        outputfile=outputdir+'/'+casename+'_'+host_ip+'_'+key+"_net.csv"
        dict2csv(RecordDicts[key],"net_rxbw_KBps",outputfile)
        #print("Finished monitor and saved to "+outputfile)


def GetNetIOstat_nethogs():
    #print("In NetHogs Monitor"+" at host="+hostname)
    ret=subprocess.call(["sh",nethogs_sh_dir,str(number),str(interval)])
    if ret != 0:
        raise Exception("GetNetIOstat_iotop FAILD at hostname="+hostname)
    logfile=abspath+"/data/nethogs.log"
    SaveNetIOstat_nethogs(logfile)


def GetTopstat(pid):
    if mon_type == "process":
        ret_lines=subprocess.check_output(["top", "-n", "1", "-p", pid, "-b"])
        top_type="process"
    else:
        ret_lines=subprocess.check_output(["top", "-H", "-n", "1", "-p", pid, "-b"])
        top_type="thread"

    ret_lines_r=ret_lines.split("\n")
    start_flag=0
    memory_write_flag=0
    for index in range(len(ret_lines_r)):
        if "PID" in ret_lines_r[index]:
            start_flag=1
            continue

        if start_flag==0:
            continue
        elif ret_lines_r[index]:
            ret_line_r=ret_lines_r[index].split()
            (pid_in_line,command,cpuUtil,residentMem)=ret_line_r[topFileFormat["pid"]],ret_line_r[topFileFormat["command"]],ret_line_r[topFileFormat["cpuUtil"]],ret_line_r[topFileFormat["residentMem"]]
            if 'm' in residentMem or 'M' in residentMem:
                residentMem=float(residentMem[:-1])*1000
            elif 'g' in residentMem or 'G' in residentMem:
                residentMem=float(residentMem[:-1])*1000000
            else:
                residentMem=residentMem

            key=processname+"_"+pid_in_line+'_'+str(residentMem)
            if memory_write_flag==0:
                """相同进程中不同线程的residentMem只记录一次"""
                memory_write_flag=1 
                #write_influxdb_txt("residentMem,proc_pid="+processname+"_"+pid,residentMem,get_timestamp())
                if "residentMem" not in RecordDict[pid]:
                    RecordDict[pid]["residentMem"]=[]
                RecordDict[pid]["residentMem"].append(residentMem)

            if mon_type == "process":
                key=processname+"_"+str(pid)
                key="mychain_CpuUlti" #processname+"_"+str(pid)
            else:
                key=command+"_"+pid_in_line

            #write_influxdb_txt("cpuUtil,proc_pid="+key,cpuUtil,get_timestamp())
            if key not in RecordDict[pid]:
                RecordDict[pid][key]=[]
            RecordDict[pid][key].append(float(cpuUtil))
    return

def TopMonitor(pid):
    #print("In TopMonitor which pid="+pid+" at host="+hostname)
    #outputfile=outputdir+'/'+casename+'_'+hostname[6:9]+'_'+processname+str(pid)+"_sysperf_"+mon_type+".csv"
    outputfile=outputdir+'/'+casename+'_'+host_ip+'_'+processname+str(pid)+"_sysperf_"+mon_type+".csv"
    RecordDict[pid]={}
    if number==0:
        while 1:
            GetTopstat(pid)
            #dict2csv(RecordDict[pid],'residentMem',outputfile)
            time.sleep(interval)
    else:
    	count=0
    	while count<number: 
            GetTopstat(pid)
            #dict2csv(RecordDict[pid],'residentMem',outputfile)
            time.sleep(interval)
    	    count+=1 
        #dict2csv(RecordDict[pid],'residentMem',outputfile)
    #print("Finished monitor and saved to "+outputfile)
    print("Finished monitor pid=%s,and mean_cpu=%f,mean_mem=%f" %(pid,sum(RecordDict[pid]["mychain_CpuUlti"])/len(RecordDict[pid]["mychain_CpuUlti"]),sum(RecordDict[pid]["residentMem"])/len(RecordDict[pid]["residentMem"])))

def PerfMonitor(pid):
    #print("In PerfMonitor which pid="+pid+" at host="+hostname)
    count=0
    if number==0:
        while 1:
            #outputfile_perf=outputdir+'/'+casename+'_'+hostname[6:9]+'_'+processname+str(pid)+"_"+mon_type+"_perf.data"+str(count)
            outputfile_perf=outputdir+'/'+casename+'_'+host_ip+'_'+processname+str(pid)+"_"+mon_type+"_perf.data"+str(count)
            subprocess.call(["sudo","perf","record","-o",outputfile_perf,"-F","99","--call-graph","dwarf","-p",str(pid),"-g","--","sleep",str(interval)])
    	    count+=1 
    else:
    	while count<number: 
            #outputfile_perf=outputdir+'/'+casename+'_'+hostname[6:9]+'_'+processname+str(pid)+"_"+mon_type+"_perf.data"+str(count)
            outputfile_perf=outputdir+'/'+casename+'_'+host_ip+'_'+processname+str(pid)+"_"+mon_type+"_perf.data"+str(count)
            subprocess.call(["sudo","perf","record","-o",outputfile_perf,"-F","99","--call-graph","dwarf","-p",str(pid),"-g","--","sleep",str(interval)])
    	    count+=1 

if( __name__=="__main__" ):
    parser = parser_add_argument()
    args = parser.parse_args()
    monitor_name=args.monitor_name
    mon_type=args.mon_type
    io_device=args.io_device
    processname=args.processname
    interval=args.interval
    number=args.number
    casename=args.casename
    abspath=os.path.dirname(os.path.abspath(__file__))
    #print (abspath)
    iotop_sh_dir=abspath+"/iotop.sh"
    nethogs_sh_dir=abspath+"/nethogs.sh"
    #print(monitor_name, mon_type, io_device, processname, number, casename)
    init_timestamp=1546272000000000000
    start_timestamp=0
    RecordDict={}

    outputdir=abspath+"/data/"+casename
    #if os.path.isdir(outputdir):
    #    subprocess.call(["sudo" ,"rm", "-rf",outputdir])
    subprocess.call(["mkdir", "-p",outputdir])

    #outputFile_txt=outputdir+'/'+hostname+"-"+mon_type+".txt"
    #if os.path.isfile(outputFile_txt):
    #    os.remove(outputFile_txt)


    if mon_type == "disk":
        if monitor_name == "bwm-ng":
            IOMonitor_bwm("disk")
        elif monitor_name == "iotop":
            if not subprocess.call(["pgrep",processname]):
                pids=subprocess.check_output(["pgrep",processname])
            else:
                raise Exception("can't find " + processname +"_process at host=!!!"+hostname)
            pool = ThreadPool()
            pool.map(GetDiskIOstat_iotop,pids.split())
            pool.close()
            pool.join()
    elif mon_type == "net":
        if monitor_name == "bwm-ng":
            IOMonitor_bwm("proc")
        elif monitor_name == "nethogs":
            GetNetIOstat_nethogs()
    elif mon_type == "thread" or mon_type == "process":
        if not subprocess.call(["pgrep",processname]):
            pids=subprocess.check_output(["pgrep",processname])
        else:
            raise Exception("can't find " + processname +"_process at host=!!!"+hostname)
        pool = ThreadPool()
        pool.map(TopMonitor,pids.split())
        pool.close()
        pool.join()
    elif mon_type == "perf":
        if not subprocess.call(["pgrep",processname]):
            pids=subprocess.check_output(["pgrep",processname])
        else:
            raise Exception("can't find " + processname +"_process at host=!!!"+hostname)
        pool = ThreadPool()
        pool.map(PerfMonitor,pids.split())
        pool.close()
        pool.join()
    else:
        raise Exception("Unsupported Monitor type!!!")
    

