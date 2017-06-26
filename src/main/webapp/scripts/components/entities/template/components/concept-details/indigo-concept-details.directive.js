(function() {
    angular
        .module('indigoeln')
        .directive('indigoConceptDetails', indigoConceptDetails);

    function indigoConceptDetails() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/concept-details/concept-details.html',
            controller: 'IndigoConceptDetailsController'
        };
    }
})();
