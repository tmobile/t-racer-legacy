#!/usr/bin/env python2
# -*- coding: utf-8 -*-

# Author: David Manouchehri <manouchehri@protonmail.com>
# This script will always echo back data on the UDP port of your choice.
# Useful if you want nmap to report a UDP port as "open" instead of "open|filtered" on a standard scan.
# Works with both Python 2 & 3.

import socket
import time
#10.5.178.31
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

server_address = '0.0.0.0'
server_port = 31337

server = (server_address, server_port)
sock.bind(server)
print("Listening on " + server_address + ":" + str(server_port))
start_time = time.time()
counter = 0
WRITE_TO_FILE = False 
DEBUG = False
last_command = "s100xa1400x".encode()
start_time = time.time()

while True:
    payload, client_address = sock.recvfrom(100000)
    # print(F"payload {time.time()}") if DEBUG else None
    counter +=1

    # command payload are very short.  image payloads are much bigger
    # use this difference to figure out what to do.
    if len(payload) < 1000:
        current_command = bytearray(payload)
        print(F'got payload {payload}') if DEBUG else None
        if current_command == last_command:
            print(F"duplicate command: {current_command}")
        else:
            last_command = current_command;
            print(F"Updating last command {last_command}")
            sent = sock.sendto(last_command, client_address)
            sent = sock.sendto(last_command, client_address)
        print(F"{time.time()} diff time {(time.time() - start_time) * 1000}")

    else:

        if WRITE_TO_FILE:
            time_time = time.time()
            newFile = open(F"./images/camera_{time.time()}.yuv", "wb")
            newFileByteArray = bytearray(payload)
            newFile.write(newFileByteArray)
            newFile.close()

            control_file = open(F"./images/control_{time.time()}.txt", "w")
            control_str = last_command.decode("utf8")
            # print(F"Control String: {control_str}")
            control_file.write(control_str)
            control_file.close()

        print("Echoing data back to " + str(last_command)) if DEBUG else None
        #print("Echoing data back to " + str(last_command))
        # received an image.  now interpret the image to a command.
        # sent = sock.sendto(last_command, client_address)       
        sent = sock.sendto(last_command, client_address)
        sent = sock.sendto(last_command, client_address)
        sent = sock.sendto(last_command, client_address)

    if (time.time() - start_time) > 1 :
        print("FPS: ", counter / (time.time() - start_time))
        counter = 0
        start_time = time.time()
