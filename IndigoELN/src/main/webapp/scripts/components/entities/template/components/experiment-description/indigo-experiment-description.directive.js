(function () {
    angular
        .module('indigoeln')
        .directive('indigoExperimentDescription', indigoExperimentDescription);

    function indigoExperimentDescription() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/experiment-description/experiment-description.html'
        };
    }
})();
