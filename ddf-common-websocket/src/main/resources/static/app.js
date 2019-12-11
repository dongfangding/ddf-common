let ws = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    } else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
}

function connect() {
    let url = $("#url").val();
    // ws://localhost:8302/msg/pay-ws?ime=huawei-1aac7aa5-d1e9-43f6-a139-4e6fe1e6f15b&token=13542612549&loginType=ANDROID
    ws = new WebSocket(url);

    ws.onmessage = function(evt) {
        showGreeting(evt.data);
    };

    ws.onopen = function(evt) {
        setConnected(true);
        setInterval(function() {
            ws.send("PING")
        }, 30000);
    }

    ws.onclose = function (evt) {
        console.log('connection is closed');
        setConnected(false);
    }
}

function disconnect() {
    if (ws !== null) {
        ws.close();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendName() {
    if (ws.closing || ws.closed) {
        alert('connection is closed, can not send message to server!');
        return;
    }
    ws.send($("#name").val());
}

function showGreeting(message) {
    $("#greetings").append("<tr><td>" + message + "</td></tr>");
}

function batchConnect() {
    let imes = $("#batchConnect").val();
    if (imes == null || imes.indexOf("-") === -1) {
        alert('多个连接请使用-连接');
    }
    let imeArr = imes.split("-");
    let startIme = imeArr[0];
    let endIme = imeArr[1];

}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $("#connect").click(function () {
        connect();
    });
    $("#disconnect").click(function () {
        disconnect();
    });
    $("#send").click(function () {
        sendName();
    });
});