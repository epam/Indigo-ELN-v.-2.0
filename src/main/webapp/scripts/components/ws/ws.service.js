angular.module('indigoeln')
    .factory('WSService', function ($window, $cookies, $http, $q, $log) {
        var stompClient = null;
        var connected = $q.defer();

        var connect = function () {
            if (!stompClient) {
                //building absolute path so that websocket doesn't fail when deploying with a context path
                var loc = $window.location;
                var url = '//' + loc.host + loc.pathname + 'websocket';
                var socket = new SockJS(url);
                stompClient = Stomp.over(socket);
                var headers = {};
                headers['X-CSRF-TOKEN'] = $cookies.get($http.defaults.xsrfCookieName);
                stompClient.connect(headers, function () {
                    connected.resolve('success');
                }, function () {
                    connected.resolve('stub');
                });
            }
        };

        var subscribe = function (destination) {
            var defer = $q.defer();
            connect();
            connected.promise.then(function (mode) {
                if(mode === 'success') {
                    var listener = $q.defer();
                    var subscriber = stompClient.subscribe('/topic/' + destination, function (data) {
                        listener.notify(JSON.parse(data.body));
                    });
                    listener.unSubscribe = function () {
                        subscriber.unsubscribe();
                    };
                    listener.onServerEvent = function (callback) {
                        listener.promise.then(null, null, callback);
                    };
                    defer.resolve(listener);
                }else {
                    defer.resolve({
                        unSubscribe: function () {
                            $log.debug('Stubbed websockets mode');
                        },
                        onServerEvent: function () {
                            $log.debug('Stubbed websockets mode');
                        }
                    });
                }
            }, null, null);

            return defer.promise;
        };

        var disconnect = function () {
            if (stompClient !== null) {
                stompClient.disconnect();
                stompClient = null;
                connected = $q.defer();
            }
        };

        return {
            disconnect: function () {
                if (stompClient) {
                    disconnect();
                }
            },

            subscribe: function (topic) {
                return subscribe(topic);
            }
        };
    });
