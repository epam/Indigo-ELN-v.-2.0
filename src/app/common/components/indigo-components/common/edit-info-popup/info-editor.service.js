/*
 * Copyright (C) 2015-2018 EPAM Systems
 *
 * This file is part of Indigo ELN.
 *
 * Indigo ELN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Indigo ELN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

var editSolubilityTemplate = require('./edit-solubility/edit-solubility.html');
var editResidualSolventsTemplate = require('./edit-residual-solvents/edit-residual-solvents.html');
var editExternalSupplierTemplate = require('./edit-external-supplier/edit-external-supplier.html');
var editMeltingPointTemplate = require('./edit-melting-point/edit-melting-point.html');
var editPurityTemplate = require('./edit-purity/edit-purity.html');
var selectFromDictionaryTemplate = require('./select-from-dictionary/select-from-dictionary.html');

infoEditorService.$inject = ['$uibModal'];

function infoEditorService($uibModal) {
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
                    'ngInject';

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
                    'ngInject';

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

module.exports = infoEditorService;
