'use strict';

angular.module('indigoeln', ['ui.router', 'ngResource', 'ui.tree', 'ui.bootstrap', 'ngAnimate', 'ngRoute'])

    .config(['$stateProvider', '$urlRouterProvider', '$provide', '$httpProvider', function ($stateProvider, $urlRouterProvider, $provide, $httpProvider) {
        $urlRouterProvider.otherwise('/');
        $stateProvider.state('navbar', {
            'abstract': true,
            views: {
                'navbar@': {
                    templateUrl: 'scripts/components/navbar/navbar.html',
                    controller: 'NavbarController'
                }
            },
            resolve: {
            }
        }).state('sidebar', {
            'abstract': true,
            parent: 'navbar',
            views: {
                'sidebar@': {
                    templateUrl: 'scripts/components/sidebar/sidebar.html',
                    controller: 'SidebarController'
                }
            },
            resolve: {
            }
        });

        $provide.factory('myHttpInterceptor', ['$q', '$rootScope', '$injector', function ($q, $rootScope, $injector) {
            return {
                'request': function (config) {
                    return config;
                },
                'requestError': function (rejection) {
                    return $q.reject(rejection);
                },
                'response': function (response) {
                    return response;
                },

                'responseError': function (rejection) {
                    if (rejection.status === 401 && rejection.config.url !== 'login') {
                        var $location = $injector.get('$location');
                        $location.path('/login');
                    }
                    return $q.reject(rejection);
                }
            };
        }]);
        $httpProvider.interceptors.push('myHttpInterceptor');

    }]);
        
