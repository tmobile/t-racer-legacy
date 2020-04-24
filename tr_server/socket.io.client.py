import socketio

sio = socketio.Client()

@sio.event
def connect():
    print('connection established')
    sio.emit('my custom event', {'response': 'my response'})
    print('msg emitted')

@sio.event
def my_message(data):
    print('my_message: message received with ', data)

@sio.event
def disconnect():
    print('disconnected from server')

sio.connect('http://localhost:5000')

sio.wait()

# message = {'ob': 'house',
#            'ids': ['54fjadb70f9756','39f1ax451f6567']}
# sio.emit("my_message", message)