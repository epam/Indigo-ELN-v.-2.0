(function() {
    angular
        .module('indigoeln')
        .directive('indigoReactionScheme', indigoReactionScheme);

    function indigoReactionScheme() {
        return {
            restrict: 'E',
            templateUrl: 'scripts/components/entities/template/components/reaction-scheme/reaction-scheme.html',
            controller: indigoReactionSchemeController,
            controllerAs: 'vm',
            bindToController: true
        };
    }

    /* @ngInject */
    function indigoReactionSchemeController($scope, EntitiesBrowser) {
        var vm = this;

        init();

        function init() {
            vm.onChangedStructure = onChangedStructure;
        }
        
        function onChangedStructure(structure) {
            if (_.isEqual($scope.model.reaction, structure)) {
                return;
            }
            $scope.model.reaction = structure;
            EntitiesBrowser.setCurrentFormDirty();
        }
    }
})();
