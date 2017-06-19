(function () {
    angular
        .module('indigoeln')
        .directive('indigoPreferredCompoundDetails', indigoPreferredCompoundDetails);

    function indigoPreferredCompoundDetails() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/prefer-compound-details/prefer-compound-details.html',
            controller: 'IndigoPreferredCompoundDetailsController'
        };
    }
})();