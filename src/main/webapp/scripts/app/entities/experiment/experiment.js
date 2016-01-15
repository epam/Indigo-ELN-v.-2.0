'use strict';

angular.module('indigoeln')
    .config(function ($stateProvider) {
        $stateProvider
            .state('experiment', {
                parent: 'entity',
                url: '/experiment/{id}',
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/entities/experiment/detail/experiment-detail.html',
                        controller: 'ExperimentDetailController',
                        controllerAs: 'vm'
                    }
                },
                bindToController: true,
                resolve: {
                    //entity: ['$stateParams', 'experimentService', function($stateParams, experimentService) {
                    //    return experimentService.get({id : $stateParams.id});
                    //}]
                }
            })
            .state('experiment.new', {
                parent: 'experiment',
                url: '/experiment/new',
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/entities/experiment/new/new-experiment.html',
                        controller: 'NewExperimentController',
                        controllerAs: 'vm'
                    }
                },
                bindToController: true,
                resolve: {
                    //entity: ['$stateParams', 'experimentService', function($stateParams, experimentService) {
                    //    return experimentService.get({id : $stateParams.id});
                    //}]
                }
            });
    });