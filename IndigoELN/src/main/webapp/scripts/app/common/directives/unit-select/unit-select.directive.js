(function() {
    angular
        .module('indigoeln')
        .directive('unitSelect', unitSelectDirective);

    function unitSelectDirective() {
        return {
            restrict: 'E',
            templateUrl: 'scripts/app/common/directives/unit-select/unit-select.html',
            scope: {
                unit: '=',
                units: '=',
                onChange: '&'
            },
            controller: UnitSelectController,
            controllerAs: 'vm',
            bindToController: true
        };

        function UnitSelectController() {
            var vm = this;

            init();

            function init() {
                vm.isOpen = false;
            }
        }
    }
})();
