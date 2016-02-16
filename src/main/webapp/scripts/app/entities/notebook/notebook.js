'use strict';

angular.module('indigoeln')
    .config(function ($stateProvider) {
        $stateProvider
            .state('notebook', {
                parent: 'entity',
                url: '/project/{projectId}/notebook/{id}',
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/entities/notebook/detail/notebook-detail.html',
                        controller: 'NotebookDetailController'
                    }
                },
                //params: {
                //    projectId: ''
                //},
                data: {
                    authorities: ['NOTEBOOK_READER', 'CONTENT_EDITOR'],
                    pageTitle: 'indigoeln'
                },
                resolve: {
                    data: ['$stateParams', 'Notebook', function($stateParams, Notebook) {
                        return Notebook.get({projectId: $stateParams.projectId, id : $stateParams.id}).$promise;
                    }]
                }
            })
            .state('newnotebook', {
                parent: 'entity',
                url: '/newnotebook',
                data: {
                    authorities: ['CONTENT_EDITOR', 'NOTEBOOK_CREATOR']
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