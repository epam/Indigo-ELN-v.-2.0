angular
    .module('indigoeln.componentsModule')
    .factory('dialogService', dialogService);

/* @ngInject */
function dialogService($uibModal) {
    return {
        structureValidation: structureValidation,
        selectEntitiesToSave: selectEntitiesToSave
    };

    function structureValidation(batches, searchQuery) {
        return $uibModal.open({
            animation: true,
            size: 'lg',
            controller: 'StructureValidationController',
            controllerAs: 'vm',
            templateUrl: 'scripts/indigo-components/common/dialog-service/structure-validation/structure-validation.html',
            resolve: {
                batches: function() {
                    return batches;
                },
                searchQuery: function() {
                    return searchQuery;
                }
            }
        }).result;
    }

    function selectEntitiesToSave(data) {
        return $uibModal.open({
            animation: true,
            size: 'md',
            controller: 'EntitiesToSaveController',
            controllerAs: 'vm',
            templateUrl: 'scripts/indigo-components/common/dialog-service/entities-to-save/entities-to-save.html',
            resolve: {
                data: function() {
                    return data;
                }
            }
        }).result;
    }
}
