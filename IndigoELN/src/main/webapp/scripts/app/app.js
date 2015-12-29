angular.module('indigoeln', ['ui.router', 'ngResource', 'ui.tree', 'ui.bootstrap', 'ngAnimate', 'ngRoute'])

    .config(['$stateProvider', '$urlRouterProvider', '$provide', '$httpProvider', function ($stateProvider, $urlRouterProvider, $provide, $httpProvider) {
        $urlRouterProvider.otherwise('/home');

        $stateProvider.state('template', {
            views: {
                '': {
                    templateUrl: 'scripts/components/template/template.html'
                }
            }
        }).state('welcome', {
            url: '/home',
            parent: 'template',
            templateUrl: 'scripts/app/home/home.html',
            controller: 'homeController'
        }).state('login', {
            url: '/login',
            templateUrl: 'scripts/app/account/login/login.html',
            controller: 'loginController'
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
        
