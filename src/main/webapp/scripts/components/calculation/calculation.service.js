angular.module('indigoeln')
    .factory('CalculationService', function ($rootScope, NumberUtil, $http, AppValues) {
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

        function getSaltConfig(reagent) {
            var saltCode = reagent.saltCode ? reagent.saltCode.value : null;
            var saltEq = reagent.saltEq ? reagent.saltEq.value : null;
            return {
                params: {
                    saltCode: saltCode && saltCode !== '0' ? saltCode : null,
                    saltEq: saltEq
                }
            };
        }

        var getMoleculeInfo = function (reagent, successCallback, failureCallback) {
            var config = _.isObject(reagent) ? getSaltConfig(reagent) : null;
            var data = reagent.structure ? reagent.structure.molfile : reagent;
            var url = 'api/calculations/molecule/info';
            if (successCallback) {
                $http.put(url, data, config)
                    .then(function (result) {
                        successCallback(result);
                    }, failureCallback);
            } else {
                return $http.put(url, data, config);
            }
        };

        var recalculateSalt = function (reagent, callback) {
            var config = getSaltConfig(reagent);
            $http.put('api/calculations/molecule/info', reagent.structure.molfile, config)
                .then(function (result) {
                    callback(reagent, result);
                });
        };

        var recalculateStoich = function (data) {
            var requestData = {
                stoicBatches: setDefaultValues(data.stoichTable.reactants),
                intendedProducts: setDefaultValues(data.stoichTable.products),
                actualProducts: setDefaultValues(data.actualProducts),
                changedBatch: null,
                changedField: null
            };
            return $http.put('api/calculations/stoich/calculate', requestData).then(function (result) {
                $rootScope.$broadcast('product-batch-summary-recalculated', result.data.actualProducts);
                $rootScope.$broadcast('stoic-table-recalculated', result.data);
            });
        };

        var recalculateStoichBasedOnBatch = function (data) {
            var requestData = {
                stoicBatches: setDefaultValues(data.stoichTable.reactants),
                intendedProducts: setDefaultValues(data.stoichTable.products),
                actualProducts: setDefaultValues(data.actualProducts),
                changedBatch: _.defaults(data.row, defaultBatch),
                changedField: data.column
            };
            return $http.put('api/calculations/stoich/calculate/batch', requestData).then(function (result) {
                $rootScope.$broadcast('product-batch-summary-recalculated', result.data.actualProducts);
                $rootScope.$broadcast('stoic-table-recalculated', result.data);
            });
        };

        return {
            getMoleculeInfo: getMoleculeInfo,
            recalculateSalt: recalculateSalt,
            recalculateStoich: recalculateStoich,
            recalculateStoichBasedOnBatch: recalculateStoichBasedOnBatch
        };
    });