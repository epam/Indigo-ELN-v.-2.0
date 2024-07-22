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

/* eslint angular/on-watch: off*/
var template = require('./common/components/timer/timer-dialog.html');

/* @ngInject */
function appRun($rootScope, $window, $state, $uibModal, editableOptions, authService, principalService, Idle,
                $http, $cookies) {
    updateCSRFTOKEN();

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

    function updateCSRFTOKEN() {
        var csrfToken = $cookies.get('CSRF-TOKEN');

        $http.defaults.headers.post['X-CSRF-TOKEN'] = csrfToken;
        $http.defaults.headers.put['X-CSRF-TOKEN'] = csrfToken;
        $http.defaults.headers.delete = {'X-CSRF-TOKEN': csrfToken};
    }
}

module.exports = appRun;
