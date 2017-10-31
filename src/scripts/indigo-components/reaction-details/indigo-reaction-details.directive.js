(function() {
    angular
        .module('indigoeln.Components')
        .directive('indigoReactionDetails', indigoReactionDetails);

    function indigoReactionDetails() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/indigo-components/reaction-details/reaction-details.html',
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
