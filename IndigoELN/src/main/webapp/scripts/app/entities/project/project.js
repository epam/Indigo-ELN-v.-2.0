'use strict';

angular.module('indigoeln')
    .config(function ($stateProvider) {
        $stateProvider
            .state('project', {
                parent: 'entity',
                url: '/projects',
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/entities/project/project.html',
                        controller: 'NewProjectCtrl'
                    }
                },
                resolve: {
                }
            })
            .state('project.new', {
                parent: 'project',
                url: '/new',
                onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                    $uibModal.open({
                        templateUrl: 'scripts/app/entities/project/new-project.html',
                        controller: 'NewProjectCtrl',
                        size: 'lg',
                        resolve: {
                            entity: function () {
                                return {
                                    title: null,
                                    id: null
                                };
                            }
                        }
                    }).result.then(function(result) {
                        //$state.go('project', null, { reload: true });
                    }, function() {
                        //$state.go('project');
                    });
                }]
            });
    });