(function() {
    angular
        .module('indigoeln.componentButtons')
        .directive('simpleCheckbox', addNewBatchDirective);

    /* @ngInject */
    function addNewBatchDirective() {
        return {
            restrict: 'E',
            scope: {model: '='},
            templateUrl: 'scripts/indigo-components/common/table/simple-checkbox/simple-checkbox.html'
        };
    }
})();
