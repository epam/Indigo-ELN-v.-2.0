'use strict';

angular.module('indigoeln')
    .config(function ($stateProvider) {
        $stateProvider
            .state('experiment', {
                parent: 'template',
                url: '/experiment/{id}',
                data: {
                    authorities: ['ROLE_USER'],
                    pageTitle: 'Experiment'
                },
                templateUrl: 'scripts/app/entities/experiment/detail/experiment-detail.html',
                controller: 'ExperimentDetailController',
                controllerAs: 'vm',
                bindToController: true,
                resolve: {
                    //entity: ['$stateParams', 'experimentService', function($stateParams, experimentService) {
                    //    return experimentService.get({id : $stateParams.id});
                    //}]
                }
            });
        //.state('experiment.new', {
        //    parent: 'experiment',
        //    url: '/new',
        //    data: {
        //        authorities: ['ROLE_USER']
        //    },
        //    onEnter: ['$stateParams', '$state', '$modal', function($stateParams, $state, $modal) {
        //        $modal.open({
        //            templateUrl: 'scripts/app/entities/experiment/experiment-dialog.html',
        //            controller: 'experimentDialogController',
        //            size: 'lg',
        //            resolve: {
        //                entity: function () {
        //                    return {
        //                        title: null,
        //                        description: null,
        //                        id: null
        //                    };
        //                }
        //            }
        //        }).result.then(function(result) {
        //                $state.go('experiment', null, { reload: true });
        //            }, function() {
        //                $state.go('experiment');
        //            });
        //    }]
        //})
        //.state('experiment.edit', {
        //    parent: 'experiment',
        //    url: '/{id}/edit',
        //    data: {
        //        authorities: ['ROLE_USER']
        //    },
        //    onEnter: ['$stateParams', '$state', '$modal', function($stateParams, $state, $modal) {
        //        $modal.open({
        //            templateUrl: 'scripts/app/entities/experiment/experiment-dialog.html',
        //            controller: 'ExperimentDialogController',
        //            size: 'lg',
        //            resolve: {
        //                entity: ['experiment', function(experiment) {
        //                    return experiment.get({id : $stateParams.id});
        //                }]
        //            }
        //        }).result.then(function(result) {
        //                $state.go('experiment', null, { reload: true });
        //            }, function() {
        //                $state.go('^');
        //            });
        //    }]
        //});
    });