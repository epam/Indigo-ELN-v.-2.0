angular
    .module('indigoeln')
    .config(appConfig);

appConfig.$inject = ['$stateProvider', '$urlRouterProvider', '$httpProvider', '$compileProvider', 'IdleProvider',
    '$animateProvider'];

function appConfig($stateProvider, $urlRouterProvider, $httpProvider, $compileProvider, IdleProvider,
                   $animateProvider) {
    // enable CSRF
    $httpProvider.defaults.xsrfCookieName = 'CSRF-TOKEN';
    $httpProvider.defaults.xsrfHeaderName = 'X-CSRF-TOKEN';
    $httpProvider.defaults.withCredentials = true;

    $urlRouterProvider.otherwise('/');
    $stateProvider.state('app_page', {
        abstract: true,
        views: {
            'app_page@': {
                template: '<app-page></app-page>'
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
                return principalService.identity();
            }
        }
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