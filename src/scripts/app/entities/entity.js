angular
    .module('indigoeln')
    .config(function($stateProvider) {
        $stateProvider
            .state('entity', {
                abstract: true,
                parent: 'app_page'
            });
    });
