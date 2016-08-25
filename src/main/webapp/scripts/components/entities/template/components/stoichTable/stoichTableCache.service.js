angular.module('indigoeln')
    .factory('StoichTableCache', function () {
        var _stoichTable;
        return {
            getStoicTable: function () {
                return _stoichTable;
            },
            setStoicTable: function (table) {
                _stoichTable = table;
            }
        };
    })
;