'use strict';

angular.module('indigoeln')
    .config(function ($stateProvider) {
        $stateProvider
            .state('project', {
                parent: 'entity',
                url: '/projects/{id}',
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/entities/project/project-detail.html',
                        controller: 'ProjectDetailController'
                    }
                },
                data: {
                    authorities: ['CONTENT_EDITOR', 'PROJECT_READER'],
                    pageTitle: 'indigoeln'
                },
                resolve: {
                    project: function($stateParams, Project) {
                        return Project.get({id : $stateParams.id}).$promise;
                    },
                    users: function(User) {
                        return User.query().$promise;
                    }
                }
            })
            .state('project.permissions', {
                parent: 'project',
                url: '/permissions',
                data: {
                    authorities: ['CONTENT_EDITOR']
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
                                return PermissionManagement.getProjectPermissions();
                            }
                        }
                    }).result.then(function(result) {
                        PermissionManagement.setAccessList(result);
                        $rootScope.$broadcast('access-list-changed');
                        $state.go('project');
                    }, function() {
                        $state.go('^');
                    })
                }
            });
    });