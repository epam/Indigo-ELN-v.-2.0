angular.module('indigoeln', ['ui.router', 'ngResource', 'ui.tree', 'ui.bootstrap', 'ngAnimate'])

.config(['$stateProvider', '$urlRouterProvider', function($stateProvider, $urlRouterProvider) {
	$urlRouterProvider.otherwise('/welcome');
	
    $stateProvider.state('template', {
        views: {
            '': {
                templateUrl: 'src/app/template.html'
            }
        }
    })
    .state('welcome', {
        url: '/welcome',
        parent: 'template',
        templateUrl: 'src/app/welcome.html',
        controller: 'homeController'
    })
    .state('login', {
        url: '/login',
        templateUrl: 'src/app/login.html',
        controller: 'loginController'
    })

}]);
        
