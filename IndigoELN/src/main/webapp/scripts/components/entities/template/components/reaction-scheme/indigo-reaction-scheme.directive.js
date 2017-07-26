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
            bindToController: true,
            scope: {
                model: '=',
                experiment: '=',
                isReadonly: '='
            }
        };
    }

    /* @ngInject */
    function indigoReactionSchemeController($scope, $rootScope, EntitiesBrowser) {
        var vm = this;

        init();

        function init() {
            vm.onChangedStructure = onChangedStructure;

            bindEvents();
        }

        function bindEvents() {
            $scope.$on('new-reaction-scheme', function(event, data) {
                vm.model.reaction.image = data.image;
                vm.model.reaction.molfile = data.molfile;
            });
        }

        function onChangedStructure(structure) {
            vm.model.reaction = structure;
            EntitiesBrowser.setCurrentFormDirty();
            $rootScope.$broadcast('REACTION_CHANGED', structure);
        }
    }
})();
