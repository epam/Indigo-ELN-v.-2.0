angular
    .module('indigoeln.Components')
    .factory('scalarService', scalarService);

/* @ngInject */
function scalarService($uibModal, RegistrationUtil, CalculationService) {
    return {
        action: action
    };

    function action(rows, title, column) {
        $uibModal.open({
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
            _.each(rows, function(row) {
                if (!RegistrationUtil.isRegistered(row)) {
                    row[column.id].value = result;
                    row[column.id].entered = true;
                    if (column.id === 'saltEq') {
                        recalculateSalt(row);
                    }
                }
            });
        });
    }

    function recalculateSalt(reagent) {
        CalculationService.recalculateSalt(reagent).then(function() {
            CalculationService.recalculateStoich();
        });
    }
}
