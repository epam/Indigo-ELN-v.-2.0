angular
    .module('indigoeln.Components')
    .factory('scalarService', scalarService);

/* @ngInject */
function scalarService($uibModal, RegistrationUtil, CalculationService) {
    return {
        action: action
    };

    function action(rows, title, column) {
        return $uibModal.open({
            templateUrl: 'scripts/indigo-components/common/table/scalar/set-scalar-value.html',
            controller: 'SetScalarValueController',
            controllerAs: 'vm',
            size: 'sm',
            resolve: {
                name: function() {
                    return title;
                }
            }
        }).result.then(function(result) {
            return _.reduce(rows, function(array, row) {
                if (!RegistrationUtil.isRegistered(row)) {
                    row[column.id].value = result;
                    row[column.id].entered = true;
                    if (column.id === 'saltEq') {
                        array.push(CalculationService.recalculateSalt(row));
                    }
                }

                return array;
            }, []);
        });
    }
}
