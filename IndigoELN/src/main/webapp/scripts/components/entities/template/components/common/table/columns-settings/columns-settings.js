(function() {
    angular
        .module('indigoeln')
        .directive('columnsSettings', columnsSettings);

    function columnsSettings() {
        return {
            restrict: 'E',
            templateUrl: 'scripts/components/entities/template/components/common/table/columns-settings/columns-settings.html',
            scope: {
                columns: '=',
                saveInLocalStorage: '&',
                resetColumns: '&'
            },
            controller: angular.noop,
            controllerAs: 'vm',
            bindToController: true
        };
    }
})();
