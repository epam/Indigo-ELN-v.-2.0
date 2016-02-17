'use strict';

angular.module('indigoeln')
    .config(function ($stateProvider) {
        $stateProvider
            .state('notebook', {
                parent: 'entity',
                url: '/project/{projectId}/notebook/{notebookId}',
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/entities/notebook/notebook-dialog.html',
                        controller: 'NotebookDialogController'
                    }
                },
                data: {
                    authorities: ['CONTENT_EDITOR', 'NOTEBOOK_READER'],
                    pageTitle: 'indigoeln'
                },
                resolve: {
                    notebook: function($stateParams, Notebook) {
                        return $stateParams.id ?  Notebook.get({projectId: $stateParams.projectId, id: $stateParams.id}).$promise : {};
                    },
                    identity: function (Principal) {
                        return Principal.identity()
                    }
                }
            })
            .state('notebook.permissions', {
                parent: 'notebook',
                url: '/permissions',
                data: {
                    authorities: ['CONTENT_EDITOR', 'NOTEBOOK_CREATOR']
                },
                onEnter: function($rootScope, $stateParams, $state, $uibModal, PermissionManagement) {
                    $uibModal.open({
                        templateUrl: 'scripts/components/permissions/permission-management.html',
                        controller: 'PermissionManagementController',
                        size: 'lg',
                        resolve: {
                            users: function(User) {
                                return User.query().$promise;
                            },
                            permissions: function() {
                                return PermissionManagement.getNotebookPermissions();
                            }
                        }
                    }).result.then(function(result) {
                        PermissionManagement.setAccessList(result);
                        $rootScope.$broadcast('access-list-changed');
                        $state.go('notebook', {projectId: $stateParams.projectId, id: $stateParams.id});
                    }, function() {
                        $state.go('^');
                    })
                }
            });
    });