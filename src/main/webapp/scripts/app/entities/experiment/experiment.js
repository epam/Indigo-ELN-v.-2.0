'use strict';

angular.module('indigoeln')
    .config(function ($stateProvider) {
        $stateProvider
            .state('experiment', {
                parent: 'entity',
                url: '/experiment/{id}',
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/entities/experiment/detail/experiment-detail.html',
                        controller: 'ExperimentDetailController'
                    }
                },
                bindToController: true,
                resolve: {
                }
            })
            .state('newexperiment', {
                parent: 'entity',
                url: '/newexperiment',
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/entities/experiment/new/new-experiment.html',
                        controller: 'NewExperimentController'
                    }
                },
                data: {
                    authorities: ['ROLE_ADMIN', 'ROLE_USER'],
                    pageTitle: 'indigoeln'
                },
                resolve: {
                }
            });
    });