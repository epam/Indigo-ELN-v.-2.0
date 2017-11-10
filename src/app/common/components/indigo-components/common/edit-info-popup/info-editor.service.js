var editSolubilityTemplate = require('./edit-solubility/edit-solubility.html');
var editResidualSolventsTemplate = require('./edit-residual-solvents/edit-residual-solvents.html');
var editExternalSupplierTemplate = require('./edit-external-supplier/edit-external-supplier.html');
var editMeltingPointTemplate = require('./edit-melting-point/edit-melting-point.html');
var editPurityTemplate = require('./edit-purity/edit-purity.html');
var selectFromDictionaryTemplate = require('./select-from-dictionary/select-from-dictionary.html');

infoEditor.$inject = ['$uibModal'];

function infoEditor($uibModal) {
    return {
        editSolubility: editSolubility,
        editResidualSolvents: editResidualSolvents,
        editExternalSupplier: editExternalSupplier,
        editMeltingPoint: editMeltingPoint,
        editPurity: editPurity,
        editHealthHazards: editHealthHazards,
        editHandlingPrecautions: editHandlingPrecautions,
        editStorageInstructions: editStorageInstructions
    };

    function editSolubility(solubility, callback) {
        return $uibModal.open({
            animation: true,
            size: 'lg',
            controller: 'EditSolubilityController',
            controllerAs: 'vm',
            template: editSolubilityTemplate,
            resolve: {
                solubility: function() {
                    return solubility;
                }
            }
        }).result.then(function(result) {
            callback(result);

            return result;
        });
    }

    function editResidualSolvents(residualSolvents) {
        return $uibModal.open({
            animation: true,
            size: 'lg',
            controller: 'EditResidualSolventsController',
            controllerAs: 'vm',
            template: editResidualSolventsTemplate,
            resolve: {
                solvents: function() {
                    return (residualSolvents && residualSolvents.data) || [];
                }
            }
        }).result;
    }

    function editExternalSupplier(data, callback) {
        $uibModal.open({
            animation: true,
            size: 'md',
            controller: 'EditExternalSupplierController',
            controllerAs: 'vm',
            template: editExternalSupplierTemplate,
            resolve: {
                data: function() {
                    return data;
                }
            }
        }).result.then(function(result) {
            callback(result);
        });
    }

    function editMeltingPoint(data, callback) {
        $uibModal.open({
            animation: true,
            size: 'md',
            controller: 'EditMeltingPointController',
            controllerAs: 'vm',
            template: editMeltingPointTemplate,
            resolve: {
                data: function() {
                    return data;
                }
            }
        }).result.then(function(result) {
            callback(result);
        });
    }

    function editPurity(data, callback) {
        $uibModal.open({
            animation: true,
            size: 'lg',
            controller: 'EditPurityController',
            controllerAs: 'vm',
            template: editPurityTemplate,
            resolve: {
                data: function() {
                    return data;
                },
                dictionary: function(dictionaryService) {
                    return dictionaryService.get({
                        id: 'purity'
                    }).$promise;
                }
            }
        }).result.then(function(result) {
            callback(result);
        });
    }

    function editHealthHazards(data, callback) {
        var dictionary = 'healthHazards';
        var title = 'Edit Health Hazards';
        selectFromDictionary(dictionary, data, title, callback);
    }

    function editHandlingPrecautions(data, callback) {
        var dictionary = 'handlingPrecautions';
        var title = 'Edit Handling Precautions';
        selectFromDictionary(dictionary, data, title, callback);
    }

    function editStorageInstructions(data, callback) {
        var dictionary = 'storageInstructions';
        var title = 'Edit Storage Instructions';
        selectFromDictionary(dictionary, data, title, callback);
    }

    function selectFromDictionary(dictionary, model, title, callback) {
        $uibModal.open({
            animation: true,
            size: 'sm',
            controller: 'SelectFromDictionaryController',
            controllerAs: 'vm',
            template: selectFromDictionaryTemplate,
            resolve: {
                data: function() {
                    return model;
                },
                dictionary: function(dictionaryService) {
                    return dictionaryService.get({
                        id: dictionary
                    }).$promise;
                },
                title: function() {
                    return title;
                }
            }
        }).result.then(function(result) {
            callback(result);
        });
    }
}

module.exports = infoEditor;
