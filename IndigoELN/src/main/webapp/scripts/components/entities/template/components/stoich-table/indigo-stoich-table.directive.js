(function() {
    angular
        .module('indigoeln')
        .directive('indigoStoichTable', indigoStoichTable);

    function indigoStoichTable() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/stoich-table/stoich-table.html',
            controller: 'IndigoStoichTableController',
            controllerAs: 'vm',
            bindToController: true,
            scope: {
                model: '=',
                experiment: '=',
                isReadonly: '=',
                infoReactants: '=',
                infoProducts: '=',
                onChangedReactants: '&',
                onChangedProducts: '&',
                onPrecursorsChanged: '&',
                onChanged: '&'
            }
        };
    }
})();
