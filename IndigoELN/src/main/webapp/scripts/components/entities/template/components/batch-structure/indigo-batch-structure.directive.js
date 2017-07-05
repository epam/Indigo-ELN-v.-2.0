(function() {
    angular
        .module('indigoeln')
        .directive('indigoBatchStructure', indigoBatchStructure);

    function indigoBatchStructure() {
        return {
            restrict: 'E',
            templateUrl: 'scripts/components/entities/template/components/batch-structure/batch-structure.html',
            controller: indigoBatchStructureController,
            controllerAs: 'vm',
            bindToController: true
        };
    }

    /* @ngInject */
    function indigoBatchStructureController($scope, EntitiesBrowser) {
        var vm = this;

        init();

        function init() {
            vm.onChangedStructure = onChangedStructure;
        }

        function onChangedStructure(structure) {
            if (_.isEqual($scope.model.molecule, structure)) {
                return;
            }

            $scope.model.molecule = structure;

            _.set($scope.share, 'selectedRow.structure', structure);

            EntitiesBrowser.setCurrentFormDirty();
        }
    }
})();
