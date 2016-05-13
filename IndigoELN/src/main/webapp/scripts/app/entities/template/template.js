angular.module('indigoeln')
    .config(function ($stateProvider) {
        $stateProvider
            .state('template', {
                parent: 'tab',
                url: '/templates',
                data: {
                    authorities: ['TEMPLATE_EDITOR'],
                    pageTitle: 'Templates',
                    tab: {
                        name: 'Templates',
                        kind: 'management',
                        state: 'template'
                    }
                },
                views: {
                    'tabContent': {
                        templateUrl: 'scripts/app/entities/template/templates.html',
                        controller: 'TemplateController'
                    }
                },
                resolve: {}
            })
            .state('template.new', {
                parent: 'tab',
                url: '/template/new',
                data: {
                    authorities: ['TEMPLATE_EDITOR'],
                    tab: {
                        name: 'New Template',
                        kind: 'management',
                        state: 'template.new'
                    }
                },
                views: {
                    'tabContent': {
                        templateUrl: 'scripts/app/entities/template/template-dialog.html',
                        controller: 'TemplateDialogController'
                    }
                },
                resolve: {
                    pageInfo: function($q) {
                        var deferred = $q.defer();
                        $q.all([
                            $q.when({ name: null, id: null })
                        ]).then(function(results){
                            deferred.resolve({
                                entity: results[0]
                            });
                        });
                        return deferred.promise;
                    }
                }
            })
            .state('template.detail', {
                parent: 'tab',
                url: '/template/{id}',
                data: {
                    authorities: ['TEMPLATE_EDITOR'],
                    pageTitle: 'Template',
                    tab: {
                        name: 'Manage Templates',
                        kind: 'management',
                        state: 'template.detail',
                        id: null,
                        cache: true,
                        service: 'Template',
                        params: null,
                        resource: null
                    }
                },
                views: {
                    'tabContent': {
                        templateUrl: 'scripts/app/entities/template/template-detail.html',
                        controller: 'TemplateDetailController'
                    }
                },
                resolve: {
                    pageInfo: function($q, $stateParams, Template) {
                        var deferred = $q.defer();
                        $q.all([
                            Template.get({id: $stateParams.id}).$promise
                        ]).then(function(results){
                            deferred.resolve({
                                entity: results[0]
                            });
                        });
                        return deferred.promise;
                    }
                }
            })
            .state('template.edit', {
                parent: 'tab',
                url: '/template/{id}/edit',
                data: {
                    authorities: ['TEMPLATE_EDITOR'],
                    tab: {
                        name: 'Edit Template',
                        kind: 'management',
                        state: 'template.edit'
                    }
                },
                views: {
                    'tabContent': {
                        templateUrl: 'scripts/app/entities/template/template-dialog.html',
                        controller: 'TemplateDialogController'
                    }
                },
                resolve: {
                    pageInfo: function($q, $stateParams, Template) {
                        var deferred = $q.defer();
                        $q.all([
                            Template.get({id: $stateParams.id}).$promise
                        ]).then(function(results){
                            deferred.resolve({
                                entity: results[0]
                            });
                        });
                        return deferred.promise;
                    }
                }
            })
            .state('template.delete', {
                parent: 'template',
                url: '/{id}/delete',
                data: {
                    authorities: ['TEMPLATE_EDITOR']
                },
                onEnter: ['$stateParams', '$state', '$uibModal', function ($stateParams, $state, $uibModal) {
                    $uibModal.open({
                        templateUrl: 'scripts/app/entities/template/template-delete-dialog.html',
                        controller: 'TemplateDeleteController',
                        size: 'md'
                    }).result.then(function () {
                            $state.go('template', null, {reload: true});
                        }, function () {
                            $state.go('^');
                        });
                }]
            });
    });
