angular
    .module('indigoeln')
    .factory('CalculationService', calculationService);

/* @ngInject */
function calculationService($rootScope, $http, $q, AppValues,
                            StoichTableCache, ProductBatchSummaryCache, capitalizeFilter) {
    var defaultBatch = AppValues.getDefaultBatch();
    var recalculatingStoich = false;

    return {
        createBatch: createBatch,
        getMoleculeInfo: getMoleculeInfo,
        getImageForStructure: getImageForStructure,
        getSaltFormula: getSaltFormula,
        setEntered: setEntered,
        findLimiting: findLimiting,
        isMoleculesEqual: isMoleculesEqual,
        combineReactionComponents: combineReactionComponents,
        resetValuesToDefault: resetValuesToDefault,
        setValuesReadonly: setValuesReadonly,
        setValuesEditable: setValuesEditable,
        calculateProductBatch: calculateProductBatch,
        recalculateSalt: recalculateSalt,
        recalculateAmounts: recalculateAmounts,
        recalculateStoich: recalculateStoich,
        recalculateStoichBasedOnBatch: recalculateStoichBasedOnBatch
    };

    function initDataForStoichCalculation(data) {
        var calcData = data || {};
        calcData.stoichTable = StoichTableCache.getStoicTable();
        calcData.actualProducts = ProductBatchSummaryCache.getProductBatchSummary();

        return calcData;
    }


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

    function setDefaultValues(batches) {
        var simpleValues = ['molWeight', 'saltEq', 'stoicPurity', 'eq'];

        if (_.isArray(batches)) {
            return _.map(batches, function(batch) {
                _.each(batch, function(value, key) {
                    if (_.isObject(value)) {
                        value.entered = value.entered || false;
                    } else if (!_.isObject(value) && _.includes(simpleValues, key)) {
                        batch[key] = {
                            value: value, entered: false
                        };
                    } else if (_.isNull(value)) {
                        // because _.defaults omits nulls
                        batch[key] = undefined;
                    }
                });

                return _.defaults(batch, defaultBatch);
            });
        } else if (_.isObject(batches)) {
            _.each(batches, function(value, key) {
                if (_.isObject(value)) {
                    value.entered = value.entered || false;
                } else if (!_.isObject(value) && _.includes(simpleValues, key)) {
                    batches[key] = {
                        value: value, entered: false
                    };
                } else if (_.isNull(value)) {
                    // because _.defaults omits nulls
                    batches[key] = undefined;
                }
            });

            return _.defaults(batches, defaultBatch);
        }
    }

    function getMoleculeInfo(reagent, successCallback, failureCallback) {
        var config = _.isObject(reagent) ? getSaltConfig(reagent) : null;
        var data = reagent.structure ? reagent.structure.molfile : reagent;
        var url = 'api/calculations/molecule/info';
        if (successCallback) {
            $http.put(url, data, config)
                .then(function(result) {
                    successCallback(result);
                }, failureCallback);
        } else {
            return $http.put(url, data, config);
        }
    }

    function getImageForStructure(molfile, type, callback) {
        return $http.post('api/renderer/' + type + '/image', molfile).then(function(response) {
            if (callback) {
                callback(response.data.image);
            }

            return response.data.image;
        });
    }

    function findLimiting(stoichTable) {
        return _.find(stoichTable.reactants, {
            'limiting': true
        });
    }

    function createBatch(stoichTable, isProduct) {
        var batch = AppValues.getDefaultBatch();
        var limiting = findLimiting(stoichTable);
        var property = isProduct ? 'theoMoles' : 'mol';
        if (limiting) {
            batch[property] = angular.copy(limiting.mol);
            batch[property].entered = false;
        }

        return batch;
    }

    function recalculateSalt(reagent) {
        if (reagent.structure && reagent.structure.molfile) {
            var config = getSaltConfig(reagent);

            return $http.put('api/calculations/molecule/info', reagent.structure.molfile, config)
                .then(function(result) {
                    var data = result.data;
                    data.mySaltEq = reagent.saltEq;
                    data.mySaltCode = reagent.saltCode;
                    reagent.molWeight = reagent.molWeight || {};
                    reagent.molWeight.value = data.molecularWeight;
                    reagent.formula = getSaltFormula(data);
                    // for product batch summary
                    reagent.lastUpdatedType = 'weight';

                    return reagent;
                });
        }

        return $q.resolve(reagent);
    }


    function recalculateStoich(calcData) {
        if (recalculatingStoich) {
            return;
        }
        recalculatingStoich = true;
        var data = initDataForStoichCalculation(calcData);
        var requestData = {
            stoicBatches: setDefaultValues(data.stoichTable.reactants),
            intendedProducts: setDefaultValues(data.stoichTable.products),
            actualProducts: setDefaultValues(data.actualProducts)
        };

        return $http.put('api/calculations/stoich/calculate', requestData).then(function(result) {
            $rootScope.$broadcast('product-batch-summary-recalculated', result.data.actualProducts);
            $rootScope.$broadcast('stoic-table-recalculated', result.data);
            recalculatingStoich = false;
        });
    }

    function recalculateStoichBasedOnBatch(calcData) {
        var data = initDataForStoichCalculation(calcData);
        var requestData = {
            stoicBatches: setDefaultValues(data.stoichTable.reactants),
            intendedProducts: setDefaultValues(data.stoichTable.products),
            actualProducts: setDefaultValues(data.actualProducts),
            changedBatchRowNumber: _.indexOf(data.stoichTable.reactants, data.row),
            changedField: data.changedField || data.column,
            molWeightChanged: data.molWeightChanged
        };

        return $http.put('api/calculations/stoich/calculate/batch', requestData).then(function(result) {
            $rootScope.$broadcast('product-batch-summary-recalculated', result.data.actualProducts);
            $rootScope.$broadcast('stoic-table-recalculated', result.data);
        });
    }

    function recalculateAmounts(data, callback) {
        var requestData = {
            productBatch: setDefaultValues(data.row)
        };

        return $http.put('api/calculations/product/calculate/batch/amounts', requestData).then(function(result) {
            if (callback) {
                callback(result);
            } else {
                _.extend(data.row, result.data);
            }
        });
    }

    function calculateProductBatch(data) {
        var requestData = {
            productBatch: setDefaultValues(data.row),
            changedField: data.column
        };

        return $http.put('api/calculations/product/calculate/batch', requestData).then(function(result) {
            result.data.yield = Math.round(result.data.yield);
            _.extend(data.row, result.data);
        });
    }

    function setEntered(data) {
        var simpleValues = ['molWeight', 'saltEq', 'stoicPurity', 'eq'];
        if (_.isObject(data.row[data.column])) {
            data.row[data.column].entered = true;
        } else if (!_.isObject(data.row[data.column]) && _.includes(simpleValues, data.column)) {
            data.row[data.column] = {
                value: data.row[data.column], entered: true
            };
        }
    }

    function resetValuesToDefault(values, batch) {
        var defaultBatch = AppValues.getDefaultBatch();
        _.each(values, function(value) {
            batch[value] = angular.copy(defaultBatch[value]);
        });
    }

    function setValuesReadonly(values, batch) {
        _.each(values, function(value) {
            batch[value].readonly = true;
        });
    }

    function setValuesEditable(values, batch) {
        _.each(values, function(value) {
            batch[value].readonly = false;
        });
    }

    function isMoleculesEqual(molecule1, molecule2) {
        return $http.put('api/calculations/molecule/equals', [molecule1, molecule2]);
    }

    function getSaltFormula(data) {
        var saltEqPart = '';
        var descriptionPart = '';
        var formulaPart = data.molecularFormula;
        if (data.mySaltEq && data.mySaltEq.value) {
            saltEqPart = '*' + data.mySaltEq.value;
        } else if (!data.mySaltEq || data.mySaltEq.value === 0) {
            return formulaPart;
        }
        if (data.mySaltCode && data.mySaltCode.name !== AppValues.getDefaultSaltCode().name) {
            var saltName = data.mySaltCode.name.split('-')[1] || data.mySaltCode.name;
            descriptionPart = '(' + capitalizeFilter(saltName.trim()) + ')';
        } else if (data.mySaltCode && data.mySaltCode.name === AppValues.getDefaultSaltCode().name) {
            return formulaPart;
        }

        return formulaPart + saltEqPart + descriptionPart;
    }

    function combineReactionComponents(reactants, products) {
        var requestData = {
            reactants: reactants,
            products: products
        };

        return $http.put('api/calculations/reaction/combine', requestData);
    }
}
