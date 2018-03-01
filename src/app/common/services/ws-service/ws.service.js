/*
 * Copyright (C) 2015-2018 EPAM Systems
 *
 * This file is part of Indigo ELN.
 *
 * Indigo ELN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Indigo ELN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

var SockJS = require('sockjs-client');
var Stomp = require('stompjs/lib/stomp').Stomp;

/* @ngInject */
function wsService($cookies, $http, $q, $log, apiUrl) {
    var stompClient = null;
    var connectionPromise = null;
    var url = getWbsocketUrl();

    return {
        disconnect: disconnect,
        subscribe: subscribe
    };

    function getWbsocketUrl() {
        if (!_.includes(apiUrl, location.host) && apiUrl !== 'api/') {
            return _.replace(apiUrl, 'api/', 'websocket');
        }

        return 'websocket';
    }

    function disconnect() {
        if (stompClient !== null) {
            stompClient.disconnect();
            stompClient = null;
            connectionPromise = null;
        }
    }

    function subscribe(destination) {
        return connect().then(
            function() {
                var listener = $q.defer();
                var subscriber = stompClient.subscribe(destination, function(data) {
                    listener.notify(angular.fromJson(data.body));
                });

                return {
                    unSubscribe: function() {
                        subscriber.unsubscribe();
                    },
                    onServerEvent: function(callback) {
                        listener.promise.then(null, null, callback);
                    }
                };
            },
            function() {
                return {
                    unSubscribe: function() {
                        $log.debug('Stubbed websockets mode');
                    },
                    onServerEvent: function() {
                        $log.debug('Stubbed websockets mode');
                    }
                };
            });
    }

    function connect() {
        if (!stompClient) {
            var connection = $q.defer();
            connectionPromise = connection.promise;

            // building absolute path so that websocket doesn't fail when deploying with a context path
            var socket = new SockJS(url);
            stompClient = Stomp.over(socket);
            var headers = {};
            headers['X-CSRF-TOKEN'] = $cookies.get($http.defaults.xsrfCookieName);

            stompClient.connect(headers, connection.resolve, connection.reject);
        }

        return connectionPromise;
    }
}

module.exports = wsService;
