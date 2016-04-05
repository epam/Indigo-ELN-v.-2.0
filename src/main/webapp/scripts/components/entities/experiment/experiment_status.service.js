'use strict';

angular.module('indigoeln')
    .factory('ExperimentStatus', function ($window, $cookies, $http, $q) {
        var stompClient = null;
        var subscriber = null;
        var listener = $q.defer();
        var connected = $q.defer();

        return {
            connect: function () {
                //building absolute path so that websocket doesn't fail when deploying with a context path
                var loc = $window.location;
                var url = '//' + loc.host + loc.pathname + 'websocket';
                var socket = new SockJS(url);
                stompClient = Stomp.over(socket);
                var headers = {};
                headers['X-CSRF-TOKEN'] = $cookies.get($http.defaults.xsrfCookieName);
                stompClient.connect(headers, function () {
                    connected.resolve('success');
                });
            },
            subscribe: function () {
                connected.promise.then(function () {
                    subscriber = stompClient.subscribe('/topic/experiment_status', function (data) {
                        listener.notify(JSON.parse(data.body));
                    });
                }, null, null);
            },
            unsubscribe: function () {
                if (subscriber !== null) {
                    subscriber.unsubscribe();
                }
            },
            receive: function () {
                return listener.promise;
            },
            disconnect: function () {
                if (stompClient !== null) {
                    stompClient.disconnect();
                    stompClient = null;
                }
            }
        };
    });
