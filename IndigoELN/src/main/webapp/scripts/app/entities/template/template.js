'use strict';

angular.module('indigoeln')
    .config(function ($stateProvider) {
        $stateProvider
            .state('template', {
                parent: 'entity',
                url: '/templates',
                data: {
                    authorities: ['ROLE_USER'],
                    pageTitle: 'Templates'
                },
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/entities/template/templates.html',
                        controller: 'TemplateController'
                    }
                },
                resolve: {}
            })
            .state('template.detail', {
                parent: 'entity',
                url: '/template/{id}',
                data: {
                    authorities: ['ROLE_USER'],
                    pageTitle: 'Template'
                },
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/entities/template/template-detail.html',
                        controller: 'TemplateDetailController'
                    }
                },
                resolve: {
                    entity: ['$stateParams', 'Template', function ($stateParams, Template) {
                        return Template.get({id: $stateParams.id}).$promise;
                    }]
                }
            })
            .state('template.new', {
                parent: 'entity',
                url: '/templates/new',
                data: {
                    authorities: ['ROLE_USER'],
                },
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/entities/template/template-dialog.html',
                        controller: 'TemplateDialogController'
                    }
                },
                resolve: {
                    entity: function () {
                        return {
                            name: null,
                            id: null
                        };
                    }
                }
            })
            .state('template.edit', {
                parent: 'entity',
                url: '/template/{id}/edit',
                data: {
                    authorities: ['ROLE_USER'],
                },
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/entities/template/template-dialog.html',
                        controller: 'TemplateDialogController'
                    }
                },
                resolve: {
                    entity: function (Template, $stateParams) {
                        return Template.get({id: $stateParams.id}).$promise;
                    }
                }
            })
            .state('template.delete', {
                parent: 'template',
                url: '/{id}/delete',
                data: {
                    authorities: ['ROLE_USER'],
                },
                onEnter: ['$stateParams', '$state', '$uibModal', function ($stateParams, $state, $uibModal) {
                    $uibModal.open({
                        templateUrl: 'scripts/app/entities/template/template-delete-dialog.html',
                        controller: 'TemplateDeleteController',
                        size: 'md',
                        resolve: {
                            entity: ['Template', function (Template) {
                                return Template.get({id: $stateParams.id});
                            }]
                        }
                    }).result.then(function (result) {
                            $state.go('template', null, {reload: true});
                        }, function () {
                            $state.go('^');
                        })
                }]
            });
    });
