'use strict';

angular.module('indigoeln').factory('websocketService', websocketService);
websocketService.$inject = ['$q'];

function websocketService($q) {

    var url = 'ws://' + location.host + location.pathname + 'websocket';
    var API = {};
    var client;

    API.connect = function () {
        var deferred = $q.defer();
        client = Stomp.client(url);

        client.connect({}, function () {
            deferred.resolve();
        });
        return deferred.promise;
    };

    API.disconnect = function () {
        client.disconnect();
    };

    API.sendText = function (destination, text) {
        client.send(destination, {
            'content-length': false,
            'content-type': 'text/plain;charset=utf-8'
        }, text);
    };

    API.sendJSON = function (destination, data) {
        var jsonData = angular.toJson(data);
        client.send(destination, {
            'content-length': false,
            'content-type': 'application/json;charset=utf-8'
        }, jsonData);
    };

    return API;
}