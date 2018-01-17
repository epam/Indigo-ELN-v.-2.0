/* @ngInject */
function authSessionService($http, apiUrl) {
    return {
        login: login,
        logout: logout,
        prolong: prolong
    };

    function login(credentials) {
        var data = 'j_username=' + encodeURIComponent(credentials.username) +
            '&j_password=' + encodeURIComponent(credentials.password) +
            '&remember-me=' + credentials.rememberMe + '&submit=Login';

        return $http.post(apiUrl + 'authentication', data, {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        }).then(function(response) {
            return response;
        });
    }

    function logout() {
        // logout from the server
        return $http.post(apiUrl + 'logout');
    }

    function prolong() {
        // ping server to prolong session
        return $http.get(apiUrl + 'accounts/prolong');
    }
}

module.exports = authSessionService;