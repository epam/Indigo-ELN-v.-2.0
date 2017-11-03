(function() {
    angular
        .module('indigoeln.componentsModule')
        .directive('columnsSettings', columnsSettings);

    function columnsSettings() {
        return {
            restrict: 'E',
            templateUrl: 'scripts/app/common/components/indigo-components/common/table/columns-settings/columns-settings.html',
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
