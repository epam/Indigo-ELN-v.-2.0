'use strict';

angular.module('indigoeln').factory('authService', authService);
authService.$inject = ['$http'];

function authService($http) {
    return {
        login: function (username, password) {
            return $http({
                method: 'POST',
                url: 'login',
                data: $.param({username: username, password: password}),
                headers: {'Content-Type': 'application/x-www-form-urlencoded'}
            });
        },
        logout: function() {
            //return $http.post('logout');
        },
        getAuth: function () {
            return $http.get('auth');
        },
        isAuthenticated:  function () {
            return $http.get('auth').then(function (response) {
                return response.data ? true : false;
            });
        }
    };

}