(function () {
    angular
        .module('indigoeln')
        .directive('indigoAutorecovery', indigoAutorecovery);

    function indigoAutorecovery() {
        return {
            restrict: 'E',
            templateUrl: 'scripts/app/entities/autorecovery/autorecovery.html',
            link: link
        };
    }

    /* @ngInject */
    function link($scope) {
    }
})();