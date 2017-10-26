(function() {
    angular
        .module('indigoeln')
        .directive('stuctureSize', stuctureSize);

    /* @ngInject */
    function stuctureSize() {
        return {
            restrict: 'E',
            scope: {
                model: '='
            },
            templateUrl: 'scripts/app/common/directives/structure-size/structure-size.html',
            controller: angular.noop,
            controllerAs: 'vm',
            bindToController: true
        };
    }
})();
