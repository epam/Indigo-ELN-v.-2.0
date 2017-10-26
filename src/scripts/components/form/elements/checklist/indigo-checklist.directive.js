(function() {
    angular
        .module('indigoeln')
        .directive('indigoChecklist', indigoChecklist);

    function indigoChecklist() {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                indigoItems: '=',
                indigoLabel: '@'
            },
            controller: controller,
            controllerAs: 'vm',
            bindToController: true,
            templateUrl: 'scripts/components/form/elements/checklist/checklist.html'
        };

        /* @ngInject */
        function controller() {
            var vm = this;
            vm.allItemsSelected = false;

            vm.allChanged = function() {
                _.each(vm.indigoItems, function(item) {
                    item.isChecked = vm.allItemsSelected;
                });
            };
        }
    }
})();
