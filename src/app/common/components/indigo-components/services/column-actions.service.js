var productBatchSummarySetSourceTemplate =
    require('../directives/product-batch-summary-set-source/product-batch-summary-set-source.html');

columnActionsService.$inject = ['infoEditorService', 'registrationUtilService', '$uibModal'];

function columnActionsService(infoEditorService, registrationUtilService, $uibModal) {
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
            if (!registrationUtilService.isRegistered(row)) {
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
                row.residualSolvents = result;
            });
        });
    }

    function editSolubility(rows) {
        var data = rows.length === 1 ? rows[0].solubility : {};

        infoEditorService.editSolubility(data, function(result) {
            iterateRegisterd(rows, function(row) {
                row.solubility = result;
            });
        });
    }

    function editStorageInstructions(rows) {
        var callback = function(result) {
            iterateRegisterd(rows, function(row) {
                row.storageInstructions = result;
            });
        };
        var data = rows.length === 1 ? rows[0].storageInstructions : {};
        infoEditorService.editStorageInstructions(data || {}, callback);
    }

    function editHandlingPrecautions(rows) {
        var callback = function(result) {
            iterateRegisterd(rows, function(row) {
                row.handlingPrecautions = result;
            });
        };
        var data = rows.length === 1 ? rows[0].handlingPrecautions : {};
        infoEditorService.editHandlingPrecautions(data || {}, callback);
    }
}

module.exports = columnActionsService;
