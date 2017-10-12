(function() {
    angular
        .module('indigoeln')
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
                _.each(rows, function(row) {
                    if (!RegistrationUtil.isRegistered(row)) {
                        row.purity = angular.copy(result);
                    }
                });
            });
        }

        function editMeltingPoint(row) {
            InfoEditor.editMeltingPoint(row.meltingPoint, function(result) {
                row.meltingPoint = result;
            });
        }

        function editMeltingPointForAllRows(rows) {
            InfoEditor.editMeltingPoint({}, function(result) {
                _.each(rows, function(row) {
                    if (!RegistrationUtil.isRegistered(row)) {
                        row.meltingPoint = angular.copy(result);
                    }
                });
            });
        }

        function openProductBatchSummaryModal(rows, title) {
            $uibModal.open({
                templateUrl: 'scripts/components/entities/template/components/product-batch-summary/product-batch-summary-set-source.html',
                controller: 'ProductBatchSummarySetSourceController',
                controllerAs: 'vm',
                size: 'sm',
                resolve: {
                    name: function() {
                        return title;
                    }
                }
            }).result.then(function(result) {
                _.each(rows, function(row) {
                    if (!RegistrationUtil.isRegistered(row)) {
                        row.source = result.source;
                        row.sourceDetail = result.sourceDetail;
                    }
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
                _.each(rows, function(row) {
                    if (!RegistrationUtil.isRegistered(row)) {
                        row.externalSupplier = angular.copy(result);
                    }
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
                _.each(rows, function(row) {
                    if (!RegistrationUtil.isRegistered(row)) {
                        row.healthHazards = angular.copy(result);
                    }
                });
            });
        }

        function editResidualSolvents(rows) {
            var data = rows.length === 1 ? rows[0].residualSolvents : {};

            InfoEditor.editResidualSolvents(data).then(function(result) {
                _.forEach(rows, function(row) {
                    if (!RegistrationUtil.isRegistered(row)) {
                        row.residualSolvents = result;
                    }
                });
            });
        }

        function editSolubility(rows) {
            var data = rows.length === 1 ? rows[0].solubility : {};

            InfoEditor.editSolubility(data, function(result) {
                _.each(rows, function(row) {
                    if (!RegistrationUtil.isRegistered(row)) {
                        row.solubility = result;
                    }
                });
            });
        }

        function editStorageInstructions(rows) {
            var callback = function(result) {
                _.each(rows, function(row) {
                    if (!RegistrationUtil.isRegistered(row)) {
                        row.storageInstructions = result;
                    }
                });
            };
            var data = rows.length === 1 ? rows[0].storageInstructions : {};
            InfoEditor.editStorageInstructions(data || {}, callback);
        }

        function editHandlingPrecautions(rows) {
            var callback = function(result) {
                _.each(rows, function(row) {
                    if (!RegistrationUtil.isRegistered(row)) {
                        row.handlingPrecautions = result;
                    }
                });
            };
            var data = rows.length === 1 ? rows[0].handlingPrecautions : {};
            InfoEditor.editHandlingPrecautions(data || {}, callback);
        }
    }
})();
