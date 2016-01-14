'use strict';

angular.module('indigoeln', ['ui.router', 'ngResource', 'ui.tree', 'ui.bootstrap', 'ngAnimate', 'ngRoute'])

    .config(['$stateProvider', '$urlRouterProvider', '$provide', '$httpProvider', function ($stateProvider, $urlRouterProvider, $provide, $httpProvider) {
        $urlRouterProvider.otherwise('/');
        $stateProvider.state('navbar', {
            'abstract': true,
            views: {
                'navbar@': {
                    templateUrl: 'scripts/components/navbar/navbar.html',
                    controller: 'NavbarController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                //getAuth: ['authService',
                //    function (authService) {
                //        return authService.getAuth();
                //    }
                //]
            }
        }).state('sidebar', {
            'abstract': true,
            parent: 'navbar',
            views: {
                'sidebar@': {
                    templateUrl: 'scripts/components/sidebar/sidebar.html',
                    controller: 'SidebarController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                //getAuth: ['authService',
                //    function (authService) {
                //        return authService.getAuth();
                //    }
                //]
            }
        });
        $httpProvider.interceptors.push('errorHandlerInterceptor');

    }]);
        
