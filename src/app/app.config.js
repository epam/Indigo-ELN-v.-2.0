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
    IdleProvider.idle(1 * 60);
    // 30 sec to do something
    IdleProvider.timeout(30);
    // to allow file's export
    $compileProvider.aHrefSanitizationWhitelist(/^\s*(https?|ftp|mailto|tel|file|blob):/);

    $animateProvider.classNameFilter(/\banimated\b/);
}

module.exports = appConfig;
