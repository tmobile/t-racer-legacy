import eventlet
import socketio

sio = socketio.Server()
app = socketio.WSGIApp(sio, static_files={
    '/': {'content_type': 'text/html', 'filename': 'index.html'}
})

@sio.event
def connect(sid, environ):
    print('connect ', sid)
    message = {'ob': 'house',
        'ids': ['54fjadb70f9756','39f1ax451f6567']}
    sio.emit('my_message', message)

@sio.event
def my_message(sid, data):
    print('message ', data)

@sio.event
def disconnect(sid):
    print('disconnect ', sid)

@sio.on('my custom event')
def my_custome_event(sid, data):
    print('my custom event', sid, data)
    sio.emit('my_message', {'test':'value'})

if __name__ == '__main__':
    eventlet.wsgi.server(eventlet.listen(('', 5000)), app)