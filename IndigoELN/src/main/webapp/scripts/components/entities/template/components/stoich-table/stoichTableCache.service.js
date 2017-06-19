angular
    .module('indigoeln')
    .factory('StoichTableCache', stoichTableCache);

/* @ngInject */
function stoichTableCache() {
    var _stoichTable;

    return {
        getStoicTable: getStoicTable,
        setStoicTable: setStoicTable
    };

    function getStoicTable() {
        if (!_stoichTable) {
            _stoichTable = {"reactants": [], "products": null};
        }
        return _stoichTable;
    }

    function setStoicTable(table) {
        _stoichTable = table;
    }

}

