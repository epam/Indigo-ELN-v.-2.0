angular
    .module('indigoeln')
    .factory('scalarService', scalarService);

/* @ngInject */
function scalarService($uibModal, RegistrationUtil, CalculationService) {

    var setScalarValueAction = {
        action: action
    };

    return {
        processColumns: processColumns
    };

    function action(id) {
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

    function recalculateSalt(reagent) {
        CalculationService.recalculateSalt(reagent).then(function () {
            CalculationService.recalculateStoich();
        });
    }

    function processColumns(columns, rows) {
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
}
