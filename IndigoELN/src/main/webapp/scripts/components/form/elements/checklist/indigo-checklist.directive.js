(function () {
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
            templateUrl: 'scripts/components/form/elements/checklist/checklist.html'
        };

        /* @ngInject */
        function controller($scope) {
            $scope.allItemsSelected = false;
            $scope.selectAll = function () {
                for (var i = 0; i < $scope.indigoItems.length; i++) {
                    $scope.indigoItems[i].isChecked = $scope.allItemsSelected;
                }
            };
        }
    }
})();
