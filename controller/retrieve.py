from asyncore import socket_map
from logging.config import valid_ident
from os import system
from threading import Thread
import json
import socket
import traceback
import time

print('Valid inputs for target - node1, node2, node3, node4, node5')
valid_inputs = ['node1', 'node2', 'node3', 'node4', 'node5']
wrong_input = True
while(wrong_input):
    target = input("Enter target node: ")
    if target not in valid_inputs:
        print('Invalid input, please try again')
    else:
        wrong_input = False

# Read Message Template
msg = json.load(open("Message.json"))

# Initialize
sender = "controller"
port_send = 8086
port_rec = 8085
target_port = 8085
nodelist = ['node1', 'node2', 'node3', 'node4', 'node5']
buffer = 1024

# Request
msg['sender_name'] = sender
msg['request'] = "RETRIEVE"
print(f"Request Created : {msg}")

# Socket Creation and Binding
skt_sender = socket.socket(family=socket.AF_INET, type=socket.SOCK_DGRAM)
skt_sender.bind((sender, port_send))
skt_rec = socket.socket(family=socket.AF_INET, type=socket.SOCK_DGRAM)
skt_rec.bind((sender, port_rec))

# Listener


def listener():
    bytemessage = skt_rec.recvfrom(buffer)
    message = json.loads(bytemessage[0])
    print('Message Received: ', message)
    print('Log: ', message['value'])


# Send Message
try:
    # Start listener in new thread
    listener_thread = Thread(target=listener)
    listener_thread.start()
    # Encoding and sending the message
    print("Sent RETRIEVE request to ", target)
    skt_sender.sendto(json.dumps(msg).encode('utf-8'), (target, target_port))
except:
    #  socket.gaierror: [Errno -3] would be thrown if target IP container does not exist or exits, write your listener
    print(f"ERROR WHILE SENDING REQUEST ACROSS : {traceback.format_exc()}")
