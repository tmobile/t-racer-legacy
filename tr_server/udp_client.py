import socket
import sys
import time
import math

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
server_address = ('192.168.1.167', 4445)
# server_address = ('192.168. .137', 31337)

for x in range(10):
    message = ('s' + str(x * 10 + 60) + 'x\n') * 100
    # Send data
    print(F'sending {math.trunc(time.time() * 1000)} {message} ')
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sent = sock.sendto(message.encode(), server_address)
    sock.close()
    time.sleep(1)
    # # Receive response
    # print('waiting to receive')
    # data, server = sock.recvfrom(4096)
    # print(F'received {data.decode("utf-8")}')
    # print('closing socket') 


