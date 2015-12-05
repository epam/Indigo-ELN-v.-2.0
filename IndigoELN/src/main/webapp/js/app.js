angular.module('indigoeln', ['ui.router', 'ngResource', 'ui.tree', 'ngMaterial'])

.config(['$stateProvider', '$urlRouterProvider', function($stateProvider, $urlRouterProvider) {
	$urlRouterProvider.otherwise('/welcome');
	
    $stateProvider.state('template', {
        views: {
            '': {
                templateUrl: 'views/template.html'
            }
        }
    })
    .state('welcome', {
        url: '/welcome',
        parent: 'template',
        templateUrl: 'views/welcome.html'
    })
}]);
        
