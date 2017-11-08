var dictionaryManagementTemplate = require('./component/dictionary-management.html');
var dictionaryManagementDeleteDialogTemplate = require('./delete-dialog/dictionary-management-delete-dialog.html');

dictionaryManagementConfig.$inject = ['$stateProvider'];

function dictionaryManagementConfig($stateProvider) {
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
                    type: 'entity'
                }
            },
            views: {
                tabContent: {
                    template: dictionaryManagementTemplate,
                    controller: 'DictionaryManagementController',
                    controllerAs: 'vm'
                }
            }
        })
        .state('entities.dictionary-management.delete', {
            url: '/dictionary/{id}/delete',
            data: {
                authorities: ['DICTIONARY_EDITOR'],
                tab: {
                    type: ''
                }
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    template: dictionaryManagementDeleteDialogTemplate,
                    controller: 'DictionaryManagementDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: function(dictionaryService) {
                            return dictionaryService.get({
                                id: $stateParams.id
                            }).$promise;
                        }
                    }
                }).result.then(function() {
                    $state.go('entities.dictionary-management', null, {
                        reload: true
                    });
                }, function() {
                    $state.go('entities.dictionary-management');
                });
            }]
        });
}

module.exports = dictionaryManagementConfig;
