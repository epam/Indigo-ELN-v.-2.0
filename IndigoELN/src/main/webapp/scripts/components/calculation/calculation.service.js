angular.module('indigoeln')
    .factory('CalculationService', function ($rootScope, NumberUtil, $http, AppValues, $log) {
        var defaultBatch = AppValues.getDefaultBatch();
        var simpleValues = ['molWeight', 'saltEq', 'stoicPurity', 'eq'];

        var setDefaultValues = function (batches) {
            if (_.isArray(batches)) {
                return _.map(batches, function (batch) {
                    _.each(batch, function (value, key) {
                        if (_.isObject(value)) {
                            value.entered = value.entered || false;
                        } else if (!_.isObject(value) && _.contains(simpleValues, key)) {
                            // TODO this can be deleted after database drop
                            batch[key] = {value: value, entered: false};
                        } else if (_.isNull(value)) {
                            batch[key] = undefined; // because _.defaults omits nulls
                        }
                    });
                    return _.defaults(batch, defaultBatch);
                });
            } else if (_.isObject(batches)) {
                _.each(batches, function (value, key) {
                    if (_.isObject(value)) {
                        value.entered = value.entered || false;
                    } else if (!_.isObject(value) && _.contains(simpleValues, key)) {
                        // TODO this can be deleted after database drop
                        batches[key] = {value: value, entered: false};
                    } else if (_.isNull(value)) {
                        batches[key] = undefined; // because _.defaults omits nulls
                    }
                });
                return _.defaults(batches, defaultBatch);
            }
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

        var getImageForStructure = function (molfile, type, callback) {
            $http({
                url: 'api/renderer/' + type + '/image',
                method: 'POST',
                data: molfile
            }).success(function (result) {
                callback(result.image);
            });
        };

        function findLimiting(stoichTable) {
            return _.findWhere(stoichTable.reactants, {limiting: true});
        }

        var createBatch = function (stoichTable, isProduct) {
            var batch = angular.copy(AppValues.getDefaultBatch());
            var limiting = findLimiting(stoichTable);
            var property = isProduct ? 'theoMoles' : 'mol';
            if (limiting) {
                batch[property] = angular.copy(limiting.mol);
                batch[property].entered = false;
            }
            return batch;
        };

        var recalculateSalt = function (reagent, callback) {
            if (reagent.structure && reagent.structure.molfile) {
                var config = getSaltConfig(reagent);
                $http.put('api/calculations/molecule/info', reagent.structure.molfile, config)
                    .then(function (result) {
                        callback(result);
                    });
            }
        };

        var recalculateStoich = function (data) {
            var requestData = {
                stoicBatches: setDefaultValues(data.stoichTable.reactants),
                intendedProducts: setDefaultValues(data.stoichTable.products),
                actualProducts: setDefaultValues(data.actualProducts)
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
                changedBatchRowNumber: _.indexOf(data.stoichTable.reactants, data.row),
                changedField: data.column
            };
            return $http.put('api/calculations/stoich/calculate/batch', requestData).then(function (result) {
                $rootScope.$broadcast('product-batch-summary-recalculated', result.data.actualProducts);
                $rootScope.$broadcast('stoic-table-recalculated', result.data);
            });
        };

        var calculateProductBatch = function (data) {
            var requestData = {
                productBatch: setDefaultValues(data.row),
                changedField: data.column
            };
            return $http.put('api/calculations/product/calculate/batch', requestData).then(function (result) {
                _.extend(data.row, result.data);
                $log.log(result);
            });
        };

        var setEntered = function (data) {
            var simpleValues = ['molWeight', 'saltEq', 'stoicPurity', 'eq'];
            if (_.isObject(data.row[data.column])) {
                data.row[data.column].entered = true;
            } else if (!_.isObject(data.row[data.column]) && _.contains(simpleValues, data.column)) {
                data.row[data.column] = {value: data.row[data.column], entered: true};
            }
        };

        var resetValuesToDefault = function (values, batch) {
            var defaultBatch = AppValues.getDefaultBatch();
            _.each(values, function (value) {
                batch[value] = angular.copy(defaultBatch[value]);
            });
        };

        var setValuesReadonly = function (values, batch) {
            _.each(values, function (value) {
                batch[value].readonly = true;
            });
        };

        var setValuesEditable = function (values, batch) {
            _.each(values, function (value) {
                batch[value].readonly = false;
            });
        };

        var isMoleculesEqual = function (molecule1, molecule2) {
            return $http.put('api/calculations/molecule/equals', [molecule1, molecule2]);
        };


        return {
            createBatch: createBatch,
            getMoleculeInfo: getMoleculeInfo,
            getImageForStructure: getImageForStructure,
            setEntered: setEntered,
            isMoleculesEqual: isMoleculesEqual,
            resetValuesToDefault: resetValuesToDefault,
            setValuesReadonly: setValuesReadonly,
            setValuesEditable: setValuesEditable,
            calculateProductBatch: calculateProductBatch,
            recalculateSalt: recalculateSalt,
            recalculateStoich: recalculateStoich,
            recalculateStoichBasedOnBatch: recalculateStoichBasedOnBatch
        };
    });
