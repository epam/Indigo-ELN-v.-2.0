var entitiesToSaveTemplate = require('./entities-to-save/entities-to-save.html');
var structureValidationTemplate = require('./structure-validation/structure-validation.html');

dialogService.$inject = ['$uibModal'];

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
            template: structureValidationTemplate,
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
            template: entitiesToSaveTemplate,
            resolve: {
                data: function() {
                    return data;
                }
            }
        }).result;
    }
}

module.exports = dialogService;

