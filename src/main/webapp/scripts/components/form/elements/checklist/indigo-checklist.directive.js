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
        function controller($scope) {
            var vm = this;
            vm.allItemsSelected = false;

            $scope.$watch('vm.allItemsSelected', function(val, old) {
                if (old !== val) {
                    _.each(vm.indigoItems, function(item) {
                        item.isChecked = vm.allItemsSelected;
                    });
                }
            })
        }
    }
})();
