angular
    .module('indigoeln')
    .factory('Auth', Auth);

/* @ngInject */
function Auth($rootScope, $state, $q, Principal, AuthServerProvider, WSService) {
    var prolongTimeout;

    return {
        login: login,
        prolong: prolong,
        logout: logout,
        authorize: authorize
    };


    function login(credentials, callback) {
        var cb = callback || angular.noop;
        var deferred = $q.defer();

        AuthServerProvider.login(credentials).then(function (data) {
            // retrieve the logged account information
            Principal.identity(true).then(function () {
                deferred.resolve(data);
            });
            return cb();
        }).catch(function (err) {
            this.logout();
            deferred.reject(err);
            return cb(err);
        }.bind(this));

        return deferred.promise;
    }

    function prolong() {
        if (prolongTimeout) {
            clearTimeout(prolongTimeout);
        }
        prolongTimeout = setTimeout(function () {
            AuthServerProvider.prolong()
        }, 5000);
    }

    function logout() {
        AuthServerProvider.logout();
        Principal.authenticate(null);
        // Reset state memory
        $rootScope.previousStateName = undefined;
        $rootScope.previousStateNameParams = undefined;
        try {
            WSService.disconnect();
        } catch (e) {
        }
    }

    function authorize(force) {
        return Principal.identity(force)
            .then(function () {
                var isAuthenticated = Principal.isAuthenticated();

                // an authenticated user can't access to login and register pages
                if (isAuthenticated && $rootScope.toState.parent === 'account' && ($rootScope.toState.name === 'login' || $rootScope.toState.name === 'register')) {
                    $state.go('experiment');
                }

                if ($rootScope.toState.data.authorities && $rootScope.toState.data.authorities.length > 0 && !Principal.hasAnyAuthority($rootScope.toState.data.authorities)) {
                    if (isAuthenticated) {
                        // user is signed in but not authorized for desired state
                        $state.go('accessdenied');
                    }
                    else {
                        // user is not authenticated. stow the state they wanted before you
                        // send them to the signin state, so you can return them when you're done
                        $rootScope.previousStateName = $rootScope.toState;
                        $rootScope.previousStateNameParams = $rootScope.toStateParams;

                        // now, send them to the signin state so they can log in
                        $state.go('login');
                    }
                }
            });
    }
}
