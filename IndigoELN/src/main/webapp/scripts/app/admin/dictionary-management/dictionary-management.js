'use strict';

angular.module('indigoeln')
    .config(function ($stateProvider) {
        $stateProvider
            .state('dictionary-management', {
                parent: 'admin',
                url: '/dictionary-management',
                data: {
                    authorities: ['DICTIONARY_EDITOR'],
                    pageTitle: 'indigoeln'
                },
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/admin/dictionary-management/dictionary-management.html',
                        controller: 'DictionaryManagementController'
                    }
                }
            })
            .state('dictionary-management.delete', {
                parent: 'dictionary-management',
                url: '/dictionary/{id}/delete',
                data: {
                    authorities: ['DICTIONARY_EDITOR']
                },
                onEnter: ['$stateParams', '$state', '$uibModal', function ($stateParams, $state, $uibModal) {
                    $uibModal.open({
                        templateUrl: 'scripts/app/admin/dictionary-management/dictionary-management-delete-dialog.html',
                        controller: 'DictionaryManagementDeleteController',
                        size: 'md',
                        resolve: {
                            entity: function (Dictionary) {
                                return Dictionary.get({id: $stateParams.id}).$promise;
                            }
                        }
                    }).result.then(function (result) {
                        $state.go('dictionary-management', null, {reload: true});
                    }, function () {
                        $state.go('^');
                    });
                }]
            });
    });
