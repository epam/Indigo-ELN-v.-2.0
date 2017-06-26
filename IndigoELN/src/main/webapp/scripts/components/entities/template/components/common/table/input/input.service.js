angular
    .module('indigoeln')
    .factory('inputService', inputService);

/* @ngInject */
function inputService($uibModal, RegistrationUtil) {
    var setInputValueAction = {
        action: action
    };

    return {
        processColumns: processColumns
    };

    function action(id) {
        var that = this;
        $uibModal.open({
            templateUrl: 'scripts/components/entities/template/components/common/table/input/set-input-value.html',
            controller: 'SetInputValueController',
            size: 'sm',
            resolve: {
                name: function() {
                    return that.title;
                }
            }
        }).result.then(function(result) {
            _.each(that.rows, function(row) {
                if (!RegistrationUtil.isRegistered(row)) {
                    row[id] = result;
                }
            });
        }, function() {

        });
    }

    function processColumns(columns, rows) {
        _.each(columns, function(column) {
            if (column.type === 'input' && column.bulkAssignment) {
                column.actions = (column.actions || [])
                    .concat([_.extend({}, setInputValueAction, {
                        name: 'Set value for ' + column.name,
                        title: column.name,
                        rows: rows
                    })]);
            }
        });
    }
}
