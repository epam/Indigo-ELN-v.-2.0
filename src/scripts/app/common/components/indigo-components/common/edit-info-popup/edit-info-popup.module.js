var indigoEditor = require('./editor/indigo-structure-editor.directive');

var EditExternalSupplierController = require('./edit-external-supplier/edit-external-supplier.controller');
var EditMeltingPointController = require('./edit-melting-point/edit-melting-point.controller');
var EditPurityController = require('./edit-purity/edit-purity.controller');
var EditResidualSolventsController = require('./edit-residual-solvents/edit-residual-solvents.controller');
var EditSolubilityController = require('./edit-solubility/edit-solubility.controller');
var SelectFromDictionaryController = require('./select-from-dictionary/select-from-dictionary.controller');

module.export = angular
    .module('indigoeln.editInfoPopup', [])

    .factory('indigoEditor', indigoEditor)

    .controller('EditExternalSupplierController', EditExternalSupplierController)
    .controller('EditMeltingPointController', EditMeltingPointController)
    .controller('EditPurityController', EditPurityController)
    .controller('EditResidualSolventsController', EditResidualSolventsController)
    .controller('EditSolubilityController', EditSolubilityController)
    .controller('SelectFromDictionaryController', SelectFromDictionaryController)

    .name;
