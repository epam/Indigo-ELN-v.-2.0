'use strict';

angular.module('indigoeln').factory('authService', authService);
authService.$inject = ['$http'];

function authService($http) {

    var API = {};

    API.getAuth = function () {
        return $http.get('auth');
    };

    API.login = function (username, password) {
        return $http({
            method: 'POST',
            url: 'login',
            data: $.param({username: username, password: password}),
            headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        });
    };

    return API;

}