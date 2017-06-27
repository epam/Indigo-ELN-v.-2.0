(function() {
    angular
        .module('indigoeln')
        .directive('indigoReactionDetails', indigoReactionDetails);

    function indigoReactionDetails() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/reaction-details/reaction-details.html',
            controller: 'IndigoReactionDetailsController'
        };
    }
})();
