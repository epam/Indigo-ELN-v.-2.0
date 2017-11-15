function stoichTableCacheService() {
    var stoichTable;

    return {
        getStoicTable: getStoicTable,
        setStoicTable: setStoicTable
    };

    function getStoicTable() {
        if (!stoichTable) {
            stoichTable = {
                reactants: [], products: null
            };
        }

        return stoichTable;
    }

    function setStoicTable(table) {
        stoichTable = table;
    }
}

module.exports = stoichTableCacheService;

