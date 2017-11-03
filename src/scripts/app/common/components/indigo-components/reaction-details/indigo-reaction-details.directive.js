(function() {
    angular
        .module('indigoeln.componentsModule')
        .directive('indigoReactionDetails', indigoReactionDetails);

    function indigoReactionDetails() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/app/common/components/indigo-components/reaction-details/reaction-details.html',
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
