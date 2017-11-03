(function() {
    angular
        .module('indigoeln.componentsModule')
        .directive('indigoConceptDetails', indigoConceptDetails);

    function indigoConceptDetails() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/indigo-components/concept-details/concept-details.html',
            controller: 'SomethingDetailsController',
            bindToController: true,
            controllerAs: 'vm',
            scope: {
                componentData: '=',
                experiment: '=',
                isReadonly: '='
            }
        };
    }
})();
