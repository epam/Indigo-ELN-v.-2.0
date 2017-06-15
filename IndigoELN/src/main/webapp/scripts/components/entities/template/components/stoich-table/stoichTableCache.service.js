angular.module('indigoeln')
    .factory('StoichTableCache', function () {
        var _stoichTable;
        return {
            getStoicTable: function () {
                if (!_stoichTable) _stoichTable = {"reactants":[],"products":null}
                return _stoichTable;
            },
            setStoicTable: function (table) {
                _stoichTable = table;
            }
        };
    })
;