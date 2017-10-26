angular
    .module('indigoeln')
    .factory('selectService', selectService);

/* @ngInject */
function selectService($uibModal, RegistrationUtil) {
    var setSelectValueAction = {
        action: action
    };

    return {
        processColumns: processColumns
    };

    function action(id) {
        var that = this;
        $uibModal.open({
            templateUrl: 'scripts/components/entities/template/components/common/table/select/set-select-value.html',
            controller: 'SetSelectValueController',
            controllerAs: 'vm',
            size: 'sm',
            resolve: {
                id: function() {
                  return id;
                },
                name: function() {
                    return that.title;
                },
                values: function() {
                    return that.values;
                },
                dictionary: function() {
                    return that.dictionary;
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
            if (column.type === 'select' && !column.hideSelectValue) {
                column.actions = (column.actions || [])
                    .concat([_.extend({}, setSelectValueAction, {
                        name: 'Set value for ' + column.name,
                        title: column.name,
                        values: column.values(),
                        rows: rows,
                        dictionary: column.dictionary
                    })]);
            }
        });
    }
}
