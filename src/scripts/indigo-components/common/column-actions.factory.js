(function() {
    angular
        .module('indigoeln.Components')
        .factory('columnActions', columnActions);

    columnActions.$inject = ['InfoEditor', 'RegistrationUtil', '$uibModal'];

    function columnActions(InfoEditor, RegistrationUtil, $uibModal) {
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
            InfoEditor.editPurity(row.purity, function(result) {
                row.purity = result;
            });
        }

        function editPurityForAllRows(rows) {
            InfoEditor.editPurity({}, function(result) {
                iterateRegisterd(rows, function(row) {
                    row.purity = angular.copy(result);
                });
            });
        }

        function iterateRegisterd(rows, iterator) {
            return _.forEach(rows, function(row) {
                if (!RegistrationUtil.isRegistered(row)) {
                    iterator(row);
                }
            });
        }

        function editMeltingPoint(row) {
            InfoEditor.editMeltingPoint(row.meltingPoint, function(result) {
                row.meltingPoint = result;
            });
        }

        function editMeltingPointForAllRows(rows) {
            InfoEditor.editMeltingPoint({}, function(result) {
                iterateRegisterd(rows, function(row) {
                    row.meltingPoint = angular.copy(result);
                });
            });
        }

        function openProductBatchSummaryModal(rows, title) {
            $uibModal.open({
                templateUrl: 'scripts/indigo-components/product-batch-summary/product-batch-summary-set-source.html',
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
            InfoEditor.editExternalSupplier(row.externalSupplier, function(result) {
                row.externalSupplier = result;
            });
        }

        function editExternalSupplierForAllRows(rows) {
            InfoEditor.editExternalSupplier({}, function(result) {
                iterateRegisterd(rows, function(row) {
                    row.externalSupplier = angular.copy(result);
                });
            });
        }

        function editHealthHazards(row) {
            InfoEditor.editHealthHazards(row.healthHazards, function(result) {
                row.healthHazards = result;
            });
        }

        function editHealthHazardsForAllRows(rows) {
            InfoEditor.editHealthHazards({}, function(result) {
                iterateRegisterd(rows, function(row) {
                    row.healthHazards = angular.copy(result);
                });
            });
        }

        function editResidualSolvents(rows) {
            var data = rows.length === 1 ? rows[0].residualSolvents : {};

            InfoEditor.editResidualSolvents(data).then(function(result) {
                iterateRegisterd(rows, function(row) {
                    row.residualSolvents = result;
                });
            });
        }

        function editSolubility(rows) {
            var data = rows.length === 1 ? rows[0].solubility : {};

            InfoEditor.editSolubility(data, function(result) {
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
            InfoEditor.editStorageInstructions(data || {}, callback);
        }

        function editHandlingPrecautions(rows) {
            var callback = function(result) {
                iterateRegisterd(rows, function(row) {
                    row.handlingPrecautions = result;
                });
            };
            var data = rows.length === 1 ? rows[0].handlingPrecautions : {};
            InfoEditor.editHandlingPrecautions(data || {}, callback);
        }
    }
})();
