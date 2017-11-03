(function() {
    angular
        .module('indigoeln.componentsModule')
        .directive('indigoStoichTable', indigoStoichTable);

    function indigoStoichTable() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/app/common/components/indigo-components/stoich-table/stoich-table.html',
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
