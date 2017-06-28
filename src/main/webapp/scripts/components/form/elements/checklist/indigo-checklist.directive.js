(function() {
    angular
        .module('indigoeln')
        .directive('indigoChecklist', indigoChecklist);

    //TODO This directive isn't used anywhere in the project
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
            vm.selectAll = selectAll;

            function selectAll() {
                for (var i = 0; i < vm.indigoItems.length; i++) {
                    vm.indigoItems[i].isChecked = vm.allItemsSelected;
                }
            }
        }
    }
})();
