var template = require('./common/components/timer/timer-dialog.html');
var $ = require('./dependencies/jquery');

/* @ngInject */
function appRun($rootScope, $window, $state, $uibModal, editableOptions, authService, principalService, Idle,
                entitiesBrowser, $http, $cookies) {
    updateCSRFTOKEN($cookies, $http);

    $.mCustomScrollbar.defaults.advanced.autoScrollOnFocus = false;
    // idleTime: 30 minutes, countdown: 30 seconds
    var countdownDialog = null;
    var idleTime = 30;
    var countdown = 30;

    $rootScope.$on('$stateChangeStart', function(event, toState, toStateParams) {
        $rootScope.toState = toState;
        $rootScope.toStateParams = toStateParams;

        if (principalService.isIdentityResolved()) {
            authService.authorize().then(function() {
                updateCSRFTOKEN($cookies, $http);
            });
        }
        var tab = angular.copy(toState.data.tab);

        if (tab) {
            tab.params = toStateParams;
            if (tab.type && tab.type === 'entity') {
                entitiesBrowser.addTab(tab);
            }
        }
    });

    $rootScope.$on('$stateChangeSuccess', function(event, toState, toParams, fromState, fromParams) {
        var titleKey = 'indigoeln';

        // Remember previous state unless we've been redirected to login or we've just
        // reset the state memory after logout. If we're redirected to login, our
        // previousState is already set in the authExpiredInterceptor. If we're going
        // to login directly, we don't want to be sent to some previous state anyway
        if (toState.name !== 'login') {
            $rootScope.previousStateName = fromState.name;
            $rootScope.previousStateParams = fromParams;
            Idle.watch();
        }

        // Set the page title key to the one configured in state or use default one
        if (toState.data.pageTitle) {
            titleKey = toState.data.pageTitle;
        }
        $window.document.title = titleKey;
    });
    $rootScope.$on('IdleStart', function() {
        if (!countdownDialog) {
            countdownDialog = $uibModal.open({
                animation: false,
                template: template,
                controller: 'CountdownDialogController',
                controllerAs: 'vm',
                windowClass: 'modal-danger',
                resolve: {
                    countdown: function() {
                        return countdown;
                    },
                    idleTime: function() {
                        return idleTime;
                    }
                }
            });
        }
    });
    $rootScope.$on('IdleEnd', function() {
        if (countdownDialog) {
            countdownDialog.close();
            countdownDialog = null;
        }
    });
    $rootScope.$on('IdleTimeout', function() {
        if (countdownDialog) {
            countdownDialog.close();
            countdownDialog = null;
        }
        authService.logout();
        $state.go('login');
    });

    $rootScope.back = function() {
        // If previous state is 'activate' or do not exist go to 'home'
        if ($rootScope.previousStateName === 'activate' || $state.get($rootScope.previousStateName) === null) {
            $state.go('experiment');
        } else {
            $state.go($rootScope.previousStateName, $rootScope.previousStateParams);
        }
    };
    // Theme for angular-xeditable. Can also be 'bs2', 'default'
    editableOptions.theme = 'bs3';

    function updateCSRFTOKEN($cookies, $http) {
        var csrfToken = $cookies.get('CSRF-TOKEN');
        $http.defaults.headers.post['X-CSRF-TOKEN'] = csrfToken;
        $http.defaults.headers.put['X-CSRF-TOKEN'] = csrfToken;
    }
}

module.exports = appRun;
