angular
    .module('indigoeln')
    .factory('dialogService', dialogService);

/* @ngInject */
function dialogService($uibModal) {
    return {
        structureValidation: structureValidation,
        selectEntitiesToSave: selectEntitiesToSave
    };

    function structureValidation(batches, searchQuery, callback) {
        $uibModal.open({
            animation: true,
            size: 'lg',
            controller: 'StructureValidationController',
            templateUrl: 'scripts/components/entities/template/components/common/dialog-service/structure-validation/structure-validation.html',
            resolve: {
                batches: function() {
                    return batches;
                },
                searchQuery: function() {
                    return searchQuery;
                }
            }
        }).result.then(function(result) {
            callback(result);
        });
    }

    function selectEntitiesToSave(data, callback) {
        return $uibModal.open({
            animation: true,
            size: 'md',
            controller: 'EntitiesToSaveController',
            controllerAs: 'vm',
            templateUrl: 'scripts/components/entities/template/components/common/dialog-service/entities-to-save/entities-to-save.html',
            resolve: {
                data: function() {
                    return data;
                }
            }
        }).result.then(function(result) {
            if (_.isFunction(callback)) {
                callback(result);
            }

            return result;
        });
    }
}
