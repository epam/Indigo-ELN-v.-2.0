angular.module('indigoeln')
    .factory('scalarService', function ($uibModal, RegistrationUtil, CalculationService, StoichTableCache, ProductBatchSummaryCache) {
        function initDataForCalculation(data) {
            var calcData = data || {};
            calcData.stoichTable = StoichTableCache.getStoicTable();
            calcData.actualProducts = ProductBatchSummaryCache.getProductBatchSummary();
            return calcData;
        }
        var recalculateSalt = function (reagent) {
            function callback(result) {
                var data = result.data;
                data.mySaltEq = reagent.saltEq;
                data.mySaltCode = reagent.saltCode;
                reagent.molWeight = reagent.molWeight || {};
                reagent.molWeight.value = data.molecularWeight;
                reagent.formula = CalculationService.getSaltFormula(data);
                reagent.lastUpdatedType = 'weight';
                CalculationService.recalculateStoich(initDataForCalculation());
            }
            CalculationService.recalculateSalt(reagent, callback);
        };
        var setScalarValueAction = {

            action: function (id) {
                var that = this;
                $uibModal.open({
                    templateUrl: 'scripts/components/entities/template/components/common/table/scalar/set-scalar-value.html',
                    controller: 'SetScalarValueController',
                    size: 'sm',
                    resolve: {
                        name: function () {
                            return that.title;
                        }
                    }
                }).result.then(function (result) {
                    _.each(that.rows, function (row) {
                        if (!RegistrationUtil.isRegistered(row)) {
                            row[id].value = result;
                            row[id].entered = true;
                            if (id === 'saltEq') {
                                recalculateSalt(row);
                            }
                        }
                    });

                }, function () {

                });
            }

        };
        return {
            processColumns: function (columns, rows) {
                _.each(columns, function (column) {
                    if (column.type === 'scalar' && column.bulkAssignment) {
                        column.actions = (column.actions || [])
                            .concat([_.extend({}, setScalarValueAction, {
                                name: 'Set value for ' + column.name,
                                title: column.name,
                                rows: rows
                            })]);
                    }
                });
            }
        };
    });