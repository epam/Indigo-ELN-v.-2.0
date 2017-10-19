(function() {
    angular
        .module('indigoeln.Components')
        .directive('columnsSettings', columnsSettings);

    function columnsSettings() {
        return {
            restrict: 'E',
            templateUrl: 'scripts/indigo-components/common/table/columns-settings/columns-settings.html',
            scope: {
                columns: '=',
                visibleColumns: '=',
                onChanged: '&',
                resetColumns: '&'
            },
            controller: angular.noop,
            controllerAs: 'vm',
            bindToController: true
        };
    }
})();
