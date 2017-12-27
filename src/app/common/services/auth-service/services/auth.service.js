/* @ngInject */
function authService($rootScope, $state, $q, principalService, authSessionService, wsService, $log, $timeout,
                     notifyService, i18en) {
    var prolongTimeout;

    wsService
        .subscribe('user_permissions_changed')
        .then(function(subscribe) {
            subscribe.onServerEvent(function(message) {
                notifyService.info(message);
                logout().then(function() {
                    notifyService.info(i18en.USER_PERMISSIONS_WERE_CHANGE);
                });
            });
        });

    return {
        login: login,
        prolong: prolong,
        logout: logout,
        authorize: authorize
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
        authSessionService.logout().then(
            function() {
                principalService.authenticate(null);
                // Reset state memory
                $rootScope.previousStateName = undefined;
                $rootScope.previousStateNameParams = undefined;
                try {
                    wsService.disconnect();
                } catch (e) {
                    $log.error('Error to disconnect');
                }
                $state.go('login');
            },
            notifyService.error);
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
}

module.exports = authService;
