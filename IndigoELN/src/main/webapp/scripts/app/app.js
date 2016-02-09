'use strict';

angular.module('indigoeln',
    ['ui.router', 'ngResource', 'ui.tree', 'ui.bootstrap', 'ngAnimate', 'ngRoute',
        'xeditable', 'angularFileUpload', 'checklist-model', 'ngTagsInput', 'ngCookies', 'prettyBytes', angularDragula(angular),
        'cgBusy'])
    .run(function ($rootScope, $window, $state, editableOptions, Auth, Principal) {
        $rootScope.$on('$stateChangeStart', function (event, toState, toStateParams) {
            $rootScope.toState = toState;
            $rootScope.toStateParams = toStateParams;

            if (Principal.isIdentityResolved()) {
                Auth.authorize();
            }
        });
        $rootScope.$on('$stateChangeSuccess', function (event, toState, toParams, fromState, fromParams) {
            var titleKey = 'indigoeln';

            // Remember previous state unless we've been redirected to login or we've just
            // reset the state memory after logout. If we're redirected to login, our
            // previousState is already set in the authExpiredInterceptor. If we're going
            // to login directly, we don't want to be sent to some previous state anyway
            if (toState.name !== 'login') {
                $rootScope.previousStateName = fromState.name;
                $rootScope.previousStateParams = fromParams;
            }

            // Set the page title key to the one configured in state or use default one
            if (toState.data.pageTitle) {
                titleKey = toState.data.pageTitle;
            }
            $window.document.title = titleKey;
        });
        $rootScope.back = function () {
            // If previous state is 'activate' or do not exist go to 'home'
            if ($rootScope.previousStateName === 'activate' || $state.get($rootScope.previousStateName) === null) {
                $state.go('experiment');
            } else {
                $state.go($rootScope.previousStateName, $rootScope.previousStateParams);
            }
        };

        // Theme for angular-xeditable. Can also be 'bs2', 'default'
        editableOptions.theme = 'bs3';
    })
    .config(function ($stateProvider, $urlRouterProvider, $provide, $httpProvider) {

        //enable CSRF
        $httpProvider.defaults.xsrfCookieName = 'CSRF-TOKEN';
        $httpProvider.defaults.xsrfHeaderName = 'X-CSRF-TOKEN';

        $urlRouterProvider.otherwise('/');
        $stateProvider.state('app_page', {
            abstract: true,
            views: {
                'app_page@': {
                    templateUrl: 'scripts/components/app_page/app_page.html'
                }
            },
            resolve: {
                authorize: ['Auth',
                    function (Auth) {
                        return Auth.authorize();
                    }
                ]
            }
        }).state('navbar', {
            abstract: true,
            parent: 'app_page',
            views: {
                'navbar@app_page': {
                    templateUrl: 'scripts/components/navbar/navbar.html',
                    controller: 'NavbarController'
                }
            }
        }).state('sidebar', {
            abstract: true,
            parent: 'navbar',
            views: {
                'sidebar@app_page': {
                    templateUrl: 'scripts/components/sidebar/sidebar.html',
                    controller: 'SidebarController'
                }
            }
        });
        $httpProvider.interceptors.push('errorHandlerInterceptor');
        $httpProvider.interceptors.push('notificationInterceptor');

    });
