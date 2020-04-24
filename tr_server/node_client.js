var PORT = 31337;
var HOST = '192.168.1.137';

var dgram = require('dgram');

function msleep(n) {
    Atomics.wait(new Int32Array(new SharedArrayBuffer(4)), 0, 0, n);
}

function sleep(n) {
    msleep(n*1000);
}

  
function runme() {
    for (var i = 0; i < 10; i++ ){
        var message = new Buffer('My KungFu is Good!');

        var client = dgram.createSocket('udp4');
        client.send(message, 0, message.length, PORT, HOST, function(err, bytes) {
        if (err) throw err;
        console.log('UDP message sent to ' + HOST +':'+ PORT);
        client.close();
        });
        msleep(1000);
        console.log('why isn\'t this printing');
    }
}

runme();