var authoritiesData = require('./authorities.json');

/* @ngInject */
function authService($rootScope, $state, $q, principalService, authSessionService, wsService, $log, $timeout) {
    var prolongTimeout;

    return {
        login: login,
        prolong: prolong,
        logout: logout,
        authorize: authorize,
        getAuthorities: getAuthorities
    };

    function login(credentials) {
        return authSessionService.login(credentials).then(function(data) {
            // retrieve the logged account information
            return principalService.checkIdentity(true).then(function() {
                return data;
            });
        }).catch(function(err) {
            logout();

            return $q.reject(err);
        });
    }

    function prolong() {
        if (prolongTimeout) {
            clearTimeout(prolongTimeout);
        }
        prolongTimeout = $timeout(function() {
            authSessionService.prolong();
        }, 5000);
    }

    function logout() {
        authSessionService.logout();
        principalService.authenticate(null);
        // Reset state memory
        $rootScope.previousStateName = undefined;
        $rootScope.previousStateNameParams = undefined;
        try {
            wsService.disconnect();
        } catch (e) {
            $log.error('Error to disconnect');
        }
    }

    function authorize(force) {
        return principalService.checkIdentity(force)
            .then(function() {
                var isAuthenticated = principalService.isAuthenticated();

                // an authenticated user can't access to login and register pages
                if (isAuthenticated && checkState($rootScope.toState)) {
                    $state.go('experiment');
                }

                if (isAnyAuthority($rootScope.toState.data.authorities)) {
                    if (isAuthenticated) {
                        // user is signed in but not authorized for desired state
                        $state.go('accessdenied');
                    } else {
                        // user is not authenticated. stow the state they wanted before you
                        // send them to the signin state, so you can return them when you're done
                        $rootScope.previousStateName = $rootScope.toState;
                        $rootScope.previousStateNameParams = $rootScope.toStateParams;

                        // now, send them to the signin state so they can log in
                        $state.go('login');
                    }
                }

                function checkState(state) {
                    return state.parent === 'account' && (state.name === 'login' || state.name === 'register');
                }

                function isAnyAuthority(authorities) {
                    return authorities && authorities.length > 0 && !principalService.hasAnyAuthority(authorities);
                }
            });
    }

    function getAuthorities() {
        return authoritiesData;
    }
}

module.exports = authService;
