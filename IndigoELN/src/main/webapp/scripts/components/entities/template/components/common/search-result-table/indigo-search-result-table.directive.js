(function () {
    angular
        .module('indigoeln')
        .directive('indigoSearchResultTable', indigoSearchResultTable);

    function indigoSearchResultTable() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/common/search-result-table/search-result-table.html',
            scope: {
                indigoTableContent: '=',
                indigoTableFilter: '=',
                indigoEditableInfo: '=',
                indigoSingleItemPerTab: '=',
                indigoTab: '=',
                indigoSelectedItemsPerTab: '='
            },
            controller: 'IndigoSearchResultTableController'
        };
    }
})();