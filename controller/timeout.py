import json
import socket
import traceback
import time

print('Valid inputs for target - node1, node2, node3, node4, node5')
valid_inputs = ['node1', 'node2', 'node3', 'node4', 'node5']
wrong_input = True
while(wrong_input):
    target = input("Enter target: ")
    if target not in valid_inputs:
        print('Invalid input, please try again')
    else:
        wrong_input = False

# Read Message Template
msg = json.load(open("Message.json"))

# Initialize
sender = "controller"
port = 8085
nodelist = ['node1', 'node2', 'node3', 'node4', 'node5']

# Request
msg['sender_name'] = sender
msg['request'] = "TIMEOUT"
print(f"Request Created : {msg}")

# Socket Creation and Binding
skt = socket.socket(family=socket.AF_INET, type=socket.SOCK_DGRAM)
skt.bind((sender, port))

# Send Message
try:
    # Encoding and sending the message
    print("Sent TIMEOUT request to ", target)
    skt.sendto(json.dumps(msg).encode('utf-8'), (target, port))
except:
    #  socket.gaierror: [Errno -3] would be thrown if target IP container does not exist or exits, write your listener
    print(f"ERROR WHILE SENDING REQUEST ACROSS : {traceback.format_exc()}")
