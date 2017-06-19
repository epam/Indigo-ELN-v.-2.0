angular.module('indigoeln')
    .config(function ($stateProvider) {
        $stateProvider
            .state('entities.template', {
                parent: 'entities',
                url: '/templates',
                data: {
                    authorities: ['TEMPLATE_EDITOR'],
                    pageTitle: 'Templates',
                    tab: {
                        name: 'Templates',
                        kind: 'management',
                        state: 'entities.template',
                        type:'entity'
                    }
                },
                views: {
                    'tabContent': {
                        templateUrl: 'scripts/app/entities/template-management/templates.html',
                        controller: 'TemplateController',
                        controllerAs: 'templateController'
                    }
                },
                resolve: {}
            })
            .state('entities.template-new', {
                parent: 'entities',
                url: '/template/new',
                data: {
                    authorities: ['TEMPLATE_EDITOR'],
                    tab: {
                        name: 'New Template',
                        kind: 'management',
                        state: 'entities.template-new',
                        type:'entity'
                    }
                },
                views: {
                    'tabContent': {
                        templateUrl: 'scripts/app/entities/template-management/modal/template-modal.html',
                        controller: 'TemplateModalController',
                        controllerAs: 'templateModalController'
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
            .state('entities.template-detail', {
                parent: 'entities',
                url: '/template/{id}',
                data: {
                    authorities: ['TEMPLATE_EDITOR'],
                    pageTitle: 'Template',
                    tab: {
                        name: 'Templates',
                        kind: 'management',
                        state: 'template.detail',
                        type:'',
                        id: null,
                        cache: true,
                        service: 'Template',
                        params: null,
                        resource: null
                    }
                },
                views: {
                    'tabContent': {
                        templateUrl: 'scripts/app/entities/template-management/detail/template-detail.html',
                        controller: 'TemplateDetailController',
                        controllerAs: 'templateDetailController'
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
            .state('entities.template-edit', {
                parent: 'entities',
                url: '/template/{id}/edit',
                data: {
                    authorities: ['TEMPLATE_EDITOR'],
                    tab: {
                        name: 'Edit Template',
                        kind: 'management',
                        type:'entity',
                        state: 'entities.template-edit'
                    }
                },
                views: {
                    'tabContent': {
                        templateUrl: 'scripts/app/entities/template-management/modal/template-modal.html',
                        controller: 'TemplateModalController',
                        controllerAs: 'templateModalController'

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
            .state('entities.template.delete', {
                parent: 'entities.template',
                url: '/{id}/delete',
                data: {
                    authorities: ['TEMPLATE_EDITOR'],
                    tab: {
                        //override parent type
                        type:''
                    }
                },
                onEnter: ['$stateParams', '$state', '$uibModal', function ($stateParams, $state, $uibModal) {
                    $uibModal.open({
                        templateUrl: 'scripts/app/entities/template-management/delete-dialog/template-delete-dialog.html',
                        controller: 'TemplateDeleteController',
                        controllerAs: 'templateDeleteController',
                        size: 'md'
                    }).result.then(function () {
                            $state.go('entities.template', null, {reload: true});
                        }, function () {
                            $state.go('^');
                        });
                }]
            });
    });
