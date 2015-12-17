angular.module('indigoeln', ['ui.router', 'ngResource', 'ui.tree', 'ui.bootstrap', 'ngAnimate', 'ngRoute'])

    .config(['$stateProvider', '$urlRouterProvider', '$provide', '$httpProvider', function ($stateProvider, $urlRouterProvider, $provide, $httpProvider) {
        $urlRouterProvider.otherwise('/welcome');

        $stateProvider.state('template', {
            views: {
                '': {
                    templateUrl: 'src/app/template.html'
                }
            }
        }).state('welcome', {
            url: '/welcome',
            parent: 'template',
            templateUrl: 'src/app/welcome.html',
            controller: 'homeController'
        }).state('login', {
            url: '/login',
            templateUrl: 'src/app/login.html',
            controller: 'loginController'
        });

        $provide.factory('myHttpInterceptor', function ($q, $rootScope, $injector) {
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
                    if (rejection.status == 401 && rejection.config.url !== 'login') {
                        var $location = $injector.get('$location');
                        $location.path("/login");
                    }
                    return $q.reject(rejection);
                }
            };
        });
        $httpProvider.interceptors.push('myHttpInterceptor');

    }]);
        
