import socket
import sys
import math
import time

# '192.168.1.167', 4445

# Create a TCP/IP socket
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# Connect the socket to the port where the server is listening
server_address = ('192.168.1.167', 4445)
sock.connect(server_address)


for x in range(10):
    # Create a UDP socket
    message = ('s' + str(x * 10 + 60) + 'x\n' ) * 100
  # Connect the socket to the port where the server is listening
    print(F'sending {math.trunc(time.time() * 1000)} {message} ')
    sock.sendall(message.encode())
    time.sleep(3)

sock.close()