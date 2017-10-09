(function() {
    angular
        .module('indigoeln.componentButtons')
        .directive('simpleCheckbox', addNewBatchDirective);

    /* @ngInject */
    function addNewBatchDirective() {
        return {
            restrict: 'E',
            scope: false,
            templateUrl: 'scripts/components/entities/template/components/common/table/simple-checkbox/simple-checkbox.html'
        };
    }
})();
