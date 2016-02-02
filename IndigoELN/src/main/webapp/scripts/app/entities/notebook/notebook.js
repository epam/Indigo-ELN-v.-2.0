'use strict';

angular.module('indigoeln')
    .config(function ($stateProvider) {
        $stateProvider
            .state('notebook', {
                parent: 'entity',
                url: '/notebook/{id}',
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/entities/notebook/detail/notebook-detail.html',
                        controller: 'NotebookDetailController'
                    }
                },
                params: {
                    projectId: ''
                },
                data: {
                    authorities: ['ROLE_ADMIN', 'ROLE_USER'],
                    pageTitle: 'indigoeln'
                },
                resolve: {
                    data: ['$stateParams', 'Notebook', function($stateParams, Notebook) {
                        return Notebook.get({id : $stateParams.id}).$promise;
                    }]
                }
            })
            .state('newnotebook', {
                parent: 'entity',
                url: '/newnotebook',
                data: {
                    authorities: []
                },
                params: {
                    projectId: '',
                    notebookName: ''
                },
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/entities/notebook/new/new-notebook.html',
                        controller: 'NewNotebookController'
                    }
                },
                bindToController: true,
                resolve: {
                    notebook: function(Notebook, $stateParams) {
                        return Notebook.save({projectId: $stateParams.projectId}, {
                            name : $stateParams.notebookName,
                            accessList: [] //TODO add access list [{userId: 'userId', permissions: 'RERSCSUE'}, {...}]
                        }).$promise;
                    },
                    projectId: function($stateParams) {
                        return $stateParams.projectId;
                    }
                }
            });
    });