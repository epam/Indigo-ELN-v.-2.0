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
            controller: IndigoChecklistController,
            controllerAs: 'vm',
            bindToController: true,
            templateUrl: 'scripts/app/common/directives/indigo-form-elements/elements/checklist/checklist.html'
        };

        /* @ngInject */
        function IndigoChecklistController() {
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
