(function() {
    angular
        .module('indigoeln.componentsModule')
        .directive('indigoExperimentDescription', indigoExperimentDescription);

    function indigoExperimentDescription() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/app/common/components/indigo-components/experiment-description/experiment-description.html',
            scope: {
                model: '=',
                experiment: '=',
                isReadonly: '=',
                onChanged: '&'
            },
            bindToController: true,
            controllerAs: 'vm',
            controller: angular.noop
        };
    }
})();
