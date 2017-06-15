angular
    .module('indigoeln')
    .factory('AuthServerProvider', AuthServerProvider);

/* @ngInject */
function AuthServerProvider($http) {

    return {
        login: login,
        logout: logout,
        prolong: prolong
    };

    function login(credentials) {
        var data = 'j_username=' + encodeURIComponent(credentials.username) +
            '&j_password=' + encodeURIComponent(credentials.password) +
            '&remember-me=' + credentials.rememberMe + '&submit=Login';
        return $http.post('api/authentication', data, {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        }).success(function (response) {
            return response;
        });
    }

    function logout() {
        // logout from the server
        $http.post('api/logout').success(function (response) {
            return response;
        });
    }

    function prolong() {
        // ping server to prolong session
        $http.get('api/accounts/prolong');
    }
}