angular
    .module('indigoeln')
    .factory('wsService', wsService);

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

        return location.host + '/websocket';
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
                var subscriber = stompClient.subscribe('/topic/' + destination, function(data) {
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
