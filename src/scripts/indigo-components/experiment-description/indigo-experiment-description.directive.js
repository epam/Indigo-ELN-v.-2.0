(function() {
    angular
        .module('indigoeln.Components')
        .directive('indigoExperimentDescription', indigoExperimentDescription);

    function indigoExperimentDescription() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/indigo-components/experiment-description/experiment-description.html',
            scope: {
                model: '=',
                experiment: '=',
                isReadonly: '=',
                onChanged: '&'
            },
            bindToController: true,
            controllerAs: 'vm',
            controller: function() {
            }
        };
    }
})();
