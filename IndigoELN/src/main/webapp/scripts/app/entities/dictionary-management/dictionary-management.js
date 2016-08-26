angular.module('indigoeln')
    .config(function ($stateProvider) {
        $stateProvider
            .state('entities.dictionary-management', {
                url: '/dictionary-management',
                data: {
                    authorities: ['DICTIONARY_EDITOR'],
                    pageTitle: 'indigoeln',
                    tab: {
                        name: 'Dictionaries',
                        kind: 'management',
                        state: 'entities.dictionary-management',
                        type:'entity'
                    }
                },
                views: {
                    'tabContent': {
                        templateUrl: 'scripts/app/entities/dictionary-management/dictionary-management.html',
                        controller: 'DictionaryManagementController'
                    }
                }
            })
            .state('entities.dictionary-management.delete', {
                url: '/dictionary/{id}/delete',
                data: {
                    authorities: ['DICTIONARY_EDITOR'],
                    tab: {
                        type:''
                    }
                },
                onEnter: ['$stateParams', '$state', '$uibModal', function ($stateParams, $state, $uibModal) {
                    $uibModal.open({
                        templateUrl: 'scripts/app/entities/dictionary-management/dictionary-management-delete-dialog.html',
                        controller: 'DictionaryManagementDeleteController',
                        size: 'md',
                        resolve: {
                            entity: function (Dictionary) {
                                return Dictionary.get({id: $stateParams.id}).$promise;
                            }
                        }
                    }).result.then(function () {
                        $state.go('entities.dictionary-management', null, {reload: true});
                    }, function () {
                        $state.go('entities.dictionary-management');
                    });
                }]
            });
    });
