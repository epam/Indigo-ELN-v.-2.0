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

var productBatchSummarySetSourceTemplate =
    require('../directives/product-batch-summary-set-source/product-batch-summary-set-source.html');

columnActions.$inject = ['infoEditorService', 'registrationUtil', '$uibModal'];

function columnActions(infoEditorService, registrationUtil, $uibModal) {
    return {
        editPurity: editPurity,
        editPurityForAllRows: editPurityForAllRows,
        editMeltingPoint: editMeltingPoint,
        editMeltingPointForAllRows: editMeltingPointForAllRows,
        openProductBatchSummaryModal: openProductBatchSummaryModal,
        editExternalSupplier: editExternalSupplier,
        editExternalSupplierForAllRows: editExternalSupplierForAllRows,
        editHealthHazards: editHealthHazards,
        editHealthHazardsForAllRows: editHealthHazardsForAllRows,
        editResidualSolvents: editResidualSolvents,
        editSolubility: editSolubility,
        editStorageInstructions: editStorageInstructions,
        editHandlingPrecautions: editHandlingPrecautions
    };

    function editPurity(row) {
        infoEditorService.editPurity(row.purity, function(result) {
            row.purity = result;
        });
    }

    function editPurityForAllRows(rows) {
        infoEditorService.editPurity({}, function(result) {
            iterateRegisterd(rows, function(row) {
                row.purity = angular.copy(result);
            });
        });
    }

    function iterateRegisterd(rows, iterator) {
        return _.forEach(rows, function(row) {
            if (!registrationUtil.isRegistered(row)) {
                iterator(row);
            }
        });
    }

    function editMeltingPoint(row) {
        infoEditorService.editMeltingPoint(row.meltingPoint, function(result) {
            row.meltingPoint = result;
        });
    }

    function editMeltingPointForAllRows(rows) {
        infoEditorService.editMeltingPoint({}, function(result) {
            iterateRegisterd(rows, function(row) {
                row.meltingPoint = angular.copy(result);
            });
        });
    }

    function openProductBatchSummaryModal(rows, title) {
        $uibModal.open({
            template: productBatchSummarySetSourceTemplate,
            controller: 'ProductBatchSummarySetSourceController',
            controllerAs: 'vm',
            size: 'sm',
            resolve: {
                name: function() {
                    return title;
                }
            }
        }).result.then(function(result) {
            iterateRegisterd(rows, function(row) {
                row.source = result.source;
                row.sourceDetail = result.sourceDetail;
            });
        });
    }

    function editExternalSupplier(row) {
        infoEditorService.editExternalSupplier(row.externalSupplier, function(result) {
            row.externalSupplier = result;
        });
    }

    function editExternalSupplierForAllRows(rows) {
        infoEditorService.editExternalSupplier({}, function(result) {
            iterateRegisterd(rows, function(row) {
                row.externalSupplier = angular.copy(result);
            });
        });
    }

    function editHealthHazards(row) {
        infoEditorService.editHealthHazards(row.healthHazards, function(result) {
            row.healthHazards = result;
        });
    }

    function editHealthHazardsForAllRows(rows) {
        infoEditorService.editHealthHazards({}, function(result) {
            iterateRegisterd(rows, function(row) {
                row.healthHazards = angular.copy(result);
            });
        });
    }

    function editResidualSolvents(rows) {
        var data = rows.length === 1 ? rows[0].residualSolvents : {};

        infoEditorService.editResidualSolvents(data).then(function(result) {
            iterateRegisterd(rows, function(row) {
                row.residualSolvents = angular.copy(result);
            });
        });
    }

    function editSolubility(rows) {
        var data = rows.length === 1 ? rows[0].solubility : {};

        infoEditorService.editSolubility(data, function(result) {
            iterateRegisterd(rows, function(row) {
                row.solubility = angular.copy(result);
            });
        });
    }

    function editStorageInstructions(rows) {
        var callback = function(result) {
            iterateRegisterd(rows, function(row) {
                row.storageInstructions = angular.copy(result);
            });
        };
        var data = rows.length === 1 ? rows[0].storageInstructions : {};
        infoEditorService.editStorageInstructions(data || {}, callback);
    }

    function editHandlingPrecautions(rows) {
        var callback = function(result) {
            iterateRegisterd(rows, function(row) {
                row.handlingPrecautions = angular.copy(result);
            });
        };
        var data = rows.length === 1 ? rows[0].handlingPrecautions : {};
        infoEditorService.editHandlingPrecautions(data || {}, callback);
    }
}

module.exports = columnActions;
