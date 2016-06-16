angular.module('indigoeln')
    .factory('StoichCalculator', function ($rootScope, NumberUtil, $http, AppValues) {
        var defaultBatch = AppValues.getDefaultBatch();
        var simpleValues = ['molWeight', 'saltEq', 'stoicPurity', 'eq'];

        var setDefaultValues = function (batches) {
            return _.map(batches, function (batch) {
                _.each(batch, function (value, key) {
                    if (_.isObject(value)) {
                        value.entered = value.entered || false;
                    } else if (!_.isObject(value) && _.contains(simpleValues, key)) {
                        batch[key] = {value: value, entered: false};
                    }
                });
                return _.defaults(batch, defaultBatch);
            });
        };

        var recalculateStoich = function (data) {
            var requestData = {
                stoicBatches: setDefaultValues(data.stoichTable.reactants),
                intendedProducts: setDefaultValues(data.stoichTable.products),
                actualProducts: setDefaultValues(data.actualProducts),
                changedBatch: _.defaults(data.row, defaultBatch),
                changedField: data.column
            };
            return $http.put('api/calculations/stoich/calculate', requestData).then(function (result) {
                $rootScope.$broadcast('product-batch-summary-recalculated', result.data.actualProducts);
                $rootScope.$broadcast('stoic-table-recalculated', result.data);
            });
        };

        return {
            recalculateStoich: recalculateStoich
        };
    });
