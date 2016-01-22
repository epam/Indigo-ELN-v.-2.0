'use strict';

angular.module('indigoeln')
    .config(function ($stateProvider) {
        $stateProvider
            .state('notebook', {
                parent: 'entity',
                url: '/notebook',
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/entities/notebook/detail/notebook-detail.html',
                        controller: 'NotebookDetailController',
                        controllerAs: 'vm'
                    }
                },
                bindToController: true,
                resolve: {
                }
            })
            .state('notebook.new', {
                parent: 'notebook',
                url: '/new',
                data: {
                    authorities: []
                },
                params: {
                    notebookName: ''
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/entities/notebook/new/new-notebook.html',
                        controller: 'NewNotebookController',
                        controllerAs: 'vm'
                    }
                },
                bindToController: true,
                resolve: {
                    notebook: function(notebookService, $stateParams) {
                        return notebookService.save({projectId: 'need_project_id'}, { // TODO added projectId
                            name : $stateParams.notebookName,
                            accessList: [] //TODO add access list [{userId: 'userId', permissions: 'RERSCSUE'}, {...}]
                        }).$promise;
                    }
                }
            });
    });