angular.module('indigoeln')
    .factory('StoichCalculator', function ($rootScope, NumberUtil, $http) {
        var defaultBatch = {
            limiting: false,
            weight: {value: 0, unit: ''},
            volume: {value: 0, unit: ''},
            density: {value: 0, unit: ''},
            molarity: {value: 0, unit: ''},
            mol: {value: 0, unit: ''},
            loadFactor: {value: 0, unit: ''},
            theoWeight: {value: 0, unit: ''},
            theoMoles: {value: 0, unit: ''},
            rxnRole: {name: 'REACTANT'},
            saltCode: {name: '00 - Parent Structure', value: '0'},
            saltEq: 0,
            molWeight: 0,
            stoicPurity: 100,
            eq: 1,
            yield: 0
        };

        function setDefaultValues(batches) {
            return _.map(batches, function (batch) {
                return _.defaults(batch, defaultBatch);
            });
        }

        var recalculateStoich = function (data) {
            var requestData = {
                stoicBatches: setDefaultValues(data.stoichTable.reactants),
                intendedProducts: setDefaultValues(data.stoichTable.products),
                actualProducts: setDefaultValues(data.actualProducts),
                changedBatch: _.defaults(data.row, defaultBatch)
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
