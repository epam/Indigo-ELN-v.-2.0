angular.module('indigoeln')
    .config(function($stateProvider) {
        $stateProvider
            .state('entities', {
                abstract: true,
                parent: 'entity',
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/entities/entities.html',
                        controller: 'EntitiesController',
                        controllerAs: 'vm'
                    }
                }
            });
    });
