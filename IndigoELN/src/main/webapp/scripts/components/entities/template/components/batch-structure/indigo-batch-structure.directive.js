(function() {
    angular
        .module('indigoeln')
        .directive('indigoBatchStructure', indigoBatchStructure);

    function indigoBatchStructure() {
        return {
            restrict: 'E',
            templateUrl: 'scripts/components/entities/template/components/batch-structure/batch-structure.html',
            scope: {
                share: '=',
                selectedBatch: '=',
                readonly: '='
            },
            controller: indigoBatchStructureController,
            controllerAs: 'vm',
            bindToController: true
        };
    }

    /* @ngInject */
    function indigoBatchStructureController($scope, $rootScope, EntitiesBrowser) {
        var vm = this;

        init();

        function init() {
            vm.onChangedStructure = onChangedStructure;
            bindEvents();
        }

        function bindEvents() {
            $scope.$on('batch-summary-row-selected', function(event, data) {
                vm.structure = _.get(data, 'row.structure');
            });
            $scope.$on('batch-summary-row-deselected', function() {
                vm.structure = null;
            });
        }

        function onChangedStructure(structure) {
            if (vm.share.selectedRow) {
                _.set(vm.share, 'selectedRow.structure', structure);
                $rootScope.$broadcast('product-batch-structure-changed', vm.share.selectedRow);
                EntitiesBrowser.setCurrentFormDirty();
            }
        }
    }
})();
