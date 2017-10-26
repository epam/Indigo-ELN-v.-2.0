(function() {
    angular
        .module('indigoeln.Components')
        .directive('indigoSearchResultTable', indigoSearchResultTable);

    function indigoSearchResultTable() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/indigo-components/common/search-result-table/search-result-table.html',
            scope: {
                indigoTableContent: '=',
                indigoTableFilter: '=',
                indigoEditableInfo: '=',
                indigoSingleItemPerTab: '=',
                indigoTab: '=',
                indigoSelectedItemsPerTab: '=',
                onChangeSelectedItems: '&'
            },
            controller: 'IndigoSearchResultTableController'
        };
    }
})();
