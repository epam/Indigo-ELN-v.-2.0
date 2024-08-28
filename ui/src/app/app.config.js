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

var errorTemplate = require('./common/templates/error.html');
var accessDeniedTemplate = require('./common/templates/access-denied.html');
appConfig.$inject = ['$stateProvider', '$urlRouterProvider', '$httpProvider', '$compileProvider', 'IdleProvider',
    '$animateProvider'];

function appConfig($stateProvider, $urlRouterProvider, $httpProvider, $compileProvider, IdleProvider,
                   $animateProvider) {
    // enable CSRF
    $httpProvider.defaults.xsrfCookieName = 'CSRF-TOKEN';
    $httpProvider.defaults.xsrfHeaderName = 'X-CSRF-TOKEN';
    $httpProvider.defaults.withCredentials = true;

    $urlRouterProvider.otherwise('/');

    $stateProvider
        .state('app_page', {
            abstract: true,
            views: {
                'app_page@': {
                    template: '<app-layout></app-layout>'
                }
            },
            resolve: {
                appUrl: function($http, apiUrl, configService) {
                    return $http.get(apiUrl + 'client_configuration').then(
                        function(response) {
                            configService.setConfiguration(response.data);
                        }
                    );
                },
                authorize: function(authService) {
                    return authService.authorize();
                },
                user: function(principalService) {
                    return principalService.checkIdentity();
                }
            }
        })
        .state('error', {
            parent: 'app_page',
            url: '/error',
            data: {
                authorities: [],
                pageTitle: 'Error page!'
            },
            views: {
                'content@app_page': {
                    template: errorTemplate
                }
            },
            resolve: {}
        })
        .state('accessdenied', {
            parent: 'app_page',
            url: '/accessdenied',
            data: {
                authorities: []
            },
            views: {
                'content@app_page': {
                    template: accessDeniedTemplate
                }
            },
            resolve: {}
        });

    $httpProvider.interceptors.push('errorHandlerInterceptor');
    $httpProvider.interceptors.push('notificationInterceptor');

    // 30 min of idleness
    IdleProvider.idle(30 * 60);
    // 30 sec to do something
    IdleProvider.timeout(30);
    // to allow file's export
    $compileProvider.aHrefSanitizationWhitelist(/^\s*(https?|ftp|mailto|tel|file|blob):/);

    $animateProvider.classNameFilter(/\banimated\b/);
}

module.exports = appConfig;
